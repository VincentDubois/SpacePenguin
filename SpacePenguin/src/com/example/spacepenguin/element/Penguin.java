package com.example.spacepenguin.element;

import java.io.IOException;

import com.example.spacepenguin.renderer.VBO;

import android.content.Context;

public class Penguin extends VBO {
	

	public Penguin(Context context) throws IOException {
		super(context, "pin_v2.obj");
	}

}
