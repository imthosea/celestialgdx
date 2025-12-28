package me.thosea.celestialgdx.image;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL13.GL_COMPRESSED_RGB;
import static org.lwjgl.opengl.GL13.GL_COMPRESSED_RGBA;

public enum PixelFormat {
	/**
	 * One component per pixel for the gray value.
	 * This cannot be uploaded to the GPU.
	 */
	GRAY(1, -1, -1),
	/**
	 * Two components per pixel for the gray and alpha values.
	 * This cannot be uploaded to the GPU.
	 */
	GRAY_ALPHA(2, -1, -1),
	/**
	 * Three components per pixel for the red, green and blue values.
	 * This can be uploaded to the GPU.
	 */
	RGB(3, GL_RGB, GL_COMPRESSED_RGB),
	/**
	 * Four components per pixel for the red, green, blue and alpha values.
	 * This can be uploaded to the GPU.
	 */
	RGBA(4, GL_RGBA, GL_COMPRESSED_RGBA);

	/**
	 * The component count per pixel
	 */
	public final int components;

	/**
	 * The OpenGL format code. -1 if this format cannot be uploaded.
	 */
	public final int glType;

	/**
	 * The OpenGL compressed variant. -1 if this format cannot be uploaded.
	 */
	public final int glCompressedType;

	PixelFormat(int components, int glType, int glCompressedType) {
		this.components = components;
		this.glType = glType;
		this.glCompressedType = glCompressedType;
	}

	public static PixelFormat byComponentCount(int components) {
		return switch(components) {
			case 1 -> GRAY;
			case 2 -> GRAY_ALPHA;
			case 3 -> RGB;
			case 4 -> RGBA;
			default -> throw new IllegalArgumentException("No format for " + components + " components");
		};
	}
}