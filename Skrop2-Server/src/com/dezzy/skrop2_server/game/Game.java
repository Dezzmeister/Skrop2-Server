package com.dezzy.skrop2_server.game;

import java.io.IOException;

import com.dezzy.skrop2_server.net.tcp.Server;
import com.dezzy.skrop2_server.net.udp.UDPServer;

/**
 * This class manages the infoserver and the gameservers. This class is responsible for interpreting client messages and managing the {@link LocalGame}.
 * This class creates the LocalGame, passes client input events to it, and gives the LocalGame access to TCP and UDP send functions. This class contains no
 * game logic and is intended only to facilitate a general multiplayer game protocol.
 * 
 * @author Dezzmeister
 *
 */
public class Game {
	public volatile GameState gameState = GameState.NO_GAME;
	
	private final String gameName;
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
	
	private final Class<? extends LocalGame> gameClass;
	private final Class<? extends Player> playerClass;
	private final WinCondition[] possibleWinConditions;
	
	/**
	 * Creates a Game and opens a TCP socket for the info server as well as several TCP sockets
	 * for each client. A total of <code>serverCount</code> sockets are opened, with consecutive ports
	 * starting at <code>startPort</code>. Because this class is responsible for creating a {@link LocalGame} object, interpreting a {@link WinCondition}
	 * and adding {@link Player Players} to the LocalGame, it needs extra information so it knows which subclasses to instantiate and which WinConditions to
	 * use. This decouples the server framework from any specific game logic.
	 * 
	 * @param _gameName the name of the game running on this server
	 * @param _serverName the name of the server
	 * @param infoServerPort info server port
	 * @param startPort port of first client server
	 * @param serverCount number of client servers
	 * @param _gameClass the type of game to create when a create-game request is fulfilled
	 * @param _playerClass the type of player to add to the game
	 * @param _possibleWinConditions every potential win condition, so that the server can pick a default or match a requested win condition
	 * @throws IOException if there is an error creating the server sockets
	 */
	public Game(final String _gameName, final String _serverName, int infoServerPort, int startPort, int serverCount, final Class<? extends LocalGame> _gameClass, final Class<? extends Player> _playerClass, final WinCondition[] _possibleWinConditions) throws IOException {
		System.out.println("Starting a " + _gameName + " server named " + _serverName + " with " + serverCount + " consecutive TCP/UDP game server ports, starting at " + startPort + ". The TCP infoserver will run on port " + infoServerPort + ".");
		
		gameName = _gameName;
		serverName = _serverName;
		
		infoServer = new Server(this, infoServerPort, -1);
		
		servers = new Server[serverCount];
		serverThreads = new Thread[serverCount];
		udpServers = new UDPServer[serverCount];
		udpServerThreads = new Thread[serverCount];
		inUse = new boolean[serverCount];
		
		for (int i = 0; i < serverCount; i++) {
			servers[i] = new Server(this, startPort + i, i);
			serverThreads[i] = new Thread(servers[i], gameName + " " + serverName + " TCP Game Server Thread " + i);
			serverThreads[i].start();
			
			udpServers[i] = new UDPServer(startPort + i);
			udpServerThreads[i] = new Thread(udpServers[i], gameName + " " + serverName + " UDP Game Server Thread " + i);
			udpServerThreads[i].start();
			
			inUse[i] = false;
		}
		
		infoServerThread = new Thread(infoServer, gameName + " " + serverName + " Info Server Thread");
		infoServerThread.start();
		
		gameClass = _gameClass;
		playerClass = _playerClass;
		possibleWinConditions = _possibleWinConditions;
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
				try {
					Player player = playerClass.getDeclaredConstructor(String.class).newInstance(name);
					
					localGame.addPlayer(clientID, player);
					System.out.println("Player \"" + name.replace('_', ' ') + "\" has connected to the server on port " + servers[clientID].port);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Player \"" + name + "\" tried to connect on port " + servers[clientID].port + " but could not be added to the game, disconnecting...");
					servers[clientID].closeConnection();
					udpServers[clientID].reset();
					inUse[clientID] = false;
				}
			}
			
			inUse[clientID] = true;
			
			sendFullPlayerList();
			
			if (checkAllPlayersJoined() && checkAllUDPServersBound()) {
				gameState = GameState.BEGINNING;
				System.out.println("All players are connected, beginning the game");
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
					try {
						float x = Float.parseFloat(body.substring(0, body.indexOf(":")));
						float y = Float.parseFloat(body.substring(body.indexOf(":") + 1));
						
						localGame.processClickEvent(clientID, x, y);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
					udpServers[i].openForNewClients(); //Tell the UDP server to expect a new client
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
				
				String playerGameName = gameName + " Game";
				
				int maxPlayers = 2;
				WinCondition winCondition = possibleWinConditions[0];
				String winConditionArg = "";
				
				for (String s : fields) {
					if (s.contains(":")) {
						String fieldHeader = s.substring(0, s.indexOf(":"));
						String fieldBody = s.substring(s.indexOf(":") + 1);
						
						if (fieldHeader.equals("name")) {
							playerGameName = fieldBody.replace('_', ' ');
						} else if (fieldHeader.equals("max-players")) {
							maxPlayers = Integer.parseInt(fieldBody);
						} else if (fieldHeader.equals("win-condition")) {
							for (WinCondition condition : possibleWinConditions) {
								if (condition.getName().equals(fieldBody)) {
									winCondition = condition;
									break;
								}
							}
						} else if (fieldHeader.equals("win-condition-arg")) { //More data on the win condition, interpreted by subclasses of LocalGame
							winConditionArg = fieldBody;
						}
					} else {
						System.err.println("Malformed \"create-game\" request received from client!");
					}
				}
				
				try {
					localGame = gameClass.getDeclaredConstructor(Game.class, String.class, int.class, WinCondition.class, String.class).newInstance(this, playerGameName, maxPlayers, winCondition, winConditionArg);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Error starting the server-side game!");
				}
				
				localGameThread  = new Thread(localGame, gameName + " Server Game Logic Thread");
				localGameThread.start();
				
				System.out.println("Creating a " + gameName + " game with name \"" + playerGameName + "\", max " + maxPlayers + " players, and " + winCondition.getInfoString(winConditionArg));
				gameState = GameState.WAITING_FOR_PLAYERS;
				
				infoServer.sendString("game-info name:" + localGame.name.replace(' ', '_') + " status:" + gameState.toString() + " max-players:" + localGame.maxPlayers + " players:" + localGame.currentPlayers + " win-condition:" + localGame.winCondition.toString() + " win-condition-arg:" + localGame.winConditionArg); //Return the new game info to the client
			} else {
				infoServer.sendString("cannot-create-game");
			}
		}
	}
}
