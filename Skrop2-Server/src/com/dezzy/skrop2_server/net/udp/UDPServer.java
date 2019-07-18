package com.dezzy.skrop2_server.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Sends UDP packets to a client. When the client first sends a packet to this server, the server keeps the IP and destination port
 * and sends future UDP packets to that location.
 * 
 * @author Dezzmeister
 *
 */
public class UDPServer implements Runnable {
	public static final int UDP_PACKET_MAX_BYTE_LENGTH = 700;
	
	private final DatagramSocket socket;
	
	private volatile InetAddress clientIP = null;
	private int portOut = -1;
	
	private int port;
	
	private volatile boolean isRunning = true;
	
	private boolean firstReceived = false;
	private volatile boolean sendMessage = false;
	private volatile String message = "";
	
	public UDPServer(int _port) throws SocketException {
		port = _port;
		
		socket = new DatagramSocket(port);
	}
	
	public void setClient(final InetAddress _clientIP) {
		clientIP = _clientIP;
	}
	
	private byte[] buf = new byte[UDP_PACKET_MAX_BYTE_LENGTH];
	
	@Override
	public void run() {
		while (isRunning) {
			if (!firstReceived) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				clientIP = packet.getAddress();
				portOut = packet.getPort();
				
				firstReceived = true;
			} else {
				if (sendMessage) {
					DatagramPacket packet;
					synchronized(message) { //Prevent sendString() from changing the message as it is being used to create a packet
						for (int i = 0; i < UDP_PACKET_MAX_BYTE_LENGTH; i++) { //Zero the array to destroy old data
							buf[i] = 0;
						}
						
						byte[] bytes = message.getBytes();
						
						System.arraycopy(bytes, 0, buf, 0, bytes.length);
						
						packet = new DatagramPacket(buf, buf.length, clientIP, portOut);
					}
					
					try {
						socket.send(packet);
					} catch (IOException e) {
						System.err.println("Error sending UDP packet to " + clientIP.getHostAddress() + ":" + portOut);
						e.printStackTrace();
					}
					
					sendMessage = false;
				}
			}
		}
		
		socket.close();
		firstReceived = false;
		clientIP = null;
		portOut = -1;
	}
	
	public boolean boundToClient() {
		return firstReceived;
	}
	
	public InetAddress clientIP() {
		return clientIP;
	}
	
	/**
	 * Tell the server to wait for a new client
	 */
	public void reset() {
		firstReceived = false;
		clientIP = null;
		portOut = -1;
	}
	
	/**
	 * Send a message to the client
	 * 
	 * @param _message String to send to the client
	 */
	public void sendString(final String _message) {
		sendMessage = true;
		message = _message;
	}
	
	public void stopServer() {
		isRunning = false;
	}
}
