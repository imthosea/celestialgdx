package me.thosea.celestialgdx.graphics;

import me.thosea.celestialgdx.utils.Disposable;

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

	public void allocate(int format, int width, int height) {
		this.requireNotDisposed();
		if(lastHandle != this.handle) {
			bind();
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