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
	
	/**
	 * The time at which this game world was the latest game world
	 */
	public int timeFrame = 0;
	
	public World(int _maxRects) {
		maxRects = _maxRects;
		
		for (int i = 0; i < maxRects; i++) {
			addRandomRectangle();
		}
	}
	
	public synchronized ScoreInfo[] update() {
		List<ScoreInfo> out = new ArrayList<ScoreInfo>();
		
		for (int i = rects.size() - 1; i >= 0; i--) {
			if (rects.get(i).isDead()) {
				Rectangle destroyed = rects.remove(i);
				Rectangle added = addRandomRectangle();
				out.add(new ScoreInfo(destroyed, added, 0));
			} else {
				rects.get(i).grow();
			}
		}
		
		return out.toArray(new ScoreInfo[out.size()]);
	}
	
	public synchronized ScoreInfo checkClick(float x, float y) {
		for (int i = rects.size() - 1; i >= 0; i--) {
			Rectangle r = rects.get(i);
			float halfSize = r.size / 2.0f;
			
			if (x <= r.x + halfSize && x >= r.x - halfSize && y <= r.y + halfSize && y >= r.y - halfSize) {
				int points = r.size > 0 ? (int)(2*r.maxSize/r.size) : 0;
				
				Rectangle destroyed = rects.remove(i);
				Rectangle added = addRandomRectangle();
				return new ScoreInfo(destroyed, added, points);
			}
		}
		
		return new ScoreInfo(null, null, 0);
	}
	
	/**
	 * Removes the rectangle, if it exists in this frame of the world. The rectangle may not exist because it has not
	 * been created yet, or it was already destroyed.
	 * 
	 * @param rect Rectangle to be removed
	 * @param replace true if the rectangle should be replaced by a random rectangle, false if it should not be replaced
	 * @return true if this rectangle existed and was removed
	 */
	public synchronized boolean removeRectangleIfExists(final Rectangle rect, boolean replace) {		
		float x = rect.x;
		float y = rect.y;
		int color = rect.color;
		
		for (int i = rects.size() - 1; i >= 0; i--) {
			Rectangle r = rects.get(i);
			if (r.x == x && r.y == y && r.color == color) {
				rects.remove(i);
				
				if (replace) {
					addRandomRectangle();
				}
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Produces a deep copy of this game world.
	 * 
	 * @return a deep copy of the game world
	 */
	synchronized World copy() {
		World out = new World(maxRects);
		out.timeFrame = timeFrame;
		rects.forEach(r -> out.rects.add(r.copy()));
		
		return out;
	}
	
	private synchronized Rectangle addRandomRectangle() {
		Rectangle out = new Rectangle((float)Math.random(), (float)Math.random());
		rects.add(out);
		return out;
	}
	
	public synchronized String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
