package com.example.spacepenguin.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import android.opengl.GLES20;


public class Square {

	private static final int BYTES_PER_FLOAT = 4;

	private Vector<Float> data; 

	private int bufferId;
	private int dataSize;


	private void add(float[] t){
		for(float f : t) data.add(f);
	}


	public Square(){ 
		
		
		data = new Vector<Float>();
		
		// v0  v1
		// v2  v3
		
		
		float[][] v = new float[4][3];
		float[][] t = new float[4][2];
		float[] n = new float[]{0,0,1};

		
		for(int i = 0; i < 4; ++i){
			v[i][0] = ((i & 1) << 1) - 1;
			v[i][1] = (i & 2) - 1;
			v[i][2] = 0;
			
			t[i][0] = (i & 1);
			t[i][1] = (i >> 1) & 1;
		}
		
		
		//square centered on O pointing toward z		
				//   1/4  6
				//
				//   2   3/5
		
		add(v[2]);add(n);add(t[2]);
		add(v[0]);add(n);add(t[0]);
		add(v[3]);add(n);add(t[3]);
		
		add(v[3]);add(n);add(t[3]);
		add(v[0]);add(n);add(t[0]);
		add(v[1]);add(n);add(t[1]);

		dataSize+=2*3;
		load();
	}



	public void load(){

		final FloatBuffer cubeBuffer = ByteBuffer.allocateDirect(data.size() * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();											

		for(float f : data){
			cubeBuffer.put(f);
		}

		cubeBuffer.position(0);

		final int buffers[] = new int[1];
		GLES20.glGenBuffers(1, buffers, 0);						

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeBuffer.capacity() * BYTES_PER_FLOAT, cubeBuffer, GLES20.GL_STATIC_DRAW);			

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		bufferId = buffers[0];			

		cubeBuffer.limit(0);

		data.clear();
		data = null;
	}

	public void render(int mPositionHandle, int mNormalHandle, int mTextureCoordinateHandle) {	    
		final int stride = (3 + 3 + 2) * BYTES_PER_FLOAT;

		// Pass in the position information
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, stride, 0);

		// Pass in the normal information
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, stride, 3 * BYTES_PER_FLOAT);

		// Pass in the texture information
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,
				stride, (3 + 3) * BYTES_PER_FLOAT);

		// Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		// Draw the cubes.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, dataSize);
	}


	public void release() {
		// Delete buffers from OpenGL's memory
		final int[] buffersToDelete = new int[] { bufferId };
		GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
	}

}



