package me.thosea.celestialgdx.graphics;

import com.badlogic.gdx.utils.Disposable;

import static org.lwjgl.opengl.GL30.*;

/**
 * A buffer optimized for being a render target for {@link Framebuffer}s.
 * Cannot directly be sampled from.
 * @author thosea
 * @see Framebuffer
 * @see <a href="https://wikis.khronos.org/opengl/Renderbuffer">Renderbuffer - OpenGL wiki</a>
 */
public final class Renderbuffer implements Disposable {
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
	}

	public void allocate(int format, int width, int height) {
		this.requireNotDisposed();
		glRenderbufferStorage(GL_RENDERBUFFER, format, width, height);
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		glDeleteFramebuffers(this.handle);
		this.disposed = true;
	}

	public boolean isDisposed() {
		return disposed;
	}
	public void requireNotDisposed() {
		if(disposed) throw new IllegalStateException("already disposed");
	}

	public static Renderbuffer wrap(int handle) {
		return new Renderbuffer(handle);
	}
	public static Renderbuffer create() {
		return new Renderbuffer(glGenRenderbuffers());
	}
}