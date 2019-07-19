package com.dezzy.skrop2_server.server;

/**
 * This interface is intended to be implemented by enums that specify several different win conditions.
 * 
 * @author Dezzmeister
 *
 */
public interface WinCondition {
	
	/**
	 * Get the name of the win condition ({@link Enum#name()}).
	 * 
	 * @return the name of this WinCondition
	 */
	public String getName();
	
	/**
	 * Get a description of the win condition, based on the argument. For example:<br>
	 * Example info string: <code>"the winner is whoever has the most points after 120 seconds"</code><br>
	 * In this case, <code>winConditionArg</code> was <code>"120"</code>
	 * 
	 * @param winConditionArg the win condition argument (120 seconds in the example)
	 * @return a string describing the win condition in the format above ("the winner is...")
	 */
	public String getInfoString(final String winConditionArg);
}
