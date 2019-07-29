package com.dezzy.skrop2_server.server;

/**
 * A player in a {@link LocalGame}.
 * 
 * @author Dezzmeister
 *
 */
public abstract class Player {
	public final String name;
	public final int color;
	
	public Player(final String _name, int _color) {
		name = _name;
		color = _color;
	}
	
	public abstract void reset();
	
	/**
	 * True if this Player's color is similar to another color. Colors are 24-bit RGB.
	 * 
	 * @param otherColor color to compare against
	 * @return true if this color is similar to <code>otherColor</code>
	 */
	boolean hasSimilarColorTo(int otherColor) {
		int blue = color & 0xFF;
		int green = (color >>> 8) & 0xFF;
		int red = (color >>> 16) & 0xFF;
		
		int otherBlue = otherColor & 0xFF;
		int otherGreen = (otherColor >>> 8) & 0xFF;
		int otherRed = (otherColor >>> 16) & 0xFF;
		
		int threshold = 30;
		
		return Math.abs(blue - otherBlue) + Math.abs(green - otherGreen) + Math.abs(red - otherRed) < threshold;
	}
}
