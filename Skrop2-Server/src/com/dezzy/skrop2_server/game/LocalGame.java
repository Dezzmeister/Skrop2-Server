package com.dezzy.skrop2_server.game;

import com.dezzy.skrop2_server.Game;

/**
 * Controls the game logic
 * 
 * @author Dezzmeister
 *
 */
public class LocalGame implements Runnable {
	private final Game game;
	public final String name;
	public final int maxPlayers;
	public volatile int currentPlayers = 0;
	
	private World gameWorld;
	
	public Player[] players;
	
	public final WinCondition winCondition;
	public final int winConditionArg;
	
	public LocalGame(final Game _game, final String _name, int _maxPlayers, final WinCondition _winCondition, int _winConditionArg) {
		game = _game;
		name = _name;
		maxPlayers = _maxPlayers;
		players = new Player[maxPlayers];
		
		winCondition = _winCondition;
		winConditionArg = _winConditionArg;
	}
	
	private long startCountdownTime = 0;
	private int prevSecondsLeft = 4;
	private int secondsLeft = SECONDS_TO_WAIT; //This will be sent to the clients to be displayed
	private static final int SECONDS_TO_WAIT = 3;
	
	private long gameStartTime = 0;
	private int prevGameSecondsLeft = 0;
	private int gameSecondsLeft = 0;
	
	public void gameTick() {
		switch (game.gameState) {
		case WAITING_FOR_PLAYERS:
			startCountdownTime = System.currentTimeMillis();
			break;
		case BEGINNING:			
			secondsLeft = (int)(SECONDS_TO_WAIT - ((System.currentTimeMillis() - startCountdownTime)/1000));
			
			if (secondsLeft != prevSecondsLeft && secondsLeft != SECONDS_TO_WAIT) {
				game.broadcastTCP("countdown-timer " + secondsLeft);
			}
			prevSecondsLeft = secondsLeft;
			if ((System.currentTimeMillis() - startCountdownTime)/1000 > SECONDS_TO_WAIT) {
				System.out.println("Creating the game world and starting the game...");
				createGameWorld();
				gameStartTime = System.currentTimeMillis();
				game.gameState = GameState.IN_GAME;
			}
			break;
		case IN_GAME:
			inGameTick();
			break;
		case GAME_ENDING:
			endGame();
			break;
		default:
			break;
		}
	}
	
	public void processClickEvent(int clientID, float x, float y) {
		if (game.gameState == GameState.IN_GAME) {
			int points = gameWorld.checkClick(x, y);
			
			if (points > 0) {
				players[clientID].score += points;
				players[clientID].rectsDestroyed++;
				
				sendScoreUpdate();
			}
		}
	}
	
	private void sendScoreUpdate() {
		String message = "scores";
		
		Player[] ranked = rankPlayers(!winCondition.countRects);
		
		for (Player p : ranked) {
			message += " " + p.name + ":";
			
			if (winCondition.countRects) {
				message += p.rectsDestroyed;
			} else {
				message += p.score;
			}
		}
		
		game.broadcastTCP(message);
	}
	
	private void createGameWorld() {
		gameWorld = new World(10);
		sendGameWorld();
	}
	
	private void inGameTick() {
		if (winCondition == WinCondition.TIMER_POINTS || winCondition == WinCondition.TIMER_RECTS) {
			gameSecondsLeft = (int)(winConditionArg - ((System.currentTimeMillis() - gameStartTime)/1000));
		}
		
		if (gameSecondsLeft != prevGameSecondsLeft && gameSecondsLeft != winConditionArg) {
			game.broadcastTCP("game-timer " + gameSecondsLeft);
		}
		prevGameSecondsLeft = gameSecondsLeft;
		
		if ((System.currentTimeMillis() - gameStartTime)/1000 > winConditionArg) {
			game.gameState = GameState.GAME_ENDING;
		}
		
		gameWorld.update();
		sendGameWorld();
	}
	
	private long lastGameWorldBroadcastTime = 0;
	/**
	 * Measured in Hz
	 */
	private static final long GAMEWORLD_BROADCAST_FREQUENCY = 60;
	
	private void sendGameWorld() {
		try {
			if (System.currentTimeMillis() - lastGameWorldBroadcastTime > 1000/GAMEWORLD_BROADCAST_FREQUENCY) {
				String serializedWorld = gameWorld.serialize();
				game.broadcastUDP(serializedWorld);
				lastGameWorldBroadcastTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			System.err.println("Error preparing the game world to be sent over UDP!");
			e.printStackTrace();
		}
	}
	
	boolean endGameScoresSent = false;
	
	private void endGame() {
		if (!endGameScoresSent) {
			Player[] ranked = rankPlayers(!winCondition.countRects);
			
			String message = "end-game-scores";
			for (Player p : ranked) {
				message += " " + p.name + ":" + p.score + ":" + p.rectsDestroyed;
			}
			
			game.broadcastTCP(message);
			
			endGameScoresSent = true;
		}
	}
	
	private Player[] rankPlayers(boolean sortByPoints) {
		Player[] ranked = new Player[players.length];
		System.arraycopy(players, 0, ranked, 0, players.length);
		
		boolean sorted = false;
		
		while (!sorted) {
			sorted = true;
			
			for (int i = 1; i < players.length; i++) {				
				if ((sortByPoints && ranked[i].score > ranked[i - 1].score) || (!sortByPoints && ranked[i].rectsDestroyed > ranked[i - 1].rectsDestroyed)) {
					sorted = false;
					Player temp = ranked[i - 1];
					ranked[i - 1] = ranked[i];
					ranked[i] = temp;
				}
			}
		}
		
		return ranked;
	}
	
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
	public synchronized void addPlayer(int clientID, final String name) {
		players[clientID] = new Player(name);
		
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
