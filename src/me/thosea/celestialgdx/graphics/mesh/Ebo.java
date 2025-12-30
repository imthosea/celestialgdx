package me.thosea.celestialgdx.graphics.mesh;

import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.opengl.GL33;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL15.*;

/**
 * An OpenGL EBO. Most commonly used with VAOs (see {@link Mesh}).
 * The buffer is bound upon creation and when {@link #bind} is called.
 * <p>
 * Upload index data with {@link #uploadIndices}. The buffer must be bound prior.
 * Using off-heap nio {@link Buffer}s has significantly faster transfer speed than arrays but is not required.
 * </p>
 * @author thosea
 * @see Mesh
 * @see <a href="https://wikis.khronos.org/opengl/Vertex_Specification#Index_buffers">
 * Vertex Specification/Index Buffers - OpenGL wiki</a>
 */
public final class Ebo implements Disposable {
	private static int lastHandle = 0;

	private final int handle;

	private int eboType = -1;
	private BufferUsage usage;
	private boolean disposed = false;

	private Ebo(int handle, BufferUsage usage) {
		this.handle = handle;
		this.usage = usage;
		bind();
	}

	public int getHandle() {
		requireNotDisposed();
		return handle;
	}

	public void bind() {
		requireNotDisposed();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle);
		this.markBound();
	}
	void markBound() {
		lastHandle = handle;
	}
	public void requireBound() {
		requireNotDisposed();
		if(lastHandle != this.handle) throw new IllegalStateException("the buffer is not bound");
	}

	public void setExpectedUsage(BufferUsage usage) {
		Objects.requireNonNull(usage);
		this.usage = usage;
	}
	public BufferUsage getUsage() {
		return usage;
	}

	/** Gets the last uploaded buffer type {i.e. {@link GL33#GL_UNSIGNED_SHORT}}. Throws if not uploaded yet. */
	public int getEboType() {
		requireNotDisposed();
		if(eboType == -1) throw new IllegalStateException("no index data uploaded yet");
		return eboType;
	}
	/**
	 * Sets the known EBO data type (i.e. {@link GL33#GL_UNSIGNED_SHORT}).
	 * This is done automatically when {@link #uploadIndices} is called.
	 */
	public void setEboType(int eboType) {
		requireNotDisposed();
		this.eboType = eboType;
	}

	public void uploadIndices(ByteBuffer buffer) {
		requireBound();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage.glType);
		this.eboType = GL_UNSIGNED_BYTE;
	}
	public void uploadIndices(ShortBuffer buffer) {
		requireBound();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage.glType);
		this.eboType = GL_UNSIGNED_SHORT;
	}
	public void uploadIndices(IntBuffer buffer) {
		requireBound();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage.glType);
		this.eboType = GL_UNSIGNED_INT;
	}

	public void uploadIndices(short[] buffer) {
		requireBound();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage.glType);
		this.eboType = GL_UNSIGNED_SHORT;
	}
	public void uploadIndices(int[] buffer) {
		requireBound();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage.glType);
		this.eboType = GL_UNSIGNED_INT;
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		glDeleteBuffers(this.handle);
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static Ebo wrap(int handle, BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new Ebo(handle, usage);
	}
	public static Ebo create(BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new Ebo(glGenBuffers(), usage);
	}
}