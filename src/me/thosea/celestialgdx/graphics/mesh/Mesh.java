package me.thosea.celestialgdx.graphics.mesh;

import me.thosea.celestialgdx.utils.Disposable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;

import java.nio.Buffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * An OpenGL VAO. Upon creation by {@link #create},
 * a VAO bound to the specified vertex attributes is created with an empty vertex and index buffer.
 * Both {@link #create} and {@link #wrap} will automatically bind the mesh.
 * <p>
 * To upload vertex or index data, use a variant of {@link #uploadVertices} / {@link #uploadIndices}.
 * The VBO/EBO will automatically be bound if needed.
 * Note that using off-heap nio {@link Buffer}s has significantly faster transfer speed than arrays.
 * </p>
 * <p>
 * Creating an index buffer can be skipped entirely by passing null as the second parameter
 * to {@link #create}.
 * </p>
 * Render by calling {@link #render}. The VAO will be automatically bound if needed.
 * <p>
 * Since CelestialGDX doesn't support mobile where the OpenGL context can be lost mid-runtime,
 * this class doesn't need to store the last uploaded buffer or reload it automatically.
 * </p>
 * @author thosea
 * @see <a href="https://wikis.khronos.org/opengl/Vertex_Specification">Vertex Specification - OpenGL wiki</a>
 */
public final class Mesh implements Disposable {
	private static int lastVao = 0;

	private final int vaoHandle;
	@Nullable private final EBO ebo;

	private boolean disposed = false;

	private Mesh(@Nullable EBO ebo, int vaoHandle) {
		this.vaoHandle = vaoHandle;
		this.ebo = ebo;

		glBindVertexArray(vaoHandle);
		lastVao = this.vaoHandle;
		if(ebo != null) ebo.bind();
	}

	private Mesh(@Nullable EBO ebo, VxAttrib[] attribs) {
		this.vaoHandle = glGenVertexArrays();
		this.ebo = ebo;

		glBindVertexArray(vaoHandle);
		lastVao = this.vaoHandle;
		if(ebo != null) ebo.bind();

		int currentSize = 0;
		int lastBuffer = 0;
		for(int i = 0; i < attribs.length; i++) {
			VxAttrib attrib = attribs[i];
			int buffer = attrib.sourceHandle;
			if(buffer != lastBuffer) {
				lastBuffer = buffer;
				currentSize = 0;
				VBO.wrap(buffer).bind();
			}

			int stride = attrib.stride != -1 ? attrib.stride : 0;
			int pointer = attrib.pointer != -1 ? attrib.pointer : currentSize;

			glVertexAttribPointer(i, attrib.components, attrib.type, attrib.normalize, stride, pointer);
			glEnableVertexAttribArray(i);

			currentSize += attrib.size;
		}
	}

	public int getHandle() {
		requireNotDisposed();
		return vaoHandle;
	}
	@Nullable
	public EBO getEbo() {
		return ebo;
	}

	/** Binds the VAO of this mesh (this also binds the EBO if present) */
	public void bind() {
		requireNotDisposed();
		glBindVertexArray(this.vaoHandle);
		lastVao = this.vaoHandle;
		if(ebo != null) ebo.markBound();
	}

	public void render(int mode, int count) {
		if(lastVao != this.vaoHandle) bind();
		if(ebo != null) {
			glDrawElements(mode, count, ebo.getEboType(), 0);
		} else {
			glDrawArrays(mode, 0, count);
		}
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		glDeleteVertexArrays(this.vaoHandle);
		if(lastVao == this.vaoHandle) lastVao = 0;
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	/** Create a new VAO with an EBO */
	public static Mesh create(EBO ebo, VxAttrib... attribs) {
		return new Mesh(ebo, attribs);
	}
	/** Create a new VAO with no EBO */
	public static Mesh create(VxAttrib... attribs) {
		return new Mesh(/*ebo*/ null, attribs);
	}

	/** Wrap an existing VAO with an EBO handle */
	public static Mesh wrap(EBO ebo, int handle) {
		return new Mesh(ebo, handle);
	}
	/** Wrap an existing VAO with no EBO */
	public static Mesh wrap(int handle) {
		return new Mesh(/*ebo*/ null, handle);
	}

	public static final class VxAttrib {
		/** Handle to the VBO that contains this attribute */
		public final int sourceHandle;

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

		private VxAttrib(int sourceHandle, int components, int type, boolean normalize, int stride, int pointer) {
			if(components < 1 || components > 4) {
				throw new IllegalArgumentException("components must be between 1 and 4");
			}
			this.sourceHandle = sourceHandle;
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

		public static VxAttrib of(int sourceHandle, int components, int type) {
			return new VxAttrib(sourceHandle, components, type, false, -1, -1);
		}
		public static VxAttrib of(int sourceHandle, int components, int type, boolean normalize) {
			return new VxAttrib(sourceHandle, components, type, normalize, -1, -1);
		}
		public static VxAttrib of(int sourceHandle, int components, int type, int stride, int pointer) {
			return new VxAttrib(sourceHandle, components, type, false, stride, pointer);
		}
		public static VxAttrib of(int sourceHandle, int components, int type, boolean normalize, int stride, int pointer) {
			return new VxAttrib(sourceHandle, components, type, normalize, stride, pointer);
		}

		public static VxAttrib of(VBO source, int components, int type) {
			return new VxAttrib(source.getHandle(), components, type, false, -1, -1);
		}
		public static VxAttrib of(VBO source, int components, int type, boolean normalize) {
			return new VxAttrib(source.getHandle(), components, type, normalize, -1, -1);
		}
		public static VxAttrib of(VBO source, int components, int type, int stride, int pointer) {
			return new VxAttrib(source.getHandle(), components, type, false, stride, pointer);
		}
		public static VxAttrib of(VBO source, int components, int type, boolean normalize, int stride, int pointer) {
			return new VxAttrib(source.getHandle(), components, type, normalize, stride, pointer);
		}
	}
}