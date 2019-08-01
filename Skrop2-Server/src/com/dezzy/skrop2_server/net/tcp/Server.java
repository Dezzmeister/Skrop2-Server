package com.dezzy.skrop2_server.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	 * Time (in milliseconds) to wait between messages before considering a client to have timed out 
	 */
	private final int timeoutMillis;
	
	/**
	 * Time in milliseconds at which the last message was received from the client, or if no messages have been received yet,
	 * the time at which the client connected
	 */
	private long lastMessageReceived;
	
	/**
	 * A FIFO that contains any messages that need to be sent to the client;
	 * this is better than the old <code>sendMessage</code> flag because multiple messages can wait in a queue instead of destroying any unsent message
	 */
	private final ConcurrentLinkedQueue<String> messageQueue;
	
	/**
	 * Create a TCP server with the specified {@link GameServer}.
	 * 
	 * @param _game GameServer object controlling this Server
	 * @param _port TCP port the server will open on
	 * @param _clientID number to identify the client connected to this server
	 * @param _timeoutMillis number of milliseconds to wait between messages before notifying the game server of a timeout
	 * @throws IOException if the {@link java.net.ServerSocket ServerSocket} cannot be created
	 */
	public Server(final GameServer _game, int _port, int _clientID, int _timeoutMillis) throws IOException {
		gameServer = _game;
		clientID = _clientID;
		port = _port;
		timeoutMillis = _timeoutMillis;
		
		messageQueue = new ConcurrentLinkedQueue<String>();
		
		serverSocket = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		while (isRunning) {
			messageQueue.clear();
			
			try (Socket socket = serverSocket.accept();
					BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter dout = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()))) {
				System.out.println("Client connected to TCP port " + port);
				
				String in = "";
				
				lastMessageReceived = System.currentTimeMillis();
				while (!quit || !messageQueue.isEmpty()) {
					
					if (din.ready()) {
						in = din.readLine();
						
						lastMessageReceived = System.currentTimeMillis();
						
						if (in == null || in.equals("quit")) {
							quit = true;
							gameServer.processClientEvent(clientID, "quit");
							break;
						}
						
						
						gameServer.processClientEvent(clientID, in);
					} else {
						
						String message = null;
						boolean send = false; //True if any messages need to be sent
						
						while ((message = messageQueue.poll()) != null) {
							dout.println(message);
							send = true;
						}
						
						if (send) {
							dout.flush(); //Flush the buffer once instead of for every waiting message, for performance
						}
					}
					
					if (System.currentTimeMillis() - lastMessageReceived > timeoutMillis) {
						if (!quit) {
							gameServer.processClientEvent(clientID, "timeout");
						}
					}
				}
				quit = false;
				messageQueue.clear();
			} catch (Exception e) {
				System.err.println("Error with TCP server on port " + port + ", processing client quit message");
				e.printStackTrace();
				gameServer.processClientEvent(clientID, "quit");
				messageQueue.clear();
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
		/*
		message = _message;
		sendMessage = true;
		*/
		messageQueue.add(_message);
	}
	
	/**
	 * True if the server is waiting to send a message.
	 * 
	 * @return true if the server needs to send a message
	 */
	public boolean sendingMessage() {
		return !messageQueue.isEmpty();
	}
}
