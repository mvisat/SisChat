/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import sischat.cmd.ChatCommand;
import sischat.cmd.ChatCommandP2P;
import sischat.member.ChatMember;
import sischat.msg.ChatMessage;

/**
 *
 * @author Visat
 */
public class P2PClient implements P2PObservable {
    private ChatMember receiver, sender;
    private P2PClientThread clientThread = new P2PClientThread();
    private List<P2PListener> listeners = new LinkedList<>();

    public P2PClient(ChatMember sender, ChatMember receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public boolean connect() {
        return clientThread.connect(receiver.getAddress().getHostAddress(), receiver.getP2PPort());
    }

    public void disconnect() {
        clientThread.disconnect();
    }

    public void doLogin() {
        clientThread.sendMessage(new ChatCommand(ChatCommandP2P.P2P_LOGIN, sender));
    }

    public void doLogout() {
        clientThread.sendMessage(new ChatCommand(ChatCommandP2P.P2P_LOGOUT, sender));
    }

    public boolean doMessage(String message) {
        return clientThread.sendMessage(new ChatCommand(ChatCommandP2P.P2P_MESSAGE,
                new ChatMessage(sender, message)));
    }

    public ChatMember getReceiver() {
        return this.receiver;
    }

    @Override
    public void addListener(P2PListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(P2PListener listener) {
        listeners.remove(listener);
    }

    public class P2PClientThread implements Runnable {
    private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private Socket socket;

	private boolean connected = false;

    public P2PClientThread() {

	}

    public boolean connect(String server, int port) {
        try {
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

                switch ((ChatCommandP2P) chatCommand.getType()) {
                case P2P_MESSAGE:
                    for (P2PListener listener: listeners)
                        listener.onMessage((ChatMessage) chatCommand.getMessage());
                    break;
                }
            }
            catch (IOException | ClassNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }
        // Exception occured
        if (connected) {
            disconnect();
            for (P2PListener listener: listeners)
                listener.onDisconnected(receiver);
        }
    }

    public boolean sendMessage(ChatCommand message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
}
