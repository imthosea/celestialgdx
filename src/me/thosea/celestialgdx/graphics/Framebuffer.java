package me.thosea.celestialgdx.graphics;

import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL33.*;

/**
 * A framebuffer on the GPU's memory.
 * The buffer is bound upon creation and when {@link #bind} is called.
 * <p>
 * Use {@link #bindForRead()} to bind the buffer at {@link GL33#GL_READ_FRAMEBUFFER} for read operations.
 * Use {@link #bindForDraw()} to bind the buffer at {@link GL33#GL_DRAW_FRAMEBUFFER} for write operations.
 * Use {@link #bind()} to bind the buffer at {@link GL33#GL_FRAMEBUFFER} for write, read and attach operations.
 * </p>
 * <p>
 * Attach a {@link Texture} of type {@link GL33#GL_TEXTURE_2D} using
 * {@link #attachColor2D}, {@link #attachDepth}, or {@link #attachStencil2D}.
 * To attach 1D/3D textures, use the OpenGL methods directly.
 * Attach a {@link Renderbuffer} using
 * {@link #attachColor}, {@link #attachDepth}, or {@link #attachStencil}.
 * The buffer must be bound for drawing or both reading and drawing.
 * </p>
 * <p>
 * After attaching targets it's recommended to use {@link #checkComplete()} to make sure this framebuffer
 * is usable by the GPU. If not, it'll throw an exception with the reason.
 * </p>
 * @author thosea
 * @see <a href="https://wikis.khronos.org/opengl/Framebuffer">Framebuffer - OpenGL wiki</a>
 */
public final class Framebuffer implements Disposable {
	/** The default framebuffer created from the window. Cannot be disposed. */
	public static final Framebuffer DEFAULT = new Framebuffer(/*handle*/ 0);

	private static int lastReadBuffer = 0; // GL_READ_FRAMEBUFFER
	private static int lastDrawBuffer = 0; // GL_DRAW_FRAMEBUFFER

	private final int handle;
	private int target = -1;

	private boolean disposed = false;

	private Framebuffer(int handle) {
		this.handle = handle;
		this.bind();
	}

	public int getHandle() {
		this.requireNotDisposed();
		return handle;
	}

	public void bindForRead() {
		requireNotDisposed();
		glBindFramebuffer(this.target = GL_READ_FRAMEBUFFER, this.handle);
		lastReadBuffer = this.handle;
	}
	public void bindForDraw() {
		requireNotDisposed();
		glBindFramebuffer(this.target = GL_DRAW_FRAMEBUFFER, this.handle);
		lastDrawBuffer = this.handle;
	}
	public void bind() {
		requireNotDisposed();
		glBindFramebuffer(this.target = GL_FRAMEBUFFER, this.handle);
		lastReadBuffer = this.handle;
		lastDrawBuffer = this.handle;
	}

	public void requireReadBound() {
		requireNotDisposed();
		if(lastReadBuffer != this.handle) throw new IllegalStateException("the buffer is not bound for reading");
	}
	public void requireDrawBound() {
		requireNotDisposed();
		if(lastDrawBuffer != this.handle) throw new IllegalStateException("the buffer is not bound for drawing");
	}

	public void attachColor2D(Texture texture) {
		attachColor2D(texture, /*slot*/ 0);
	}
	public void attachColor2D(Texture texture, int slot) {
		if(texture.glType != GL_TEXTURE_2D) {
			throw new IllegalArgumentException("cannot attach non-2D texture");
		} else if(slot > 31) {
			throw new IllegalArgumentException("slot cannot be above 31");
		}
		requireDrawBound();
		glFramebufferTexture2D(
				GL_FRAMEBUFFER, // must always be GL_FRAMEBUFFER for some reason?
				GL_COLOR_ATTACHMENT0 + slot,
				GL_TEXTURE_2D, texture.getHandle(),
				/*level*/ 0
		);
	}
	public void attachDepth2D(Texture texture) {
		if(texture.glType != GL_TEXTURE_2D) {
			throw new IllegalArgumentException("cannot attach non-2D texture");
		}
		requireDrawBound();
		glFramebufferTexture2D(
				GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
				GL_TEXTURE_2D, texture.getHandle(),
				/*level*/ 0
		);
	}
	public void attachStencil2D(Texture texture) {
		if(texture.glType != GL_TEXTURE_2D) {
			throw new IllegalArgumentException("cannot attach non-2D texture");
		}
		requireDrawBound();
		glFramebufferTexture2D(
				GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT,
				GL_TEXTURE_2D, texture.getHandle(),
				/*level*/ 0
		);
	}
	public void attachDepthStencil2D(Texture texture) {
		if(texture.glType != GL_TEXTURE_2D) {
			throw new IllegalArgumentException("cannot attach non-2D texture");
		}
		requireDrawBound();
		glFramebufferTexture2D(
				GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
				GL_TEXTURE_2D, texture.getHandle(),
				/*level*/ 0
		);
	}

	public void attachColor(Renderbuffer buffer) {
		attachColor(buffer, 0);
	}
	public void attachColor(Renderbuffer buffer, int slot) {
		if(slot > 31) {
			throw new IllegalArgumentException("slot cannot be above 31");
		}
		requireDrawBound();
		glFramebufferRenderbuffer(
				GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + slot,
				GL_RENDERBUFFER, buffer.getHandle()
		);
	}
	public void attachDepth(Renderbuffer buffer) {
		requireDrawBound();
		glFramebufferRenderbuffer(
				GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
				GL_RENDERBUFFER, buffer.getHandle()
		);
	}
	public void attachStencil(Renderbuffer buffer) {
		requireDrawBound();
		glFramebufferRenderbuffer(
				GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT,
				GL_RENDERBUFFER, buffer.getHandle()
		);
	}
	public void attachDepthStencil(Renderbuffer buffer) {
		requireDrawBound();
		glFramebufferRenderbuffer(
				GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
				GL_RENDERBUFFER, buffer.getHandle()
		);
	}

	public void checkComplete() {
		requireNotDisposed();
		int status = glCheckFramebufferStatus(this.target);
		if(status == GL_FRAMEBUFFER_COMPLETE) return;

		// https://registry.khronos.org/OpenGL-Refpages/gl4/html/glCheckFramebufferStatus.xhtml
		String error = switch(status) {
			case GL_FRAMEBUFFER_UNDEFINED -> "Undefined framebuffer";
			case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "Framebuffer has incomplete attachment point";
			case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "Framebuffer has no images attached";
			case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "Framebuffer has incomplete draw buffer";
			case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "Framebuffer has incomplete read buffer";
			case GL_FRAMEBUFFER_UNSUPPORTED -> "Framebuffer uses unsupported formats for attachments";
			case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE ->
					"Framebuffer uses different sample count for specific attachments";
			case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS ->
					"Framebuffer has one layered attachment while the others aren't layered";
			default -> "Unknown status (code " + status + ")";
		};
		throw new IllegalStateException(error);
	}

	public void setDrawTargets(int... targets) {
		this.requireDrawBound();
		glDrawBuffers(targets);
	}

	/**
	 * Copies one framebuffer onto another.
	 * Mask is a bitwise OR of what values to be copied. Can be
	 * {@link GL33#GL_COLOR_BUFFER_BIT}, {@link GL33#GL_DEPTH_BUFFER_BIT} and/or {@link GL33#GL_STENCIL_BUFFER_BIT}.
	 */
	public static void blit(
			Framebuffer src, Framebuffer target,
			int srcX0, int srcY0,
			int srcX1, int srcY1,
			int dstX0, int dstY0,
			int dstX1, int dstY1,
			int mask, BlitFilter filter
	) {
		src.requireReadBound();
		target.requireDrawBound();

		glBlitFramebuffer(
				srcX0, srcY0,
				srcX1, srcY1,
				dstX0, dstY0,
				dstX1, dstY1,
				mask, filter.glType
		);
	}

	@Override
	public void dispose() {
		if(this.handle == 0) {
			throw new IllegalStateException("cannot dispose the default framebuffer");
		}
		this.requireNotDisposed();
		glDeleteFramebuffers(this.handle);
		if(lastReadBuffer == this.handle) lastReadBuffer = 0;
		if(lastDrawBuffer == this.handle) lastDrawBuffer = 0;
		disposed = true;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static Framebuffer wrap(int handle) {
		return new Framebuffer(handle);
	}
	public static Framebuffer create() {
		return new Framebuffer(glGenFramebuffers());
	}

	public enum BlitFilter {
		LINEAR(GL_LINEAR),
		NEAREST(GL_NEAREST);

		public final int glType;
		BlitFilter(int glType) {
			this.glType = glType;
		}
	}
}