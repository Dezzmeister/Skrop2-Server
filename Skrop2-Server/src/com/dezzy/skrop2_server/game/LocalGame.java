package com.dezzy.skrop2_server.game;

import com.dezzy.skrop2_server.Game;

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
	
	public abstract void processClickEvent(int clientID, float x, float y);
	
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
	public synchronized void addPlayer(int clientID, final Player newPlayer) {
		players[clientID] = newPlayer;
		
		recountPlayers();
	}
	
	/**
	 * Removes a player from the game
	 * 
	 * @param clientID clientID of the player that disconnected
	 */
	public synchronized void disconnectPlayer(int clientID) {
		Player oldPlayer = players[clientID];
		players[clientID] = null;
		
		System.out.println("Player \"" + oldPlayer.name + "\" has disconnected");
		
		recountPlayers();
	}
	
	/**
	 * Recounts all players to avoid errors that may come from bad client messages
	 */
	public synchronized void recountPlayers() {
		int playerCount = 0;
		for (Player player : players) {
			if (player != null) {
				playerCount++;
			}
		}
		
		currentPlayers = playerCount;
	}
}
