package fr.univartois.iutlens.spacepenguin.element;



import java.io.IOException;

import android.content.Context;
import fr.univartois.iutlens.spacepenguin.renderer.VBO;

public class Penguin extends VBO {
	

	public Penguin(Context context) throws IOException {
		super(context, "pin_v2_tex.obj");
	}

}
