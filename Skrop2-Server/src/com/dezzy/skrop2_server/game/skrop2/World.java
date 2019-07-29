package com.dezzy.skrop2_server.game.skrop2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class World implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3909871252377706303L;
	
	public List<Rectangle> rects = new ArrayList<Rectangle>();
	private final transient int maxRects;
	
	public World(int _maxRects) {
		maxRects = _maxRects;
		
		for (int i = 0; i < maxRects; i++) {
			addRandomRectangle();
		}
	}
	
	public synchronized void update() {
		for (int i = rects.size() - 1; i >= 0; i--) {
			if (rects.get(i).isDead()) {
				rects.remove(i);
				addRandomRectangle();
			} else {
				rects.get(i).grow();
			}
		}
	}
	
	public synchronized int checkClick(float x, float y) {
		for (int i = rects.size() - 1; i >= 0; i--) {
			Rectangle r = rects.get(i);
			float halfSize = r.size / 2.0f;
			
			if (x <= r.x + halfSize && x >= r.x - halfSize && y <= r.y + halfSize && y >= r.y - halfSize) {
				int points = r.size > 0 ? (int)(2*r.maxSize/r.size) : 0;
				
				rects.remove(i);
				addRandomRectangle();
				return points;
			}
		}
		
		return 0;
	}
	
	private synchronized void addRandomRectangle() {
		rects.add(new Rectangle((float)Math.random(), (float)Math.random()));
	}
	
	public synchronized String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
