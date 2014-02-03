package com.example.spacepenguin.renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import android.content.Context;
import android.opengl.GLES20;

public class VBO {
	private static final int BYTES_PER_FLOAT = 4;
	
	private Vector<Float> vertice,texture,normal;
	Vector<Float> buffer;

	private int bufferId = -1;
	private int dataSize = 0;
	
	
	public VBO(Context context, String filename) throws IOException{
		InputStream stream=context.getAssets().open(filename);
		BufferedReader in= new BufferedReader(new InputStreamReader(stream));
		String str;
		
		vertice = new Vector<Float>();
		texture = new Vector<Float>();
		normal = new Vector<Float>();
		buffer = new Vector<Float>();


		while ((str=in.readLine()) != null) {
			if (!str.startsWith("#")) parse(str);
		}

		in.close();
		
		vertice = null;
		texture = null;
		normal = null;
	}
	
	public void load(){
		final FloatBuffer vbo = ByteBuffer.allocateDirect(buffer.size() * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();											

		for(float f : buffer){
			vbo.put(f);
		}

		vbo.position(0);

		final int buffers[] = new int[1];
		GLES20.glGenBuffers(1, buffers, 0);						

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vbo.capacity() * BYTES_PER_FLOAT, vbo, GLES20.GL_STATIC_DRAW);			

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		bufferId = buffers[0];			

		vbo.limit(0);

		buffer.clear();
		buffer = null;

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


	private void parse(String str) {
		String[] s = str.split("\\s+");
		if (s.length<2) return;
		if("v".equals(s[0])) addTo(vertice,s,3);
		else if("vt".equals(s[0])) addTo(texture,s,2);
		else if("vn".equals(s[0])) addTo(normal,s,3);
		else if("f".equals(s[0])) addBuffer(s);
	}

	Vector<Integer> vStack = new Vector<Integer>();
	Vector<Integer> tStack = new Vector<Integer>();
	Vector<Integer> nStack = new Vector<Integer>();
	
	float[] norm = new float[3];
	float[] tex = {0,0};

	
	private void addBuffer(String[] s) {
		vStack.clear();
		tStack.clear();
		nStack.clear();
		
		for(int i = 1; i < s.length; ++i){
			String[] v = s[i].split("/");
			vStack.add(Integer.decode(v[0]));
			if(v.length>1 && v[1] != null && v[1].length()!= 0)
				tStack.add(Integer.decode(v[1]));
			if(v.length>2 && v[2] != null && v[2].length()!= 0)
				nStack.add(Integer.decode(v[2]));
		}
		
		if (nStack.isEmpty()) computeNormal();
		
		for(int i = 0; i < vStack.size(); ++i){			
			addBufferVertex(vStack.get(i));
			if (nStack.isEmpty()) addArray(norm);
			else addBufferNormal(nStack.get(i));
			if (tStack.isEmpty()) addArray(tex);
			else addBufferTexture(tStack.get(i));
			dataSize+=1;
		}	
	}

	private void addArray(float[] array) {
		for(float f : array) buffer.add(f);
	}

	float[] u = new float[3];
	float[] v = new float[3];

	
	private void computeNormal() {
		diff(vStack.get(0),vStack.get(1),u);
		diff(vStack.get(0),vStack.get(2),v);
		float n =0;
		for(int i = 0; i < 3 ; ++i){
			norm[i] = u[(i+1)%3]*v[(i+2)%3]-u[(i+2)%3]*v[(i+1)%3];
			n += norm[i]*norm[i];
		}
		n = (float) Math.sqrt(n);
		for(int i = 0; i<3; ++i){
			norm[i] /= n;
		}
	}

	private void diff(int a, int b, float[] result) {
		for(int i = 0; i < 3; ++i){
			result[i] = vertice.get(a*3+i-3)-vertice.get(b*3+i-3);
		}
	}

	private void addBufferVertex(Integer ndx) {
		for(int i =ndx*3-3; i < ndx*3; ++i){
			buffer.add(vertice.get(i));
		}
	}
	
	private void addBufferTexture(Integer ndx) {
		for(int i =ndx*2-2; i < ndx*2; ++i){
			buffer.add(texture.get(i));
		}
	}

	private void addBufferNormal(Integer ndx) {
		for(int i =ndx*3-3; i < ndx*3; ++i){
			buffer.add(normal.get(i));
		}
	}


	private void addTo(Vector<Float> vector, String[] s, int nb) {
		for(int i = 1; i <=nb;++i) vector.add(Float.valueOf(s[i]));		
	}
}
