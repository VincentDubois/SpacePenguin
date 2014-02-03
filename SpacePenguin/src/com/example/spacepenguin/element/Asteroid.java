package com.example.spacepenguin.element;

public class Asteroid {
	
	
	
	public static final int MIN_DIST = 0;
	public static final int MAX_DIST = 200;
	private static final float MIN_SIZE = 0.12f;
	private static final float MAX_SIZE = 0.9f;
	
	private float[] coord;
	private float size;
	
	public float[] getCoord() {
		return coord;
	}
	
	public float getSize() {
		return size;
	}

	public Asteroid(boolean far){
		coord = new float[3];
		
		coord[0] = 5*((float) Math.random()*2f-1f);
		coord[1] = 5*((float) Math.random()*2f-1f);
		coord[2] = (far ? MAX_DIST : ((float)(MIN_DIST+(MAX_DIST-MIN_DIST)*Math.random())));
		
		size = (float)(MIN_SIZE+(MAX_SIZE-MIN_SIZE)*Math.random());
	}
	
	public boolean move(float d){
		coord[2] -= d;
		if (coord[2]< MIN_DIST){
			coord[2] += MAX_DIST-MIN_DIST;
			return true;
		}
		return false;
	}

	public boolean collision(float x, float y) {
		return (coord[2]<10+size+MIN_DIST && coord[2]>10-size-MIN_DIST) && dist2(coord[0]-x,coord[1]-y) < (size+MIN_SIZE)*(size+MIN_SIZE);
	}

	private float dist2(float dx, float dy) {
		return dx*dx+dy*dy;
	}

}
