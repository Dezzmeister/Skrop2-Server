package com.dezzy.skrop2_server.game.skrop2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

public class Rectangle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5642753329858256825L;
	
	/**
	 * Normalized coordinates, so that the same game can be displayed easily on different devices
	 */
	float x;
	float y;
	float size = 0;
	final int color;
	private final float growthFactor;
	final float maxSize;
	private transient boolean growing = true;
	private transient boolean dead = false;
	
	public Rectangle(float _x, float _y) {
		x = _x;
		y = _y;
		growthFactor = (float) (0.001 * Math.random() + 0.004f);
		maxSize = (float) (0.2 * Math.random() + 0.2f);
		
		int red = (int)(Math.random() * 256);
		int green = (int)(Math.random() * 256);
		int blue = (int)(Math.random() * 256);
		
		color = (red << 16) | (green << 8) | blue;
	}
	
	public String encode() {
		return x + ":" + y + ":" + color + ":" + growthFactor + ":" + maxSize;
	}
	
	public static Rectangle decode(final String encoded) {
		String[] fields = encoded.split(":");
		float x = Float.parseFloat(fields[0]);
		float y = Float.parseFloat(fields[1]);
		int color = Integer.parseInt(fields[2]);
		float growthFactor = Float.parseFloat(fields[3]);
		float maxSize = Float.parseFloat(fields[4]);
		
		return new Rectangle(x, y, 0, color, growthFactor, maxSize, true, false);
	}
	
	private Rectangle(float _x, float _y, float _size, int _color, float _growthFactor, float _maxSize, boolean _growing, boolean _dead) {
		x = _x;
		y = _y;
		size = _size;
		color = _color;
		growthFactor = _growthFactor;
		maxSize = _maxSize;
		growing = _growing;
		dead = _dead;
	}
	
	public static Rectangle createIdentifier(float x, float y, int color) {
		return new Rectangle(x, y, 0, color, 0, 0, false, true);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		
		if (!(object instanceof Rectangle)) return false;
			
		Rectangle other = (Rectangle) object;
		return x == other.x && y == other.y && size == other.size && color == other.color;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, size, color);
	}
	
	public Rectangle copy() {
		return new Rectangle(x, y, size, color, growthFactor, maxSize, growing, dead);
	}
	
	public void grow() {
		if (!dead) {
			if (growing) {
				if (size + growthFactor >= maxSize) {
					size = maxSize;
					growing = false;
				} else {
					size += growthFactor;
				}
			} else {
				if (size - growthFactor <= 0) {
					size = 0;
					dead = true;
				} else {
					size -= growthFactor;
				}
			}
		}
	}
	
	public synchronized String serialize() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	
	public float x() {
		return x;
	}
	
	public float y() {
		return y;
	}
	
	public float size() {
		return size;
	}
	
	public int color() {
		return color;
	}
	
	public boolean isDead() {
		return dead;
	}
}
