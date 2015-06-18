/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sischat.server;

/**
 *
 * @author Visat
 */
public class ChatServerConsole {
    public static void main(String[] args) {
		int port = 1500;
		switch (args.length) {
			case 1:
				try {
					port = Integer.parseInt(args[0]);
				}
				catch (Exception e) {
					System.out.println("Error: Invalid port number");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage: java port_number");
				return;
		}

		ChatServer server = new ChatServer(port);
        if (server.connect()) {
            System.out.println("Starting server...");
            server.start();
        }
        else {
            System.out.println("Server failed to connect.");
        }
	}
}
