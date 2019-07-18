package com.dezzy.skrop2_server.game.skrop2;

import com.dezzy.skrop2_server.game.WinCondition;

public enum SkropWinCondition implements WinCondition {
	FIRST_TO_X_POINTS(false, "the winner is the first to X points"),
	FIRST_TO_X_RECTS(true, "the winner is the first to X rectangles destroyed"),
	TIMER_RECTS(true, "the winner is whoever has destroyed the most rectangles after X seconds"),
	TIMER_POINTS(false, "the winner is whoever has the most points after X seconds");
	
	public final boolean countRects;
	private final String infoString;
	
	private SkropWinCondition(boolean _countRects, final String _infoString) {
		countRects = _countRects;
		infoString = _infoString;
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getInfoString(final String winConditionArg) {
		int winGoal = Integer.parseInt(winConditionArg);
		
		return infoString.substring(0, infoString.indexOf("X")) + winGoal + infoString.substring(infoString.indexOf("X") + 1);
	}
}
