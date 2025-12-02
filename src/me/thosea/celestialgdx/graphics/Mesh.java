package me.thosea.celestialgdx.graphics;

import com.badlogic.gdx.utils.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL33.*;

public final class Mesh implements Disposable {
	public final int vaoHandle;

	public final int vboHandle;
	/**
	 * can be -1 if there is no EBO
	 */
	public final int eboHandle;


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

		public final int code;

		BufferUsage(int usage) {
			this.code = usage;
		}
	}

	public final BufferUsage vertUsage;
	@Nullable public final BufferUsage eboUsage;

	private int eboType;

	/**
	 * Create a new VAO with no data
	 * @param vertUsage expected usage for vertex buffer
	 * @param eboUsage expected usage for index buffer. set to null to have no index buffer
	 * @param attribs attributes
	 */
	public Mesh(
			@NotNull Mesh.BufferUsage vertUsage,
			@Nullable Mesh.BufferUsage eboUsage,
			VxAttrib... attribs
	) {
		this.vaoHandle = glGenVertexArrays();
		this.vboHandle = glGenBuffers();
		this.vertUsage = Objects.requireNonNull(vertUsage);
		this.eboUsage = eboUsage;

		glBindVertexArray(vaoHandle);
		glBindBuffer(GL_ARRAY_BUFFER, vboHandle);

		if(eboUsage != null) {
			this.eboHandle = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboHandle);
		} else {
			this.eboHandle = -1;
		}

		int totalSize = 0;
		for(VxAttrib attrib : attribs) totalSize += attrib.size;

		int currentSize = 0;
		for(int i = 0; i < attribs.length; i++) {
			VxAttrib attrib = attribs[i];
			int stride = attrib.stride != -1 ? attrib.stride : totalSize;
			int pointer = attrib.pointer != -1 ? attrib.pointer : currentSize;

			glVertexAttribPointer(i, attrib.components, attrib.type, attrib.normalize, stride, pointer);
			glEnableVertexAttribArray(i);

			currentSize += attrib.size;
		}
	}

	/**
	 * Be sure to call this before calling {@link #uploadVertices}, {@link #uploadIndices} or {@link #render}
	 */
	public void bind() {
		glBindVertexArray(this.vaoHandle);
	}

	public void uploadVertices(ByteBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(ShortBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(IntBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(LongBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(FloatBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(DoubleBuffer buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}

	public void uploadVertices(short[] buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(int[] buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(long[] buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(float[] buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}
	public void uploadVertices(double[] buffer) {
		glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
		glBufferData(GL_ARRAY_BUFFER, buffer, this.vertUsage.code);
	}

	/*
	 * extracting if(!hasIndexBuffer) throw ... makes intellij complain about eboUsage being nullable
	 */

	//
	public void uploadIndices(ByteBuffer buffer) {
		if(!this.hasIndexBuffer()) throw new IllegalStateException("this mesh does not have an EBO");
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, eboUsage.code);
		this.eboType = GL_UNSIGNED_BYTE;
	}
	public void uploadIndices(ShortBuffer buffer) {
		if(!this.hasIndexBuffer()) throw new IllegalStateException("this mesh does not have an EBO");
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, eboUsage.code);
		this.eboType = GL_UNSIGNED_SHORT;
	}
	public void uploadIndices(IntBuffer buffer) {
		if(!this.hasIndexBuffer()) throw new IllegalStateException("this mesh does not have an EBO");
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, eboUsage.code);
		this.eboType = GL_UNSIGNED_INT;
	}

	public void uploadIndices(short[] buffer) {
		if(!this.hasIndexBuffer()) throw new IllegalStateException("this mesh does not have an EBO");
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, eboUsage.code);
		this.eboType = GL_UNSIGNED_SHORT;
	}
	public void uploadIndices(int[] buffer) {
		if(!this.hasIndexBuffer()) throw new IllegalStateException("this mesh does not have an EBO");
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, eboUsage.code);
		this.eboType = GL_UNSIGNED_INT;
	}

	public boolean hasIndexBuffer() {
		return eboUsage != null;
	}

	public void render(int mode, int count) {
		if(hasIndexBuffer()) {
			glDrawElements(mode, count, this.eboType, 0);
		} else {
			glDrawArrays(mode, 0, count);
		}
	}

	@Override
	public void dispose() {
		glDeleteVertexArrays(this.vaoHandle);
		glDeleteBuffers(this.vboHandle);
		if(hasIndexBuffer()) glDeleteBuffers(this.eboHandle);
	}

	public static final class VxAttrib {
		/**
		 * Component count for the attribute. 1 for single types, 2 for vec2, 3 for vec3, 4 for vec4
		 */
		public final int components;

		/**
		 * Component type. Can be: {@link GL33#GL_BYTE}, {@link GL33#GL_UNSIGNED_BYTE}, {@link GL33#GL_SHORT},
		 * {@link GL33# }, {@link GL33#GL_INT} {@link GL33#GL_INT}, {@link GL33#GL_UNSIGNED_INT},
		 * {@link GL33#GL_HALF_FLOAT}, {@link GL33#GL_FLOAT}, {@link GL33#GL_DOUBLE},
		 * {@link GL33#GL_INT_2_10_10_10_REV}, {@link GL33#GL_UNSIGNED_INT_2_10_10_10_REV},
		 * {@link GL33#GL_UNSIGNED_INT_10F_11F_11F_REV}
		 */
		public final int type;

		/**
		 * If true, fixed-point values will be normalized between [-1, +1] for signed values
		 * and [0, 1] for unsigned values
		 */
		public final boolean normalize;

		/**
		 * Offset between entries in the array.
		 * Set to -1 (the default) to have it calculated as the sum of all attributes in the mesh
		 */
		public final int stride;

		/**
		 * Offset to first entry in the array.
		 * Set to -1 (the default) to have it calculated as the sum of the previous attributes in the mesh
		 */
		public final int pointer;

		/**
		 * Size of the attribute in bytes, based on {@link #components} and {@link #type}.
		 * Calculated on construction
		 */
		public final int size;

		private VxAttrib(int components, int type, boolean normalize, int stride, int pointer) {
			if(components < 1 || components > 4) {
				throw new IllegalArgumentException("components must be between 1 and 4");
			}
			this.components = components;
			this.type = type;
			this.normalize = normalize;
			this.stride = stride;
			this.pointer = pointer;

			this.size = components * switch(type) {
				case GL_BYTE, GL_UNSIGNED_BYTE -> 1;
				case GL_SHORT, GL_UNSIGNED_SHORT, GL_HALF_FLOAT -> 2;
				case GL_INT, GL_UNSIGNALED, GL_INT_2_10_10_10_REV, GL_UNSIGNED_INT_2_10_10_10_REV, GL_FLOAT -> 4;
				case GL_DOUBLE -> 8;
				default -> throw new IllegalArgumentException("unsupported component type");
			};
		}

		public static VxAttrib of(int components, int type) {
			return new VxAttrib(components, type, false, -1, -1);
		}

		public static VxAttrib of(int components, int type, boolean normalize) {
			return new VxAttrib(components, type, normalize, -1, -1);
		}

		public static VxAttrib of(int components, int type, int stride, int pointer) {
			return new VxAttrib(components, type, false, stride, pointer);
		}

		public static VxAttrib of(int components, int type, boolean normalize, int stride, int pointer) {
			return new VxAttrib(components, type, normalize, stride, pointer);
		}
	}
}