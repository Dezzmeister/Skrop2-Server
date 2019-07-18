package com.dezzy.skrop2_server.net.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.dezzy.skrop2_server.Game;

/**
 * Facilitates a TCP connection to another device
 * 
 * @author Dezzmeister
 *
 */
public class Server implements Runnable {
	private final Game game;
	private final int clientID;
	public final int port;
	
	private volatile boolean quit = false;
	private volatile boolean isRunning = true;
	
	private final ServerSocket serverSocket;
	private volatile boolean sendMessage = false;
	private volatile String message = "";
	
	private volatile InetAddress lastClientIP;
	
	public Server(final Game _game, int _port, int _clientID) throws IOException {
		game = _game;
		clientID = _clientID;
		port = _port;
		
		serverSocket = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		while (isRunning) {
			try (Socket socket = serverSocket.accept();
					BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {
				lastClientIP = socket.getInetAddress();
				
				String in = "";
				
				while (!quit) {
					if (din.ready()) {						
						in = din.readLine();
						
						if (in.equals("quit")) {
							quit = true;
							game.processClientEvent(clientID, "quit");
							break;
						}
						
						game.processClientEvent(clientID, in);					
					} else {
						if (sendMessage) {
							
							synchronized(message) { //Prevent sendString() from changing the message as it is being sent
								dout.writeChars(message);
							}
							dout.flush();
								
							sendMessage = false;
						}
					}
				}
				quit = false;
			} catch (Exception e) {
				System.err.println("Error with TCP server on port " + port + ", processing client quit message");
				e.printStackTrace();
				game.processClientEvent(clientID, "quit");
			}
		}
		
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress lastClientIP() {
		return lastClientIP;
	}
	
	public void closeConnection() {
		quit = true;
	}
	
	public void stopServer() {
		quit = true;
		isRunning = false;
	}
	
	/**
	 * Tries to send a String to the client.
	 * 
	 * @param _message String to send, with the newline omitted
	 */
	public void sendString(final String _message) {
		message = _message + "\r\n";
		sendMessage = true;
	}
}
