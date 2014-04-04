package fr.univartois.iutlens.spacepenguin.element;



import java.io.IOException;
import fr.univartois.iutlens.spacepenguin.renderer.VBO;
import android.content.Context;



public class Penguin extends VBO {
	
	static String[] tablepin = { "pin_f1.obj" , "pin_f2.obj" , "pin_f3.obj" , "pin_f4.obj" };
//String[] tablepin=new String[]{ "pin_f1.obj" , "pin_f2.obj" , "pin_f3.obj" , "pin_f4.obj" };

	public Penguin(Context context, int i) throws IOException {
		super(context, tablepin[i]);
	}

}
