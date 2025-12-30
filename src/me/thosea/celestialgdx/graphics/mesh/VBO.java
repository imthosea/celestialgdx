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
 * The buffer is automatically bound upon creation.
 * <p>
 * Upload vertex data with {@link #uploadVertices}. The VBO will be automatically bound if needed.
 * Using off-heap nio {@link Buffer}s has significantly faster transfer speed than arrays but is not required.
 * </p>
 * @author thosea
 * @see Mesh
 * @see <a href="https://wikis.khronos.org/opengl/Vertex_Specification#Vertex_Buffer_Object">
 * Vertex Specification/Vertex Buffer Object - OpenGL wiki</a>
 */
public final class VBO implements Disposable {
	private static int lastHandle = 0;

	private final int handle;
	private BufferUsage usage;
	private boolean disposed = false;

	private VBO(int handle, BufferUsage usage) {
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
		lastHandle = handle;
	}
	private void bindIfNeeded() {
		requireNotDisposed();
		if(lastHandle != this.handle) bind();
	}

	public void setExpectedUsage(BufferUsage usage) {
		Objects.requireNonNull(usage);
		this.usage = usage;
	}
	public BufferUsage getUsage() {
		return usage;
	}

	public void uploadVertices(ByteBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(ShortBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(IntBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(LongBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(FloatBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(DoubleBuffer buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}

	public void uploadVertices(short[] buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(int[] buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(long[] buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(float[] buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}
	public void uploadVertices(double[] buffer) {
		bindIfNeeded();
		glBufferData(GL_ARRAY_BUFFER, buffer, this.usage.glType);
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		glDeleteBuffers(this.handle);
		if(lastHandle == handle) lastHandle = 0;
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static VBO wrap(int handle) {
		return new VBO(handle, BufferUsage.STATIC);
	}
	public static VBO wrap(int handle, BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new VBO(handle, usage);
	}
	public static VBO create(BufferUsage usage) {
		Objects.requireNonNull(usage);
		return new VBO(glGenBuffers(), usage);
	}
}