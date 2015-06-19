/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.client;

import java.io.*;
import java.net.*;
import java.util.*;

import sischat.cmd.*;
import sischat.member.*;
import sischat.room.*;
import sischat.msg.*;

/**
 *
 * @author Visat
 */

public class ChatClient implements ChatClientObservable {
    private ChatClientThread clientThread = new ChatClientThread(this);
    private boolean loggedIn = false, joiningRoom = false;

    @Override
    public void addListener(ChatClientListener listener) {
        clientThread.addListener(listener);
    }

    @Override
    public void removeListener(ChatClientListener listener) {
        clientThread.removeListener(listener);
    }

	public boolean connect(String server, int port) {
        return clientThread.connect(server, port);
	}

    public void disconnect() {
        clientThread.disconnect();
    }

    public boolean isConnected() {
        return clientThread.isConnected();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isJoiningRoom() {
        return joiningRoom;
    }

    public void start() {
        new Thread(clientThread).start();
    }

    public void doLogin(String name, int port) {
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_LOGIN, new ChatLoginInfo(name, port)));
    }

    public void doLogout() {
        loggedIn = false;
        joiningRoom = false;
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_LOGOUT, null));
    }

    public void doRoomList() {
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_ROOM_LIST, null));
    }

    public void doJoinRoom(int roomID) {
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_ROOM_JOIN, roomID));
    }

    public void doLeaveRoom() {
        joiningRoom = false;
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_ROOM_LEAVE, null));
    }

    public void doCreateRoom(String roomName) {
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_ROOM_CREATE, roomName));
    }

    public void doMessage(String content) {
        clientThread.sendMessage(new ChatCommand(ChatCommandClient.CLIENT_MESSAGE_SEND, content));
    }

    public class ChatClientThread implements Runnable, ChatClientObservable {
    private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Socket socket;

    private ChatClient sender;
	private boolean connected = false;

    private List<ChatClientListener> listeners = new LinkedList<>();

    public ChatClientThread(ChatClient sender) {
        this.sender = sender;
	}

    public boolean connect(String server, int port) {
        try {
            disconnect();
			socket = new Socket(server, port);
            inputStream  = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
            connected = true;
		}
		catch (Exception e) {
			connected = false;
		}
        return connected;
    }

    public void disconnect() {
        connected = false;
        try { if (inputStream != null) inputStream.close(); }
        catch (Exception e) {}

        try { if (outputStream != null) outputStream.close(); }
        catch (Exception e) {}

        try { if (socket != null) socket.close(); }
        catch (Exception e) {}
    }

    @Override
    public void run() {
        while (connected) {
            try {
                ChatCommand chatCommand = (ChatCommand)inputStream.readObject();
                ChatCommandServer cmdServer = (ChatCommandServer) chatCommand.getType();
                switch (cmdServer) {
                case SERVER_LOGIN_SUCCESS:
                    loggedIn = true;
                    ChatMember loginMember = (ChatMember) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onLoginSuccess(sender, loginMember);
                    break;
                case SERVER_LOGIN_FAIL:
                    loggedIn = false;
                    for (ChatClientListener listener: listeners)
                        listener.onLoginFail(sender);
                    break;
                case SERVER_LOGOUT:
                    loggedIn = false;
                    joiningRoom = false;
                    for (ChatClientListener listener: listeners)
                        listener.onLogout(sender);
                    break;
                case SERVER_ROOM_LIST:
                    List<ChatRoom> rooms = (List<ChatRoom>) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onRoomList(sender, rooms);
                    break;
                case SERVER_ROOM_CREATE_SUCCESS:
                    ChatRoom createdRoom = (ChatRoom) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onRoomCreationSuccess(sender, createdRoom);
                    break;
                case SERVER_ROOM_CREATE_FAIL:
                    for (ChatClientListener listener: listeners)
                        listener.onRoomCreationFail(sender);
                    break;
                case SERVER_ROOM_JOIN_SUCCESS:
                    joiningRoom = true;
                    ChatRoom joinedRoom = (ChatRoom) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onRoomJoinSuccess(sender, joinedRoom);
                    break;
                case SERVER_ROOM_JOIN_FAIL:
                    joiningRoom = false;
                    for (ChatClientListener listener: listeners)
                        listener.onRoomJoinFail(sender);
                    break;
                case SERVER_ROOM_LEAVE:
                    joiningRoom = false;
                    for (ChatClientListener listener: listeners)
                        listener.onRoomLeave(sender);
                    break;
                case SERVER_MESSAGE_SEND:
                    ChatMessage chatMessage = (ChatMessage) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onMessage(sender, chatMessage);
                    break;
                case SERVER_ROOM_MEMBER_LEAVE:
                    ChatMember memberLeave = (ChatMember) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onRoomMemberLeave(sender, memberLeave);
                    break;
                case SERVER_ROOM_MEMBER_JOIN:
                    ChatMember memberJoin = (ChatMember) chatCommand.getMessage();
                    for (ChatClientListener listener: listeners)
                        listener.onRoomMemberJoin(sender, memberJoin);
                    break;
                }
            }
            catch (IOException | ClassNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }
        loggedIn = false;
        joiningRoom = false;

        // Exception occured
        if (connected) {
            disconnect();
            for (ChatClientListener listener: listeners)
                listener.onDisconnected(sender);
        }
    }

    public void sendMessage(ChatCommand message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        }
        catch (Exception e) {
            System.err.println("Error " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void addListener(ChatClientListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ChatClientListener listener) {
        listeners.remove(listener);
    }
}
}

