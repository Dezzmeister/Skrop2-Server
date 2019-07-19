package com.dezzy.skrop2_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dezzy.skrop2_server.game.GameServer;
import com.dezzy.skrop2_server.game.skrop2.SkropGame;
import com.dezzy.skrop2_server.game.skrop2.SkropPlayer;
import com.dezzy.skrop2_server.game.skrop2.SkropWinCondition;

public class Main {
	private static final int EXPECTED_ARGS = 4;
	
	public static final void main(String[] args) throws IOException {
		
		if (args.length != EXPECTED_ARGS) {
			System.err.println("Please enter exactly " + EXPECTED_ARGS + " arguments, see the README for more info!\nStopping...");
			System.exit(-1);
		} else {
			String serverName = args[0];
			int infoServerPort = Integer.parseInt(args[1]);
			int gameServerStartPort = Integer.parseInt(args[2]);
			int gameServerCount = Integer.parseInt(args[3]);
			
			@SuppressWarnings("unused")
			GameServer gameServer = new GameServer("Skrop 2", serverName, infoServerPort, gameServerStartPort, gameServerCount, SkropGame.class, SkropPlayer.class, SkropWinCondition.values());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while (!reader.readLine().equals("stop"));
			System.out.println("Stopping the server...");
			System.exit(0);
		}
	}
}
