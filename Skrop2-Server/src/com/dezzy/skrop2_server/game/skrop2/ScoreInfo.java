package com.dezzy.skrop2_server.game.skrop2;

public class ScoreInfo {
	public final Rectangle destroyed;
	public final Rectangle added;
	public final int points;
	
	public ScoreInfo(final Rectangle _destroyed, final Rectangle _added, int _points) {
		destroyed = _destroyed;
		added = _added;
		points = _points;
	}
}
