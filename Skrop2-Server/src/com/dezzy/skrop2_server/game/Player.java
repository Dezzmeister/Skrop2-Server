package com.dezzy.skrop2_server.game;

public class Player {
	public final String name;
	public int score = (int)(Math.random() * 50);
	public int rectsDestroyed = (int)(Math.random() * 100);
	
	public Player(final String _name) {
		name = _name;
	}
	
	public void reset() {
		score = 0;
		rectsDestroyed = 0;
	}
}
