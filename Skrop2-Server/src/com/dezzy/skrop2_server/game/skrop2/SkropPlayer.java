package com.dezzy.skrop2_server.game.skrop2;

import com.dezzy.skrop2_server.server.Player;

public class SkropPlayer extends Player {
	public int score = 0;
	public int rectsDestroyed = 0;
	
	public SkropPlayer(final String _name) {
		super(_name);
	}
	
	@Override
	public void reset() {
		score = 0;
		rectsDestroyed = 0;
	}
}
