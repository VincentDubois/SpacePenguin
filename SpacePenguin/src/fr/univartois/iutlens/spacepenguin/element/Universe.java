package fr.univartois.iutlens.spacepenguin.element;

import java.util.Vector;

import android.util.Log;

public class Universe {
	
	
	private Vector<Asteroid> vector;
	
	
	public Universe (int size){
		vector = new Vector<Asteroid>();		
		
		for(int i = 0; i < size; ++i){
			getVector().add(new Asteroid(false));
		}
	}
	
	public Universe() {
		this(120);
	}

	public  void move(float d){
		for(Asteroid a : vector){
			a.move(d);
		}
	}

	public  Vector<Asteroid> getVector() {
		return vector;
	}

	public boolean collision(float x, float y) {
		for(Asteroid a : vector)
			if (a.collision(x,y)) return true;
		
		return false;
	}
}
