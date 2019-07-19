package com.dezzy.skrop2_server.game;

/**
 * General game states. The server is in one of these states at any given time.
 * 
 * @author Dezzmeister
 *
 */
public enum GameState {
	
	/**
	 * The server is not running a game
	 */
	NO_GAME,
	
	/**
	 * The server is waiting for all players to join
	 */
	WAITING_FOR_PLAYERS,
	
	/**
	 * A new match is beginning
	 */
	BEGINNING,
	
	/**
	 * A match is in progress
	 */
	IN_GAME,
	
	/**
	 * The match has ended, but the server is still running the game and a new match may begin
	 */
	GAME_ENDING,
	
	/**
	 * The server is stopping the game and the players will be disconnected
	 */
	GAME_CLOSING
}
