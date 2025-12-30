package me.thosea.celestialgdx.graphics.mesh;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;

/**
 * The expected way a VBO or EBO will be used
 */
public enum BufferUsage {
	/**
	 * The data store contents will be modified once and used at most a few times
	 */
	STREAM(GL_STREAM_DRAW),
	/**
	 * The data store contents will be modified once and used many times
	 */
	STATIC(GL_STATIC_DRAW),
	/**
	 * The data store contents will be modified repeatedly and used many times
	 */
	DYNAMIC(GL_DYNAMIC_DRAW);

	public final int glType;
	BufferUsage(int usage) {
		this.glType = usage;
	}
}