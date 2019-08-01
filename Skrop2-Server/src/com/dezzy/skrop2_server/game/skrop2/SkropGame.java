package com.dezzy.skrop2_server.game.skrop2;

import com.dezzy.skrop2_server.server.GameServer;
import com.dezzy.skrop2_server.server.GameState;
import com.dezzy.skrop2_server.server.LocalGame;
import com.dezzy.skrop2_server.server.Player;
import com.dezzy.skrop2_server.server.WinCondition;

/**
 * Controls the game logic for Skrop 2.
 * 
 * @author Dezzmeister
 *
 */
public class SkropGame extends LocalGame {
	
	private World gameWorld;
	
	private long startCountdownTime = 0;
	private int prevSecondsLeft = 4;
	private int secondsLeft = SECONDS_TO_WAIT; //This will be sent to the clients to be displayed
	private static final int SECONDS_TO_WAIT = 3;
	
	private final SkropWinCondition skropWinCondition;
	private final int winGoal;
	
	private long gameStartTime = 0;
	private int prevGameSecondsLeft = 0;
	private int gameSecondsLeft = 0;
	
	public SkropGame(final GameServer _game, final String _name, int _maxPlayers, final WinCondition _winCondition, final String _winConditionArg) {
		super(_game, _name, _maxPlayers, _winCondition, _winConditionArg);
		
		skropWinCondition = (SkropWinCondition) winCondition;
		winGoal = Integer.parseInt(winConditionArg);
	}
	
	/**
	 * Measured in Hz
	 */
	private static final int LOCAL_GAME_UPDATE_FREQUENCY = 60;
	private long lastGameUpdateTime = 0;
	
	@Override
	public void gameTick() {
		switch (gameServer.gameState) {
		case WAITING_FOR_PLAYERS:
			startCountdownTime = System.currentTimeMillis();
			break;
		case BEGINNING:			
			secondsLeft = (int)(SECONDS_TO_WAIT - ((System.currentTimeMillis() - startCountdownTime)/1000));
			
			if (secondsLeft != prevSecondsLeft && secondsLeft != SECONDS_TO_WAIT) {
				gameServer.broadcastTCP("countdown-timer " + (secondsLeft + 1));
			}
			prevSecondsLeft = secondsLeft;
			if ((System.currentTimeMillis() - startCountdownTime)/1000 > SECONDS_TO_WAIT) {
				System.out.println("Creating the game world and starting the game...");
				createGameWorld();
				gameServer.broadcastTCP("game-begin");
				gameStartTime = System.currentTimeMillis();
				gameServer.gameState = GameState.IN_GAME;
			}
			break;
		case IN_GAME:
			if (System.currentTimeMillis() - lastGameUpdateTime > 1000/LOCAL_GAME_UPDATE_FREQUENCY) {
				inGameTick();
				lastGameUpdateTime = System.currentTimeMillis();
			}
			break;
		case GAME_ENDING:
			endGame();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void processClickEvent(int clientID, float x, float y) {
		if (gameServer.gameState == GameState.IN_GAME) {
			int points = gameWorld.checkClick(x, y);
			
			if (points > 0) {
				if (players[clientID] instanceof SkropPlayer) {
					((SkropPlayer) players[clientID]).score += points;
					((SkropPlayer) players[clientID]).rectsDestroyed++;
				} else {
					System.err.println("Player \"" + players[clientID].name + "\" is not a valid SkropPlayer!");
				}
				
				sendScoreUpdate();
			}
		}
	}
	
	private void sendScoreUpdate() {		
		String message = "scores";
		
		Player[] ranked = rankPlayers(!skropWinCondition.countRects);
		
		for (Player p : ranked) {
			if (p instanceof SkropPlayer) { //Can't wait for pattern matching in Java
				SkropPlayer player = (SkropPlayer) p;
				
				message += " " + player.name.replace(' ', '_') + ":";
				
				if (skropWinCondition.countRects) {
					message += player.rectsDestroyed;
				} else {
					message += player.score;
				}
			} else {
				System.err.println("Player \"" + p.name + "\" is not a valid SkropPlayer!");
			}
		}
		
		gameServer.broadcastTCP(message);
	}
	
	private void createGameWorld() {
		gameWorld = new World(10);
		sendGameWorld();
	}
	
	private void inGameTick() {
		if (skropWinCondition == SkropWinCondition.TIMER_POINTS || skropWinCondition == SkropWinCondition.TIMER_RECTS) {
			gameSecondsLeft = (int)(winGoal - ((System.currentTimeMillis() - gameStartTime)/1000));
		}
		
		if (gameSecondsLeft != prevGameSecondsLeft && gameSecondsLeft != winGoal) {
			gameServer.broadcastTCP("game-timer " + gameSecondsLeft);
		}
		prevGameSecondsLeft = gameSecondsLeft;
		
		if ((System.currentTimeMillis() - gameStartTime)/1000 > winGoal) {
			gameServer.gameState = GameState.GAME_ENDING;
		}
		
		gameWorld.update();
		sendGameWorld();
	}
	
	private long lastGameWorldBroadcastTime = 0;
	/**
	 * Measured in Hz
	 */
	private static final long GAMEWORLD_BROADCAST_FREQUENCY = 10;
	
	private void sendGameWorld() {
		try {
			if (System.currentTimeMillis() - lastGameWorldBroadcastTime > 1000/GAMEWORLD_BROADCAST_FREQUENCY) {
				String serializedWorld = gameWorld.serialize();
				gameServer.broadcastUDP(serializedWorld);
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
			Player[] ranked = rankPlayers(!skropWinCondition.countRects);
			
			String message = "end-game-scores";
			for (Player p : ranked) {
				if (p instanceof SkropPlayer) {
					message += " " + p.name + ":" + ((SkropPlayer) p).score + ":" + ((SkropPlayer) p).rectsDestroyed;
				} else {
					
				}
			}
			
			gameServer.broadcastTCP(message);
			
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
				SkropPlayer current = (SkropPlayer) ranked[i];
				SkropPlayer prev = (SkropPlayer) ranked[i - 1];
				
				if ((sortByPoints && current.score > prev.score) || (!sortByPoints && current.rectsDestroyed > prev.rectsDestroyed)) {
					sorted = false;
					Player temp = ranked[i - 1];
					ranked[i - 1] = ranked[i];
					ranked[i] = temp;
				}
			}
		}
		
		return ranked;
	}
}
