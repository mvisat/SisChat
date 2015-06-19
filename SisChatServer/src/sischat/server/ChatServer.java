/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import sischat.member.*;
import sischat.cmd.*;
import sischat.msg.*;
import sischat.room.*;

/**
 *
 * @author Visat
 */
public class ChatServer {
	private List<ChatServerThread> serverThreads = new LinkedList<>();

	private int port;
	private boolean keepRunning, connected = false;
    private ServerSocket serverSocket = null;

	public ChatServer(int port) {
		this.port = port;
	}

    public boolean connect() {
        if (!connected) {
            try {
                serverSocket = new ServerSocket(port);
                connected = true;
            }
            catch (Exception e) {
                connected = false;
            }
        }
        return connected;
    }

    public void disconnect() {
        if (!connected)
            return;
        try {
            serverSocket.close();
            serverThreads.stream().forEach((thread) -> {
                thread.closeConnection();
            });
        }
        catch (Exception e) {}
        connected = false;
    }

	public void start() {
        if (!connected)
            return;
		keepRunning = true;
		try {
			while (keepRunning) {
                System.out.println("Waiting connection from client...");
				Socket socket = serverSocket.accept();
				if (!keepRunning)
					break;
                System.out.println("Connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				ChatServerThread serverThread = new ChatServerThread(socket);
				serverThreads.add(serverThread);
				Thread thread = new Thread(serverThread);
                thread.start();
			}
		}
		catch (IOException e) {}
        disconnect();
	}

	public void stop() {
		keepRunning = false;
		try {
            Socket socket = new Socket("localhost", port);
		}
		catch(Exception e) {}
	}

    public boolean isConnected() {
        return connected;
    }

    private synchronized void broadcast(ChatCommand chatCommand) {
        for (Iterator<ChatServerThread> it = serverThreads.listIterator(); it.hasNext();) {
            ChatServerThread thread = it.next();
            if (!thread.sendMessage(chatCommand))
                it.remove();
        }
    }

    private synchronized void broadcastTo(int roomID, ChatCommand chatCommand) {
        for (Iterator<ChatServerThread> it = serverThreads.listIterator(); it.hasNext();) {
            ChatServerThread thread = it.next();
            if (thread.member != null && thread.member.getRoomID() == roomID) {
                if (!thread.sendMessage(chatCommand)) {
                    it.remove();
                }
            }
        }
    }

    private class ChatServerThread implements Runnable {
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	private ChatMember member = null;

	public ChatServerThread(Socket socket) {
		this.socket = socket;
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream  = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e) {}
	}

	@Override
	public void run() {
        ChatCommand chatCommand;
		do {
			try {
				chatCommand = (ChatCommand)inputStream.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
				break;
			}
		} while (processCommand(chatCommand));
		closeConnection();
	}

    private boolean processCommand(ChatCommand chatCommand) {
        boolean keepRunning = true;
        Object message = chatCommand.getMessage();
        ChatCommandClient cmdClient = (ChatCommandClient) chatCommand.getType();
		switch (cmdClient) {

		case CLIENT_LOGIN:
            ChatLoginInfo loginInfo = (ChatLoginInfo)message;
            System.out.println("CLIENT_LOGIN " + loginInfo.getName());
			if ((member != null) || ((member = ChatMemberManager.add(loginInfo.getName(), socket.getInetAddress(), loginInfo.getPort())) == null)) {
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_LOGIN_FAIL, null));
            }
            else {
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_LOGIN_SUCCESS, member));
            }
			break;

		case CLIENT_LOGOUT:
            System.out.println("CLIENT_LOGOUT");
            clientLogout();
            keepRunning = sendMessage(new ChatCommand(ChatCommandServer.SERVER_LOGOUT, null));
			break;

        case CLIENT_ROOM_LIST:
            System.out.println("CLIENT_ROOM_LIST");
            keepRunning = sendMessage(
                    new ChatCommand(ChatCommandServer.SERVER_ROOM_LIST, ChatRoomManager.getRooms()));
            break;

        case CLIENT_ROOM_CREATE:
            System.out.println("CLIENT_ROOM_CREATE");
            ChatRoom createdRoom = ChatRoomManager.add((String)message);
            if (createdRoom != null) {
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_ROOM_CREATE_SUCCESS, createdRoom));
            }
            else {
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_ROOM_CREATE_FAIL, null));
            }
            break;

		case CLIENT_ROOM_JOIN:
            System.out.println("CLIENT_ROOM_JOIN");
            ChatRoom joinedRoom = ChatRoomManager.join(member, (Integer)message);
            if (joinedRoom != null) {
                // I don't know but I have to copy the object
                ChatRoom copiedRoom = new ChatRoom(joinedRoom);
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_ROOM_JOIN_SUCCESS, copiedRoom));
                clientJoinRoom();
            }
            else
                keepRunning = sendMessage(
                        new ChatCommand(ChatCommandServer.SERVER_ROOM_JOIN_FAIL, null));
			break;

        case CLIENT_ROOM_LEAVE:
            System.out.println("CLIENT_ROOM_LEAVE");
            clientLeaveRoom();
            keepRunning = sendMessage(
                    new ChatCommand(ChatCommandServer.SERVER_ROOM_LEAVE, null));
            break;

        case CLIENT_MESSAGE_SEND:
            System.out.println("CLIENT_MESSAGE_SEND");
            broadcastTo(member.getRoomID(),
                    new ChatCommand(ChatCommandServer.SERVER_MESSAGE_SEND,
                            new ChatMessage(member, (String)message)));
            break;
		}
        return keepRunning;
    }

	/**
	 * Close connection
	 */
	public void closeConnection() {
		try { if (outputStream != null) outputStream.close(); }
		catch (Exception e) {}

		try { if (inputStream != null) inputStream.close(); }
		catch (Exception e) {}

		try { if (socket != null) socket.close(); }
		catch (Exception e) {}

        clientLogout();
	}

    private void clientLogout() {
        if (member != null) {
            clientLeaveRoom();
            ChatMemberManager.remove(member);
            member = null;
        }
    }
    private void clientLeaveRoom() {
        broadcastTo(member.getRoomID(),
                new ChatCommand(ChatCommandServer.SERVER_ROOM_MEMBER_LEAVE, member));
        ChatRoomManager.leave(member);
    }

    private void clientJoinRoom() {
        broadcastTo(member.getRoomID(),
                new ChatCommand(ChatCommandServer.SERVER_ROOM_MEMBER_JOIN, member));
    }

	/**
	 * Send a message to the client
	 * @param chatCommand
	 * @return
	 */
	private boolean sendMessage(ChatCommand chatCommand) {
		if (!socket.isConnected()) {
			closeConnection();
			return false;
		}
		try {
			outputStream.writeObject(chatCommand);
            outputStream.flush();
		}
		catch (IOException e) {
			return false;
		}
		return true;
	}
    }
}


