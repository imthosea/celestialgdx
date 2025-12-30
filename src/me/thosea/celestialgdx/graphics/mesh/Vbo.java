package me.thosea.celestialgdx.graphics.mesh;

import me.thosea.celestialgdx.utils.Disposable;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL15.*;

/**
 * An OpenGL VBO. Most commonly used with VAOs (see {@link Mesh}).
 * The buffer is bound upon creation and when {@link #bind} is called.
 * <p>
 * Upload vertex data with {@link #uploadVertices}. The buffer must be bound prior.
 * Using off-heap nio {@link Buffer}s has significantly faster transfer speed than arrays but is not required.
 * </p>
 * @author thosea
 * @see Mesh
 * @see <a href="https://wikis.khronos.org/opengl/Vertex_Specification#Vertex_Buffer_Object">
 * Vertex Specification/Vertex Buffer Object - OpenGL wiki</a>
 */
public final class Vbo implements Disposable {
	private static long lastHandle = 0;

	private final int handle;
	private BufferUsage usage;
	private boolean disposed = false;

	private Vbo(int handle, BufferUsage usage) {
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
		glBindBuffer(GL_ARRAY_BUFFER, handle);
		lastHandle = this.handle;
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

	public void uploadVertices(ByteBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(ShortBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(IntBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(LongBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(FloatBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(DoubleBuffer buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}

	public void uploadVertices(short[] buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(int[] buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(long[] buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(float[] buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(double[] buffer) {
		requireBound();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
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

	public static Vbo wrap(int handle) {
		return new Vbo(handle, BufferUsage.STATIC);
	}
	public static Vbo wrap(int handle, BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new Vbo(handle, usage);
	}
	public static Vbo create(BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new Vbo(glGenBuffers(), usage);
	}
}