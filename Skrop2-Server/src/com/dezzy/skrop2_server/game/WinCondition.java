package com.dezzy.skrop2_server.game;

public enum WinCondition {
	FIRST_TO_X_POINTS(false),
	FIRST_TO_X_RECTS(true),
	TIMER_RECTS(true),
	TIMER_POINTS(false);
	
	public final boolean countRects;
	
	private WinCondition(boolean _countRects) {
		countRects = _countRects;
	}
}
