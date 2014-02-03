package com.example.spacepenguin.renderer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.spacepenguin.R;
import com.example.spacepenguin.R.drawable;
import com.example.spacepenguin.R.raw;
import com.example.spacepenguin.element.Asteroid;
import com.example.spacepenguin.element.Penguin;
import com.example.spacepenguin.element.Universe;
import com.example.spacepenguin.util.RawResourceReader;
import com.example.spacepenguin.util.ShaderHelper;
import com.example.spacepenguin.util.TextureHelper;




import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 */
public class GameRenderer implements GLSurfaceView.Renderer {

	private final Activity activity;
	private final GLSurfaceView surfaceView;


	private float[] modelMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] mvpMatrix = new float[16];
	private float[] mvMatrix = new float[16];


//	private float[] accumulatedRotation = new float[16];
	private float[] tmpMatrix = new float[16];


	private int mMVPMatrixHandle; //Model view projection
	private int mMVMatrixHandle;  //Model view
	private int mLightPosHandle;
	private int mTextureUniformHandle;
	private int mPositionHandle;
	private int mNormalHandle;
	private int mTextureCoordinateHandle;
	private int mProgramHandle;
	private int asteroidTextureHandle;		

	static final int POSITION_DATA_SIZE = 3;	
	static final int NORMAL_DATA_SIZE = 3;
	static final int TEXTURE_COORDINATE_DATA_SIZE = 2;

	static final int BYTES_PER_FLOAT = 4;


	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];

	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];


	// These still work without volatile, but refreshes are not guaranteed to happen.					
	public volatile float mDeltaX;					
	public volatile float mDeltaY;	


	private Square square;

	private float[] mVMMatrix = new float[16];
	public float[] mPVMMatrix = new float[16];
	public float[] mInvPVMMatrix = new float[16];
	public float[] mInvProjectionMatrix = new float[16];

	public float left;
	public float right;
	public float bottom;
	public float top;

	public volatile float time = 0f;

	private float ratio;

	private Universe universe;
	private float x,y;
	private float z = -10;
	
	static class RefreshHandler extends Handler {
		WeakReference<GameRenderer> weak;
		
		RefreshHandler(GameRenderer gameRenderer){
			weak = new WeakReference<GameRenderer>(gameRenderer);
		}

		@Override
		public void handleMessage(Message msg) {
			if (weak.get() == null) return;
			weak.get().update();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	
	RefreshHandler handler = new RefreshHandler(this);
	private Penguin penguin;
	private int pinguinTextureHandle;
	private boolean playing;
	public static final int DELAY = 30; // ms
	

	public GameRenderer(final Activity activity, final GLSurfaceView glSurfaceView, final Universe universe) {
		this.activity = activity;	
		surfaceView = glSurfaceView;
		this.universe = universe;
		try {
			this.penguin = new Penguin(activity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		x = 0;
		y = 0;
	}
	
	public void move (float dx, float dy){
//		Log.d("move",x+" "+y);
		x += 8*dx;
		y += 8*dy;
		
		if (x< -1) x = -1;
		if (x> 1)  x = 1;
		
		if (y< -1) y = -1;
		if (y> 1)  y = 1;

	}


	public void update() {
		if (playing){
			handler.sleep(DELAY);
			move();
			time += 1;
			
			if (time>50 && universe.collision(x,y)){
				playing = false;
			}
			
			surfaceView.requestRender();
		}
	}
	
	public void start(){
		playing = true;
		time = 0;
		update();
		
		Log.d("start","start");
	}
	
	public synchronized void move(){
		universe.move(1f);
	}




	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{		
		square = new Square();
		penguin.load();
		start();

//		generateCubes();			

		// Set the background clear color to black.
		GLES20.glClearColor(0f, 0f, 0f, 0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);						

		// Position the eye in front of the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = -0.0f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		

		final String vertexShader = RawResourceReader.readTextFileFromRawResource(activity, R.raw.vertex_shader);   		
		final String fragmentShader = RawResourceReader.readTextFileFromRawResource(activity, R.raw.fragment_shader);

		final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		

		mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position",  "a_Normal", "a_TexCoordinate"});		            


		// Load  textures
		asteroidTextureHandle = setTexture(R.drawable.asteroid, 1); 
		pinguinTextureHandle = setTexture(R.drawable.penguin_tex, 1); 



	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		float c = 0.15f;
		ratio = (float) width / height;
		if (ratio<1) c/=ratio;

		left = -ratio*c;
		right = ratio*c;
		bottom = -1.0f*c;
		top = 1.0f*c;
		final float near = 1f;
		final float far = Asteroid.MAX_DIST;

		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
		Matrix.invertM(mInvProjectionMatrix , 0, projectionMatrix, 0);
	}	

	@Override
	public synchronized void onDrawFrame(GL10 glUnused) 
	{		
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);	
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glDepthMask(true);

		GLES20.glUseProgram(mProgramHandle);


		// Set program handles for cube drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix"); 
		mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");        
		mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal"); 
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");


		// Translate the scene into the screen.
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, -x*0.5f, -y*0.5f, 5f);     


		Matrix.multiplyMM(mVMMatrix, 0, viewMatrix, 0, modelMatrix, 0);
//		Matrix.invertM(mInvPVMMatrix, 0, mVMMatrix, 0);




		// Pass in the light position in eye space.
		GLES20.glUniform3f(mLightPosHandle, -2,0,-1);

		// Pass in the texture information
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, asteroidTextureHandle);

		// Tell the texture uniform sampler to use this texture in the
		// shader by binding to texture unit 0.
		GLES20.glUniform1i(mTextureUniformHandle, 0);

		
		float[] matrix = new float[16];
		for(Asteroid asteroid : universe.getVector()){
			Matrix.setIdentityM(matrix, 0);
			float[] t = asteroid.getCoord();
			Matrix.translateM(matrix, 0, t[0], t[1], -t[2]);
			float s = asteroid.getSize();
			Matrix.scaleM(matrix, 0, s, s, s);
			
			Matrix.rotateM(matrix, 0, time/(2*s), 0, 0, 1);
			
			Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
			Matrix.multiplyMM(tmpMatrix, 0, mvpMatrix, 0, matrix, 0);
			System.arraycopy(tmpMatrix, 0, mvpMatrix, 0, 16);
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvpMatrix, 0);
			
			Matrix.multiplyMM(tmpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
			System.arraycopy(tmpMatrix, 0, mvpMatrix, 0, 16);
			
			// Pass in the combined matrix.
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
			
			
			
			square.render(mPositionHandle, mNormalHandle, mTextureCoordinateHandle);
		}
		
		// Pass in the texture information
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, pinguinTextureHandle);

		// Tell the texture uniform sampler to use this texture in the
		// shader by binding to texture unit 0.
		GLES20.glUniform1i(mTextureUniformHandle, 0);
		
		Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.translateM(mvpMatrix, 0, x, y, z );
        Matrix.scaleM(mvpMatrix, 0, .10f, 0.10f, 0.10f);
        if ((((int)time/180) % 4) == 3){
        	Matrix.rotateM(mvpMatrix, 0, time*2, -1, 0, 0);
        } else {
        	Matrix.rotateM(mvpMatrix, 0, time*2, 0, 0, 1);
        }
		Matrix.rotateM(mvpMatrix, 0, -90, 1, 0, 0);

		
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvpMatrix, 0);
		
		Matrix.multiplyMM(tmpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
		System.arraycopy(tmpMatrix, 0, mvpMatrix, 0, 16);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		penguin.render(mPositionHandle, mNormalHandle, mTextureCoordinateHandle);
		
	}




	public int setTexture(int id, int scale) {
		int handle = TextureHelper.loadTexture(activity, id,scale);		

		if (handle == 0) return handle;

		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);			

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);		

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);	
		
		return handle;
	}


	public Bundle getBundle() {
		Bundle bundle = new Bundle();
//		bundle.putFloatArray("rotation", accumulatedRotation);
		return bundle;
	}
	
	public void setBundle(Bundle bundle){
//		accumulatedRotation = bundle.getFloatArray("rotation");
	}
}
