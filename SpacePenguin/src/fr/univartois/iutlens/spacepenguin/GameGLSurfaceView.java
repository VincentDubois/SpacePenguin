package fr.univartois.iutlens.spacepenguin;

import fr.univartois.iutlens.spacepenguin.renderer.GameRenderer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class GameGLSurfaceView extends GLSurfaceView {	

	private GameRenderer renderer;
	private float previousX;
	private float previousY;


	public GameGLSurfaceView(Context context) {
		super(context);
	}

	public GameGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float x = event.getX()*(renderer.right-renderer.left)/this.getWidth()+renderer.left;
		float y = event.getY()*(renderer.bottom-renderer.top)/this.getHeight()+renderer.top;

		if (event.getAction()== MotionEvent.ACTION_DOWN){
			previousX = x;
			previousY = y;

		} else if (event.getAction() == MotionEvent.ACTION_MOVE){
			renderer.move(x-previousX, y-previousY);
			previousX = x;
			previousY = y;
			return true;
		}
		else if (event.getAction() == MotionEvent.ACTION_UP){
			return true;
		}

		return true;

	}

	public void setRenderer(GameRenderer renderer) {
		this.renderer = renderer;
		super.setRenderer(renderer);
	}
	

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable("super"));
		renderer.setBundle(bundle.getBundle("state"));

	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable result = super.onSaveInstanceState();
		Bundle bundle  = new Bundle();
		bundle.putParcelable("super", result);
		bundle.putBundle("state", renderer.getBundle());
		return bundle;
	}

	public void start() {
		renderer.start();
	}
	





}
