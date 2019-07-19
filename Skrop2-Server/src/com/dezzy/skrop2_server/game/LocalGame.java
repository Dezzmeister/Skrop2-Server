package com.dezzy.skrop2_server.game;

/**
 * Subclasses of this class contain game logic and run the actual game that the players play. Subclasses receive player input events from the {@link Game} and send
 * crucial game info through the Game to the players. 
 * 
 * @author Dezzmeister
 *
 */
public abstract class LocalGame implements Runnable {
	protected final Game game;
	public final String name;
	public final int maxPlayers;
	public volatile int currentPlayers = 0;
	
	public final WinCondition winCondition;
	public final String winConditionArg;
	
	public Player[] players;
	
	public LocalGame(final Game _game, final String _name, int _maxPlayers, final WinCondition _winCondition, final String _winConditionArg) {
		game = _game;
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
	 */
	public abstract void processClickEvent(int clientID, float x, float y);
	
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
	synchronized void addPlayer(int clientID, final Player newPlayer) {
		players[clientID] = newPlayer;
		
		recountPlayers();
	}
	
	/**
	 * Removes a player from the game
	 * 
	 * @param clientID clientID of the player that disconnected
	 */
	synchronized void disconnectPlayer(int clientID) {
		Player oldPlayer = players[clientID];
		players[clientID] = null;
		
		System.out.println("Player \"" + oldPlayer.name + "\" has disconnected");
		
		recountPlayers();
	}
	
	/**
	 * Recounts all players to avoid errors that may come from bad client messages
	 */
	synchronized void recountPlayers() {
		int playerCount = 0;
		for (Player player : players) {
			if (player != null) {
				playerCount++;
			}
		}
		
		currentPlayers = playerCount;
	}
}
