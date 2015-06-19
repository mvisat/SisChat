/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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
public class P2PServer extends Thread implements P2PObservable {
    private boolean connected = false;
    private ServerSocket serverSocket;
    private boolean keepRunning;
    private List<P2PListener> listeners = new LinkedList<>();
    private List<P2PServerThread> threads = new LinkedList<>();

    public synchronized boolean connect() {
        if (!connected) {
            try {
                // Let system decides the port
                serverSocket = new ServerSocket(0);
                threads.clear();
                connected = true;
            }
            catch (Exception e) {
                connected = false;
            }
        }
        return connected;
    }

    public synchronized void disconnect() {
        if (!connected)
            return;
        connected = false;
        keepRunning = false;
        try {
            for (P2PServerThread thread: threads)
                thread.closeConnection();
            serverSocket.close();
            threads.clear();
        }
        catch (Exception e) {}
        serverSocket = null;

    }

    @Override
	public void run() {
        if (!connected)
            return;
		keepRunning = true;
		try {
			while (keepRunning) {
				Socket socket = serverSocket.accept();
				if (!keepRunning)
					break;
				P2PServerThread serverThread = new P2PServerThread(socket);
                threads.add(serverThread);
				Thread thread = new Thread(serverThread);
                thread.start();
			}
		}
		catch (IOException e) {}
        disconnect();
	}

    public boolean isConnected() {
        return connected;
    }

    public int getPort() {
        if (serverSocket != null)
            return serverSocket.getLocalPort();
        else
            return 0;
    }

    @Override
    public void addListener(P2PListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(P2PListener listener) {
        listeners.remove(listener);
    }

    private class P2PServerThread implements Runnable {
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	private ChatMember member = null;

	public P2PServerThread(Socket socket) {
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
        for (P2PListener listener: listeners)
            listener.onDisconnected(this.member);
		closeConnection();
	}

    private boolean processCommand(ChatCommand chatCommand) {
        boolean keepRunning = true;
        Object message = chatCommand.getMessage();

		switch ((ChatCommandP2P) chatCommand.getType()) {

		case P2P_LOGIN:
            System.out.println("ada yang login");
            this.member = (ChatMember) message;
            for (P2PListener listener: listeners)
                listener.onConnected(this.member);
            break;
        case P2P_LOGOUT:
            System.out.println("ada yang logout");
            for (P2PListener listener: listeners)
                listener.onDisconnected(member);
            this.member = null;
            keepRunning = false;
            break;
        case P2P_MESSAGE:
            System.out.println("ada yang message");
            for (P2PListener listener: listeners)
                listener.onMessage((ChatMessage)message);
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
