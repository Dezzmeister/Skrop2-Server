package com.dezzy.skrop2_server;

public class Main {
	private static final int EXPECTED_ARGS = 2;
	
	public static final void main(String[] args) {
		try {			
			Game game = new Game("Raoul", 30200, 30500, 5);
			while (true);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (args.length != EXPECTED_ARGS) {
			System.err.println("Please enter exactly " + EXPECTED_ARGS + " arguments, see the README for more info!");
			error();
		} else {
			int port1 = Integer.parseInt(args[0]);
			int port2 = Integer.parseInt(args[1]);
			
			openTCP(port1, port2);
		}
	}
	
	private static final void openTCP(int port1, int port2) {
		System.out.println("Opening TCP sockets on ports " + port1 + " and " + port2);
	}
	
	private static final void error() {
		System.err.println("Stopping...");
		System.exit(-1);
	}
}
