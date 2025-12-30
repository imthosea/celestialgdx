package me.thosea.celestialgdx.graphics.mesh;

import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL33.*;

/**
 * An OpenGL VAO. Contains attributes pointing to VBOs and an EBO binding.
 * The buffer is automatically bound upon creation.
 * <p>
 * Set attributes using {@link #setAttributes}. The VAO must be bound.
 * If autoPosition is enabled, the stride and initial pointer will be automatically calculated
 * for each attribute. It will throw an exception if the attributes are sourced from multiple buffers, however.
 * If autoPosition is disabled, the stride and pointer for each attribute must be explicitly specified.
 * </p>
 * <p>
 * Set the known attached EBO using {@link #setEbo}. The VAO and EBO must be bound.
 * </p>
 * <p>
 * Render by calling {@link #render} while the VAO is bound.
 * If the known EBO is set, it will be used when rendering.
 * </p>
 * @author thosea
 * @see <a href="https://wikis.khronos.org/opengl/Vertex_Specification">Vertex Specification - OpenGL wiki</a>
 */
public final class Mesh implements Disposable {
	private static int lastHandle = 0;

	private final int handle;
	private Ebo ebo = null;
	private int lastAttribCount = 0;
	private boolean disposed = false;

	private Mesh(int handle) {
		this.handle = handle;
		this.bind();
	}

	public int getHandle() {
		requireNotDisposed();
		return handle;
	}

	public void bind() {
		requireNotDisposed();
		glBindVertexArray(this.handle);
		lastHandle = handle;
		if(ebo != null) ebo.markBound();
	}
	public void requireBound() {
		requireNotDisposed();
		if(lastHandle != this.handle) throw new IllegalStateException("the buffer is not bound");
	}

	public void setAttributes(boolean autoPosition, VxAttrib... attribs) {
		requireBound();
		if(this.lastAttribCount > attribs.length) {
			for(int i = attribs.length; i < lastAttribCount; i++) {
				glDisableVertexAttribArray(i);
			}
		}
		this.lastAttribCount = attribs.length;

		if(attribs.length == 0) return;

		if(autoPosition) {
			setAttribsAuto(attribs);
		} else {
			setAttribsManual(attribs);
		}
	}

	private void setAttribsManual(VxAttrib[] attribs) {
		Vbo lastBuffer = null;
		for(int i = 0; i < attribs.length; i++) {
			VxAttrib attrib = attribs[i];
			if(attrib.source != lastBuffer) {
				lastBuffer = attrib.source;
				attrib.source.bind();
			}
			if(attrib.stride == -1 || attrib.pointer == -1) {
				throw new IllegalArgumentException("automatic positioning is disabled and stride/pointer wasn't specified");
			}
			addAttribute(i, attrib, attrib.stride, attrib.pointer);
		}
	}

	private void setAttribsAuto(VxAttrib[] attribs) {
		Vbo initialBuffer = attribs[0].source;
		initialBuffer.bind();

		int totalSize = 0;
		for(VxAttrib attrib : attribs) {
			totalSize += attrib.size;
			if(attrib.source.getHandle() != initialBuffer.getHandle()) {
				throw new IllegalArgumentException("cannot use automatic positioning when there are multiple source VBOs");
			}
		}
		int currentSize = 0;
		for(int i = 0; i < attribs.length; i++) {
			VxAttrib attrib = attribs[i];
			int stride = attrib.stride != -1 ? attrib.stride : totalSize;
			int pointer = attrib.pointer != -1 ? attrib.pointer : currentSize;
			addAttribute(i, attrib, stride, pointer);
			currentSize = pointer + attrib.size;
		}
	}

	private void addAttribute(int i, VxAttrib attrib, int stride, int pointer) {
		glVertexAttribPointer(i, attrib.components, attrib.type, attrib.normalize, stride, pointer);
		glEnableVertexAttribArray(i);
	}

	public void setEbo(Ebo ebo) {
		this.requireBound();
		if(ebo != null) {
			ebo.bind();
			this.ebo = ebo;
		} else {
			if(this.ebo != null) {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			}
			this.ebo = null;
		}
	}

	public void render(int mode, int count) {
		requireBound();
		if(ebo != null) {
			glDrawElements(mode, count, ebo.getEboType(), 0);
		} else {
			glDrawArrays(mode, 0, count);
		}
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		glDeleteVertexArrays(this.handle);
		if(lastHandle == this.handle) lastHandle = 0;
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static Mesh create() {
		return new Mesh(glGenVertexArrays());
	}
	public static Mesh wrap(int handle) {
		return new Mesh(handle);
	}

	public static final class VxAttrib {
		/** VBO that contains this attribute */
		public final Vbo source;

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

		private VxAttrib(Vbo source, int components, int type, boolean normalize, int stride, int pointer) {
			if(components < 1 || components > 4) {
				throw new IllegalArgumentException("components must be between 1 and 4");
			}
			this.source = source;
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

		public static VxAttrib of(Vbo source, int components, int type) {
			return new VxAttrib(source, components, type, false, -1, -1);
		}
		public static VxAttrib of(Vbo source, int components, int type, boolean normalize) {
			return new VxAttrib(source, components, type, normalize, -1, -1);
		}
		public static VxAttrib of(Vbo source, int components, int type, int stride, int pointer) {
			return new VxAttrib(source, components, type, false, stride, pointer);
		}
		public static VxAttrib of(Vbo source, int components, int type, boolean normalize, int stride, int pointer) {
			return new VxAttrib(source, components, type, normalize, stride, pointer);
		}
	}
}