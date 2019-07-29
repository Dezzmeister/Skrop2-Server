package com.dezzy.skrop2_server.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.dezzy.skrop2_server.server.GameServer;

/**
 * Facilitates a TCP connection to another device
 * 
 * @author Dezzmeister
 *
 */
public class Server implements Runnable {
	
	/**
	 * The {@link GameServer} in control of this Server
	 */
	private final GameServer gameServer;
	
	/**
	 * Server identifier
	 */
	private final int clientID;
	
	/**
	 * True if the server should close its current connection
	 */
	private volatile boolean quit = false;
	
	/**
	 * True if the server should continue running
	 */
	private volatile boolean isRunning = true;
	
	private final ServerSocket serverSocket;
	
	/**
	 * Server TCP port
	 */
	public final int port;
	
	/**
	 * True if the server should try to send the contents of <code>message</code>
	 */
	private volatile boolean sendMessage = false;
	
	/**
	 * Contains the next message to be sent to the client
	 */
	private volatile String message = "";
	
	/**
	 * Create a TCP server with the specified {@link GameServer}.
	 * 
	 * @param _game GameServer object controlling this Server
	 * @param _port TCP port the server will open on
	 * @param _clientID number to identify the client connected to this server
	 * @throws IOException if the {@link java.net.ServerSocket ServerSocket} cannot be created
	 */
	public Server(final GameServer _game, int _port, int _clientID) throws IOException {
		gameServer = _game;
		clientID = _clientID;
		port = _port;
		
		serverSocket = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		while (isRunning) {
			try (Socket socket = serverSocket.accept();
					BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter dout = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()))) {
				System.out.println("Client connected to TCP port " + port);
				
				String in = "";
				
				while (!quit) {
					if (din.ready()) {						
						in = din.readLine();
						
						if (in.equals("quit")) {
							quit = true;
							gameServer.processClientEvent(clientID, "quit");
							System.out.println("Client closing connection to TCP port " + port);
							break;
						}
						
						gameServer.processClientEvent(clientID, in);					
					} else {
						if (sendMessage) {
							
							synchronized(message) { //Prevent sendString() from changing the message as it is being sent
								dout.println(message);
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
				gameServer.processClientEvent(clientID, "quit");
			}
		}
		
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the IP of the last client to connect to this server, or the current client if one is connected.
	 * 
	 * @return the last client to connect to this server
	 */
	public InetAddress lastClientIP() {
		return serverSocket.getInetAddress();
	}
	
	/**
	 * Forces the server to close its current connection.
	 */
	public void closeConnection() {
		quit = true;
	}
	
	/**
	 * Stops the server and closes the {@link java.net.ServerSocket ServerSocket}.
	 */
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
		message = _message;
		sendMessage = true;
	}
}
