package me.thosea.celestialgdx.graphics;

import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL30.*;

/**
 * A buffer optimized for being a render target for {@link Framebuffer}s.
 * Cannot directly be sampled from.
 * @author thosea
 * @see Framebuffer
 * @see <a href="https://wikis.khronos.org/opengl/Renderbuffer">Renderbuffer - OpenGL wiki</a>
 */
public final class Renderbuffer implements Disposable {
	private static int lastHandle = 0;

	private final int handle;
	private boolean disposed = false;

	private Renderbuffer(int handle) {
		this.handle = handle;
		this.bind();
	}

	public int getHandle() {
		this.requireNotDisposed();
		return handle;
	}

	public void bind() {
		this.requireNotDisposed();
		glBindRenderbuffer(GL_RENDERBUFFER, this.handle);
		lastHandle = this.handle;
	}

	/**
	 * The buffer must be bound before calling this.
	 * For supported formats, see
	 * <a href="https://wikis.khronos.org/opengl/Image_Format">Image Format - OpenGL wiki</a>.
	 * @param format the OpenGL format, like {@link GL33#GL_STENCIL_INDEX8} or {@link GL33#GL_DEPTH24_STENCIL8}
	 * @param width the width
	 * @param height the height
	 */
	public void allocate(int format, int width, int height) {
		this.requireNotDisposed();
		if(lastHandle != this.handle) {
			throw new IllegalStateException("the buffer is not bound");
		}
		glRenderbufferStorage(GL_RENDERBUFFER, format, width, height);
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		glDeleteFramebuffers(this.handle);
		this.disposed = true;
		if(lastHandle == this.handle) lastHandle = 0;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static Renderbuffer wrap(int handle) {
		return new Renderbuffer(handle);
	}
	public static Renderbuffer create() {
		return new Renderbuffer(glGenRenderbuffers());
	}
}