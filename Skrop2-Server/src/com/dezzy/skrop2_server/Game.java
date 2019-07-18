package com.dezzy.skrop2_server;

import java.io.IOException;

import com.dezzy.skrop2_server.game.GameState;
import com.dezzy.skrop2_server.game.LocalGame;
import com.dezzy.skrop2_server.game.Player;
import com.dezzy.skrop2_server.game.WinCondition;
import com.dezzy.skrop2_server.net.tcp.Server;
import com.dezzy.skrop2_server.net.udp.UDPServer;

/**
 * Clients that want to join or create a game communicate with the info server first. The info server provides game and
 * server info and tells clients which port to connect to if they want to join a game. If there is no game running, clients can create a game
 * by communicating with the info server. All conversations with the info server should be as quick as possible to allow other clients to communicate.
 * 
 * @author Dezzmeister
 *
 */
public class Game {
	public volatile GameState gameState = GameState.NO_GAME;
	
	private final String serverName;
	
	private final Server infoServer;
	private final Thread infoServerThread;
	
	private final Server[] servers;
	private final Thread[] serverThreads;
	private final UDPServer[] udpServers;
	private final Thread[] udpServerThreads;
	private final boolean[] inUse;
	
	private LocalGame localGame;
	private Thread localGameThread;
	
	/**
	 * Creates a Game and opens a TCP socket for the info server as well as several TCP sockets
	 * for each client. A total of <code>serverCount</code> sockets are opened, with consecutive ports
	 * starting at <code>startPort</code>.
	 * 
	 * @param _serverName the name of the server
	 * @param infoServerPort info server port
	 * @param startPort port of first client server
	 * @param serverCount number of client servers
	 * @throws IOException if there is an error creating the server sockets
	 */
	public Game(final String _serverName, int infoServerPort, int startPort, int serverCount) throws IOException {
		serverName = _serverName;
		
		infoServer = new Server(this, infoServerPort, -1);
		
		servers = new Server[serverCount];
		serverThreads = new Thread[serverCount];
		udpServers = new UDPServer[serverCount];
		udpServerThreads = new Thread[serverCount];
		inUse = new boolean[serverCount];
		
		for (int i = 0; i < serverCount; i++) {
			servers[i] = new Server(this, startPort + i, i);
			serverThreads[i] = new Thread(servers[i], "Skrop 2 " + serverName + " TCP Game Server Thread " + i);
			serverThreads[i].start();
			
			udpServers[i] = new UDPServer(startPort + i);
			udpServerThreads[i] = new Thread(udpServers[i], "Skrop 2 " + serverName + " UDP Game Server Thread " + i);
			udpServerThreads[i].start();
			
			inUse[i] = false;
		}
		
		infoServerThread = new Thread(infoServer, "Skrop 2 " + serverName + " Info Server Thread");
		infoServerThread.start();
	}
	
	/**
	 * Processes a message received from the client.
	 * 
	 * @param clientID ID of the client that sent the message
	 * @param message String message received from client
	 */
	public void processClientEvent(int clientID, final String message) {
		String header = message;
		String body = "";
		
		if (message.contains(" ")) {
			header = message.substring(0, message.indexOf(" "));
			body = message.substring(message.indexOf(" ") + 1);	
		}
		
		if (header.equals("init-player")) { //A new player has connected to the server
			if (gameState == GameState.WAITING_FOR_PLAYERS) {
				String name = body.substring(body.indexOf(":") + 1);
				localGame.addPlayer(clientID, name);
				System.out.println("Player \"" + name.replace('_', ' ') + "\" has connected to the server on port " + servers[clientID].port);
			}
			
			inUse[clientID] = true;
			
			sendFullPlayerList();
			
			if (checkAllPlayersJoined() && checkAllUDPServersBound()) {
				gameState = GameState.BEGINNING;
				System.out.println("All players are connected, beginning the game countdown...");
			}
		} else if (header.equals("quit")) {
			if (clientID >= 0) {
				udpServers[clientID].reset();				
				localGame.disconnectPlayer(clientID);
				inUse[clientID] = false;
			}
		} else if (header.equals("c")) {
			if (gameState == GameState.IN_GAME) {
				if (body.contains(":")) {
					float x = Float.parseFloat(body.substring(0, body.indexOf(":")));
					float y = Float.parseFloat(body.substring(body.indexOf(":") + 1));
					
					localGame.processClickEvent(clientID, x, y);
				} else {
					System.err.println("Malformed body in click event received from client: \"" + body + "\"");
				}
			}
		}
		
		if (clientID == -1) {
			handleInfoMessage(header, body);
		}
	}
	
	private void sendFullPlayerList() {
		String playerList = "player-list";
		
		for (Player player : localGame.players) {
			if (player != null) {
				playerList += " name:" + player.name;
			}
		}
		
		broadcastTCP(playerList);
	}
	
	public void broadcastTCP(final String message) {
		for (int i = 0; i < servers.length; i++) {
			servers[i].sendString(message);
		}
	}
	
	public void broadcastUDP(final String message) {
		for (int i = 0; i < udpServers.length; i++) {
			udpServers[i].sendString(message);
		}
	}
	
	private boolean checkAllPlayersJoined() {
		int connections = 0;
		for (boolean playerConnected : inUse) {
			if (playerConnected) {
				connections++;
			}
		}
		
		return (connections == localGame.maxPlayers);
	}
	
	private boolean checkAllUDPServersBound() {
		for (int i = 0; i < udpServers.length; i++) {
			if (inUse[i] && !udpServers[i].boundToClient()) {
				return false;
			}
		}
		
		return true;
	}
	
	private void handleInfoMessage(final String header, final String body) {
		if (header.equals("server-info-request")) { //The client requested info about the server
			System.out.println("Client requesting server info");
			infoServer.sendString("server-info name:" + serverName.replace(' ', '_') + " open-ports:" + servers.length + " game-running:" + (gameState != GameState.NO_GAME));
		} else if (header.equals("join-game")) { //The client wants to join the game and needs a port
			System.out.println("Client requesting to join game");
			
			if (gameState == GameState.NO_GAME) {
				infoServer.sendString("join-game-no-game");
			} else {
				
				int i = 0;
				while (inUse[i] == true && i < localGame.maxPlayers) {
					i++;
				}
				
				if (i == localGame.maxPlayers) { //The game has reached the max number of players
					infoServer.sendString("game-full");
				} else {
					infoServer.sendString("port " + servers[i].port);
				}
			}
		} else if (header.equals("game-info-request")) { //The client has requested info about the current game
			System.out.println("Client requesting game info");
			if (gameState != GameState.NO_GAME) {
				infoServer.sendString("game-info name:" + localGame.name + " status:" + gameState.toString() + " max-players:" + localGame.maxPlayers + " players:" + localGame.currentPlayers + " win-condition:" + localGame.winCondition.toString() + " win-condition-arg:" + localGame.winConditionArg);
			} else {
				infoServer.sendString("game-info-no-game");
			}
		} else if (header.equals("create-game")) { //The client wants to create a game
			
			if (gameState == GameState.NO_GAME) {
				String[] fields = body.split(" ");
				
				String gameName = "Skrop 2 Game";
				
				int maxPlayers = 2;
				WinCondition winCondition = WinCondition.FIRST_TO_X_POINTS;
				int winConditionArg = 500;
				
				for (String s : fields) {
					if (s.contains(":")) {
						String fieldHeader = s.substring(0, s.indexOf(":"));
						String fieldBody = s.substring(s.indexOf(":") + 1);
						
						if (fieldHeader.equals("name")) {
							gameName = fieldBody.replace('_', ' ');
						} else if (fieldHeader.equals("max-players")) {
							maxPlayers = Integer.parseInt(fieldBody);
						} else if (fieldHeader.equals("win-condition")) {
							for (WinCondition condition : WinCondition.values()) {
								if (condition.toString().equals(fieldBody)) {
									winCondition = condition;
									break;
								}
							}
						} else if (fieldHeader.equals("win-condition-arg")) { //Number of points to reach, rectangles or destroy, or seconds to wait until ending the game
							winConditionArg = Integer.parseInt(fieldBody);
						}
					} else {
						System.err.println("Malformed \"create-game\" request received from client!");
					}
				}
				
				localGame = new LocalGame(this, gameName, maxPlayers, winCondition, winConditionArg);
				localGameThread  = new Thread(localGame, "Skrop 2 Server Game Logic Thread");
				localGameThread.start();
				
				
				String winInfoString = "";
				/*
				switch (winCondition) {
				case FIRST_TO_X_POINTS:
					winInfoString = "is the first to " + winConditionArg + " points";
					break;
				}
				*/
				
				System.out.println("Creating a game with name \"" + gameName + "\", max " + maxPlayers + " players, and the winner " + winInfoString);
				gameState = GameState.WAITING_FOR_PLAYERS;
				
				infoServer.sendString("game-info name:" + localGame.name.replace(' ', '_') + " status:" + gameState.toString() + " max-players:" + localGame.maxPlayers + " players:" + localGame.currentPlayers + " win-condition:" + localGame.winCondition.toString() + " win-condition-arg:" + localGame.winConditionArg); //Return the new game info to the client
			} else {
				infoServer.sendString("cannot-create-game");
			}
		}
	}
}