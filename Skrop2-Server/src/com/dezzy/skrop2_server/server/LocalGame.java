package com.dezzy.skrop2_server.server;

/**
 * Subclasses of this class contain game logic and run the actual game that the players play. Subclasses receive player input events from the {@link GameServer} and send
 * crucial game info through the GameServer to the players. 
 * 
 * @author Dezzmeister
 *
 */
public abstract class LocalGame implements Runnable {
	protected final GameServer gameServer;
	public final String name;
	public final int maxPlayers;
	public volatile int currentPlayers = 0;
	
	public final WinCondition winCondition;
	public final String winConditionArg;
	
	public final Player[] players;
	
	/**
	 * Creates a LocalGame with the specified parameters. The LocalGame controls game logic and is created/destroyed by the {@link GameServer}.
	 * 
	 * @param _game Game object
	 * @param _name name of the game/match
	 * @param _maxPlayers number of players to expect
	 * @param _winCondition when to end this game/match
	 * @param _winConditionArg auxiliary win condition information
	 */
	public LocalGame(final GameServer _game, final String _name, int _maxPlayers, final WinCondition _winCondition, final String _winConditionArg) {
		gameServer = _game;
		name = _name;
		maxPlayers = _maxPlayers;
		players = new Player[maxPlayers];
		
		winCondition = _winCondition;
		winConditionArg = _winConditionArg;
	}
	
	/**
	 * Processes a client's click at the specified normalized coordinates.
	 * 
	 * @param clientID clientID of the client
	 * @param x x coordinate of the click, from 0 to 1
	 * @param y y coordinate of the click, from 0 to 1
	 * @param aux any auxiliary information that the game may need about a click event
	 */
	public abstract void processClickEvent(int clientID, float x, float y, final String aux);
	
	/**
	 * Run one game tick
	 */
	protected abstract void gameTick();
	
	@Override
	public void run() {
		while (true) {
			gameTick();
		}
	}
	
	/**
	 * Adds a player to the game
	 * 
	 * @param clientID clientID of the player that connected and joined the game
	 * @param name name of the player
	 */
	synchronized final void addPlayer(int clientID, final Player newPlayer) {
		players[clientID] = newPlayer;
		
		recountPlayers();
	}
	
	/**
	 * Removes a player from the game
	 * 
	 * @param clientID clientID of the player that disconnected
	 */
	synchronized final void disconnectPlayer(int clientID) {
		Player oldPlayer = players[clientID];
		players[clientID] = null;
		
		System.out.println("Player \"" + oldPlayer.name + "\" has disconnected");
		
		recountPlayers();
	}
	
	/**
	 * Recounts all players to avoid errors that may come from bad client messages
	 */
	synchronized final void recountPlayers() {
		int playerCount = 0;
		for (Player player : players) {
			if (player != null) {
				playerCount++;
			}
		}
		
		currentPlayers = playerCount;
	}
}
