package com.dezzy.skrop2_server.game;

public abstract class Player {
	public final String name;
	
	public Player(final String _name) {
		name = _name;
	}
	
	public abstract void reset();
}
