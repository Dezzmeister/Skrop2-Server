package com.dezzy.skrop2_server;

import com.dezzy.skrop2_server.game.skrop2.SkropGame;
import com.dezzy.skrop2_server.game.skrop2.SkropPlayer;
import com.dezzy.skrop2_server.game.skrop2.SkropWinCondition;

public class Main {
	private static final int EXPECTED_ARGS = 2;
	
	public static final void main(String[] args) {
		try {			
			Game game = new Game("Skrop 2", "Raoul", 30200, 30500, 5, SkropGame.class, SkropPlayer.class, SkropWinCondition.values());
			while (true);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (args.length != EXPECTED_ARGS) {
			System.err.println("Please enter exactly " + EXPECTED_ARGS + " arguments, see the README for more info!\nStopping...");
			System.exit(-1);
		} else {
			String serverName = args[0];
			int infoServerPort = Integer.parseInt(args[1]);
			int gameServerStartPort = Integer.parseInt(args[2]);
			int gameServerCount = Integer.parseInt(args[3]);
		}
	}
}
