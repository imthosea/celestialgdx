package me.thosea.celestialgdx.graphics;

import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * An image on the GPU's memory. The texture's buffer is automatically bound after creation.
 * <p>
 * To upload data, use a variant of {@link #upload}.
 * To allocate the memory for the texture without uploading anything
 * (for example to use with a framebuffer), use a variant of {@link #allocate}.
 * Be sure to call {@link #bindBuffer()} before using either of those methods.
 * If you want to upload a non-2D texture or data from somewhere other than a pixmap,
 * use the OpenGL methods directly, then call {@link #setKnownSize(int, int)}.
 * </p>
 * <p>
 * You can set the wrap behavior with
 * {@link #setHorizontalWrap(TextureWrap)} / {@link #setVerticalWrap(TextureWrap)},
 * or {@link #setWrap(TextureWrap)} as a shorthand for calling both.
 * If the behavior is {@link TextureWrap#CLAMP_TO_BORDER}, use {@link #setBorderColor(float, float, float, float)}
 * to set the color.
 * You can set the filter with
 * {@link #setMagnificationFilter(TextureFilter)} / {@link #setMinificationFilter(TextureFilter)}.
 * Make sure to call {@link #bindBuffer()} before.
 * </p>
 * @author thosea
 */
public final class Texture implements Disposable {
	private static int lastHandle = 0;

	private final int handle;
	/** The OpenGL type, like {@link GL33#GL_TEXTURE_2D} or {@link GL33#GL_TEXTURE_3D} */
	public final int glType;

	private int width = -1;
	private int height = -1;

	private boolean disposed = false;

	private Texture(int handle, int glType) {
		this.handle = handle;
		this.glType = glType;
		this.bindBuffer();
	}

	private Texture(int glType) {
		this.handle = glGenTextures();
		this.glType = glType;
		this.bindBuffer();
	}

	public int getHandle() {
		this.requireNotDisposed();
		return handle;
	}

	/**
	 * Bind the buffer. You must call this before modifying the texture.
	 */
	public void bindBuffer() {
		this.requireNotDisposed();
		glBindTexture(this.glType, this.handle);
		lastHandle = this.handle;
	}

	/**
	 * Bind the texture for use in a shader
	 * @param slot the texture slot
	 * (between 0 and the GPUs {@link GL33#GL_MAX_TEXTURE_IMAGE_UNITS}, guaranteed to be minimum 16)
	 */
	public void bindTexture(int slot) {
		if(slot > 31) {
			throw new IllegalArgumentException("texture slot cannot be above 31");
		}
		this.requireNotDisposed();
		glActiveTexture(GL_TEXTURE0 + slot);
		glBindTexture(this.glType, this.handle);
		lastHandle = this.handle;
	}

	private void bindBufferIfNeeded() {
		requireNotDisposed();
		if(lastHandle != this.handle) bindBuffer();
	}

	public int getWidth() {
		if(width == -1) {
			throw new IllegalStateException("No known size for this texture");
		}
		return width;
	}
	public int getHeight() {
		if(height == -1) {
			throw new IllegalStateException("No known size for this texture");
		}
		return height;
	}

	/**
	 * The texture must have its buffer bound before calling this
	 * @param pixmap pixmap to upload
	 */
	public void upload(Pixmap pixmap) {
		upload(pixmap, /*compress*/ false, /*level*/ 0);
	}

	/**
	 * The texture must have its buffer bound before calling this
	 * @param pixmap pixmap to upload
	 * @param compress whether the texture should be compressed on the GPU
	 */
	public void upload(Pixmap pixmap, boolean compress) {
		upload(pixmap, compress, /*level*/ 0);
	}

	/**
	 * The texture must have its buffer bound before calling this
	 * @param pixmap pixmap to upload
	 * @param compress whether the texture should be compressed on the GPU
	 * @param levelOfDetail the mipmap reduction level, 0 if you aren't use mipmaps
	 */
	public void upload(Pixmap pixmap, boolean compress, int levelOfDetail) {
		if(pixmap.format.glType == -1) {
			throw new IllegalArgumentException("Pixmap format " + pixmap.format + " cannot be uploaded to OpenGL. " +
					"The easiest way to fix this is to explicitly specify a format at construction.");
		}
		this.bindBufferIfNeeded();
		glTexImage2D(
				glType,
				levelOfDetail,
				pixmap.format.glType, pixmap.width, pixmap.height,
				/*border*/ 0, compress ? pixmap.format.glCompressedType : pixmap.format.glType,
				GL_UNSIGNED_BYTE,
				pixmap.getBuffer()
		);
		this.width = pixmap.width;
		this.height = pixmap.height;
	}

	/**
	 * Allocates the memory required to store the texture without filling it.
	 * Useful if this is a texture for a framebuffer.
	 * The texture must have its buffer bound before calling this.
	 * @param format pixel format
	 * @param width width
	 * @param height height
	 */
	public void allocate(PixelFormat format, int width, int height) {
		this.allocate(format, width, height, /*compress*/ false);
	}

	/**
	 * Allocates the memory required to store the texture without filling it.
	 * Useful if this is a texture for a framebuffer.
	 * The texture must have its buffer bound before calling this.
	 * @param format pixel format
	 * @param width width
	 * @param height height
	 * @param compress whether the texture should be compressed on the GPU
	 */
	public void allocate(PixelFormat format, int width, int height, boolean compress) {
		this.bindBufferIfNeeded();
		glTexImage2D(
				glType,
				/*level*/ 0,
				format.glType, width, height,
				/*border*/ 0, compress ? format.glCompressedType : format.glType,
				GL_UNSIGNED_BYTE,
				(ByteBuffer) null
		);
	}

	public enum TextureWrap {
		REPEAT(GL_REPEAT),
		MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
		CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
		CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER);

		public final int glType;

		TextureWrap(int glType) {
			this.glType = glType;
		}
	}

	public void setHorizontalWrap(TextureWrap wrap) {
		this.bindBufferIfNeeded();
		glTexParameteri(this.glType, GL_TEXTURE_WRAP_S, wrap.glType);
	}
	public void setVerticalWrap(TextureWrap wrap) {
		this.bindBufferIfNeeded();
		glTexParameteri(this.glType, GL_TEXTURE_WRAP_T, wrap.glType);
	}
	/** Sets both the horizontal and vertical wrap behavior */
	public void setWrap(TextureWrap wrap) {
		this.setHorizontalWrap(wrap);
		this.setVerticalWrap(wrap);
	}
	/** Sets the border color for when the wrap behavior is {@link TextureWrap#CLAMP_TO_BORDER} */
	public void setBorderColor(float r, float g, float b, float a) {
		this.bindBufferIfNeeded();
		glTexParameterfv(this.glType, GL_TEXTURE_BORDER_COLOR, new float[] {r, g, b, a});
	}

	/**
	 * Sets the internal stored size of the texture returned by {@link #getWidth()} / {@link #getHeight()}.
	 * Use this if you modified the texture directly with OpenGL instead of using {@link #upload}.
	 */
	public void setKnownSize(int width, int height) {
		if(width < 0 || height < 0) {
			throw new IllegalArgumentException("Cannot have negative texture size");
		}
		this.width = width;
		this.height = height;
	}

	public void setMinificationFilter(TextureFilter filter) {
		this.bindBufferIfNeeded();
		glTexParameteri(this.glType, GL_TEXTURE_MIN_FILTER, filter.glType);
	}
	public void setMagnificationFilter(TextureFilter filter) {
		this.bindBufferIfNeeded();
		glTexParameteri(this.glType, GL_TEXTURE_MAG_FILTER, filter.glType);
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		glDeleteTextures(this.handle);
		this.disposed = true;
		if(lastHandle == this.handle) lastHandle = 0;
	}

	/**
	 * @return a new texture of type {@link GL33#GL_TEXTURE_2D}
	 */
	public static Texture create2d() {
		return new Texture(GL_TEXTURE_2D);
	}

	/**
	 * @return a new texture of the specified GL type with the existing handle
	 */
	public static Texture create(int glType) {
		return new Texture(glType);
	}

	/**
	 * @return a new texture of the specified GL type with the existing handle
	 */
	public static Texture wrap(int handle, int glType) {
		return new Texture(handle, glType);
	}

	public enum TextureFilter {
		/** Nearest texel */
		NEAREST(GL_NEAREST),
		/** Samples average of the four surrounding texels */
		LINEAR(GL_LINEAR),
		/** Choose the best-fitting mipmap and get the nearest texel */
		NEAREST_MIPMAP_NEAREST(GL_LINEAR_MIPMAP_LINEAR),
		/** Choose the best-fitting mipmap and sample the average of the four surrounding texels */
		LINEAR_MIPMAP_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
		/** Choose the two best-fitting mipmaps and get the nearest texel */
		NEAREST_MIPMAP_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
		/** Choose the two best-fitting mipmaps and sample the average of the four surrounding texels */
		LINEAR_MIPMAP_LINEAR(GL_LINEAR_MIPMAP_LINEAR);

		public final int glType;

		TextureFilter(int glType) {
			this.glType = glType;
		}
	}
}