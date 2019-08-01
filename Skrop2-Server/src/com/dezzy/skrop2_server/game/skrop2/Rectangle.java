package com.dezzy.skrop2_server.game.skrop2;

import java.io.Serializable;

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
	private final transient float growthFactor;
	final transient float maxSize;
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
