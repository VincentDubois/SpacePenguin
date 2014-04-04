package fr.univartois.iutlens.spacepenguin;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
//import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import fr.univartois.iutlens.spacepenguin.element.Universe;
import fr.univartois.iutlens.spacepenguin.gameutils.BaseGameActivity;
import fr.univartois.iutlens.spacepenguin.renderer.GameRenderer;

public class MainActivity extends BaseGameActivity implements OnClickListener {

	private GameGLSurfaceView surfaceView;
	private View gameover;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);  

		surfaceView = (GameGLSurfaceView) findViewById(R.id.gameGLSurfaceView1);



		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		Universe universe = new Universe();

		

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			surfaceView.setEGLContextClientVersion(2);

			final GameRenderer mRenderer = new GameRenderer(this, surfaceView,universe);

			TextView score = (TextView) findViewById(R.id.score);
			mRenderer.setScore(score);

			gameover = (View) findViewById(R.id.layoutgameover);
			mRenderer.setGameover(gameover);
			gameover.setVisibility(View.GONE);


			surfaceView.setRenderer(mRenderer);
			surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		} else {
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void start(View view){
		surfaceView.start();
		gameover.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button) {
			// start the asynchronous sign in flow
			beginUserInitiatedSignIn();
		}
		else if (view.getId() == R.id.sign_out_button) {
			// sign out.
			signOut();

			// show sign-in button, hide the sign-out button
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
		}
	}


	@Override
	public void onSignInSucceeded() {
		// show sign-out button, hide the sign-in button
		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

		// (your code here: update UI, enable functionality that depends on sign in, etc)
	}

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}	

}
