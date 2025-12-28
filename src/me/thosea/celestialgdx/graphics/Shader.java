/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package me.thosea.celestialgdx.graphics;

import com.badlogic.gdx.utils.Disposable;
import me.thosea.celestialgdx.files.FileHandle;
import org.joml.Matrix2fc;
import org.joml.Matrix3fc;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * A shader encapsulates a vertex and fragment shader pair linked to form a shader program.
 * When a shader is bound, you can use fields exposed by the superclass to set uniforms.
 *
 * <p>
 * Shaders are compiled when constructed and can be recompiled using {@link #compile(String, String)}.
 * If compilation fails, an exception will be thrown, and, in the case of recompiling, will not
 * update the current {@link Shader} to point to the new program. This means a {@link Shader} can never
 * represent a shader which isn't compiled.
 * </p>
 *
 * <p>
 * A shader must be disposed via a call to {@link Shader#dispose()} when it is no longer needed
 * </p>
 * @author thosea
 */
@SuppressWarnings("unused")
public abstract class Shader implements Disposable {
	/** default name for position attributes **/
	public static final String POSITION_ATTRIBUTE = "a_position";
	/** default name for normal attributes **/
	public static final String NORMAL_ATTRIBUTE = "a_normal";
	/** default name for color attributes **/
	public static final String COLOR_ATTRIBUTE = "a_color";
	/** default name for texcoords attributes, append texture unit number **/
	public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";

	private int id = -1;

	protected Shader(FileHandle vertexFile, FileHandle fragmentFile) {
		this(vertexFile.readString(), fragmentFile.readString());
	}

	/**
	 * If the shaders fail to compile, an exception will be thrown
	 * @param vertexShader vertex
	 * @param fragmentShader fragment
	 */
	protected Shader(String vertexShader, String fragmentShader) {
		Objects.requireNonNull(vertexShader);
		Objects.requireNonNull(fragmentShader);
		this.compile(vertexShader, fragmentShader);
	}

	public void compile(String vertexShader, String fragmentShader) {
		int vertexId = compile("vertex", GL_VERTEX_SHADER, vertexShader, /*deleteOnFail*/ -1);
		int fragmentId = compile("fragment", GL_FRAGMENT_SHADER, fragmentShader, /*deleteOnFail*/ vertexId);

		int programId = glCreateProgram();
		glAttachShader(programId, vertexId);
		glAttachShader(programId, fragmentId);
		glLinkProgram(programId);
		glDeleteShader(vertexId);
		glDeleteShader(fragmentId);

		if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			String error = glGetProgramInfoLog(programId);
			throw new IllegalStateException("Failed to link shaders\n" + error);
		}

		if(id != -1) {
			glDeleteProgram(this.id);
		}
		this.id = programId;
	}

	private int compile(String name, int type, String source, int deleteOnFail) {
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
			String log = glGetShaderInfoLog(shader);
			glDeleteShader(shader);
			if(deleteOnFail != -1) glDeleteShader(deleteOnFail);
			throw new IllegalStateException("Failed to compile " + name + " shader\n" + log);
		}
		return shader;
	}

	public void bind() {
		glUseProgram(id);
	}

	public int getHandle() {
		return id;
	}

	@Override
	public void dispose() {
		glDeleteProgram(this.id);
	}

	public abstract class Uniform {
		private final String name;
		private int location;

		protected Uniform(String name) {
			this.name = name;
			this.setLocation();
		}

		protected void setLocation() {
			this.location = glGetUniformLocation(Shader.this.id, this.name);
			if(location == -1) {
				throw new IllegalStateException("No uniform named " + name);
			}
		}

		public int getLocation() {
			return location;
		}
	}

	// region float uniforms
	public final class FloatUniform extends Uniform {
		FloatUniform(String name) {super(name);}
		public void set(float value) {
			glUniform1f(getLocation(), value);
		}
	}
	protected final FloatUniform uFloat(String name) {return new FloatUniform(name);}
	public final class FloatArrayUniform extends Uniform {
		FloatArrayUniform(String name) {super(name);}
		public void set(float[] values) {
			glUniform1fv(getLocation(), values);
		}
		public void set(FloatBuffer buffer) {
			glUniform1fv(getLocation(), buffer);
		}
	}
	protected final FloatArrayUniform uFloatArray(String name) {return new FloatArrayUniform(name);}
	public final class Vec2fUniform extends Uniform {
		Vec2fUniform(String name) {super(name);}
		public void set(float a, float b) {
			glUniform2f(getLocation(), a, b);
		}
	}
	protected final Vec2fUniform uVec2f(String name) {return new Vec2fUniform(name);}
	public final class Vec2fArrayUniform extends Uniform {
		Vec2fArrayUniform(String name) {super(name);}
		public void set(float[] values) {
			glUniform2fv(getLocation(), values);
		}
		public void set(FloatBuffer buffer) {
			glUniform2fv(getLocation(), buffer);
		}
	}
	protected final Vec2fArrayUniform uVec2fArray(String name) {return new Vec2fArrayUniform(name);}
	public final class Vec3fUniform extends Uniform {
		Vec3fUniform(String name) {super(name);}
		public void set(float a, float b, float c) {
			glUniform3f(getLocation(), a, b, c);
		}
	}
	protected final Vec3fUniform uVec3f(String name) {return new Vec3fUniform(name);}
	public final class Vec3fArrayUniform extends Uniform {
		Vec3fArrayUniform(String name) {super(name);}
		public void set(float[] values) {
			glUniform3fv(getLocation(), values);
		}
		public void set(FloatBuffer buffer) {
			glUniform3fv(getLocation(), buffer);
		}
	}
	protected final Vec3fArrayUniform uVec3fArray(String name) {return new Vec3fArrayUniform(name);}
	public final class Vec4fUniform extends Uniform {
		Vec4fUniform(String name) {super(name);}
		public void set(float a, float b, float c, float d) {
			glUniform4f(getLocation(), a, b, c, d);
		}
	}
	protected final Vec4fUniform uVec4f(String name) {return new Vec4fUniform(name);}
	public final class Vec4fArrayUniform extends Uniform {
		Vec4fArrayUniform(String name) {super(name);}
		public void set(float[] values) {
			glUniform4fv(getLocation(), values);
		}
		public void set(FloatBuffer buffer) {
			glUniform4fv(getLocation(), buffer);
		}
	}
	protected final Vec4fArrayUniform uVec4fArray(String name) {return new Vec4fArrayUniform(name);}
	// endregion

	// region int uniforms
	public final class IntUniform extends Uniform {
		IntUniform(String name) {super(name);}
		public void set(int value) {
			glUniform1i(getLocation(), value);
		}
	}
	protected final IntUniform uInt(String name) {return new IntUniform(name);}
	public final class IntArrayUniform extends Uniform {
		IntArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform1iv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform1iv(getLocation(), buffer);
		}
	}
	protected final IntArrayUniform uIntArray(String name) {return new IntArrayUniform(name);}
	public final class Vec2iUniform extends Uniform {
		Vec2iUniform(String name) {super(name);}
		public void set(int a, int b) {
			glUniform2i(getLocation(), a, b);
		}
	}
	protected final Vec2iUniform uVec2i(String name) {return new Vec2iUniform(name);}
	public final class Vec2iArrayUniform extends Uniform {
		Vec2iArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform2iv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform2iv(getLocation(), buffer);
		}
	}
	protected final Vec2iArrayUniform uVec2iArray(String name) {return new Vec2iArrayUniform(name);}
	public final class Vec3iUniform extends Uniform {
		Vec3iUniform(String name) {super(name);}
		public void set(int a, int b, int c) {
			glUniform3i(getLocation(), a, b, c);
		}
	}
	protected final Vec3iUniform uVec3i(String name) {return new Vec3iUniform(name);}
	public final class Vec3iArrayUniform extends Uniform {
		Vec3iArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform3iv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform3iv(getLocation(), buffer);
		}
	}
	protected final Vec3iArrayUniform uVec3iArray(String name) {return new Vec3iArrayUniform(name);}
	public final class Vec4iUniform extends Uniform {
		Vec4iUniform(String name) {super(name);}
		public void set(int a, int b, int c, int d) {
			glUniform4i(getLocation(), a, b, c, d);
		}
	}
	protected final Vec4iUniform uVec4i(String name) {return new Vec4iUniform(name);}
	public final class Vec4iArrayUniform extends Uniform {
		Vec4iArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform4iv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform4iv(getLocation(), buffer);
		}
	}
	protected final Vec4iArrayUniform uVec4iArray(String name) {return new Vec4iArrayUniform(name);}
	// endregion

	// region unsigned int uniforms
	public final class UIntUniform extends Uniform {
		UIntUniform(String name) {super(name);}
		public void set(int value) {
			glUniform1ui(getLocation(), value);
		}
	}
	protected final UIntUniform uUInt(String name) {return new UIntUniform(name);}
	public final class UIntArrayUniform extends Uniform {
		UIntArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform1uiv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform1uiv(getLocation(), buffer);
		}
	}
	protected final UIntArrayUniform uUIntArray(String name) {return new UIntArrayUniform(name);}
	public final class Vec2uiUniform extends Uniform {
		Vec2uiUniform(String name) {super(name);}
		public void set(int a, int b) {
			glUniform2ui(getLocation(), a, b);
		}
	}
	protected final Vec2uiUniform uVec2ui(String name) {return new Vec2uiUniform(name);}
	public final class Vec2uiArrayUniform extends Uniform {
		Vec2uiArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform2uiv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform2uiv(getLocation(), buffer);
		}
	}
	protected final Vec2uiArrayUniform uVec2uiArray(String name) {return new Vec2uiArrayUniform(name);}
	public final class Vec3uiUniform extends Uniform {
		Vec3uiUniform(String name) {super(name);}
		public void set(int a, int b, int c) {
			glUniform3ui(getLocation(), a, b, c);
		}
	}
	protected final Vec3uiUniform uVec3ui(String name) {return new Vec3uiUniform(name);}
	public final class Vec3uiArrayUniform extends Uniform {
		Vec3uiArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform3uiv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform3uiv(getLocation(), buffer);
		}
	}
	protected final Vec3uiArrayUniform uVec3uiArray(String name) {return new Vec3uiArrayUniform(name);}
	public final class Vec4uiUniform extends Uniform {
		Vec4uiUniform(String name) {super(name);}
		public void set(int a, int b, int c, int d) {
			glUniform4ui(getLocation(), a, b, c, d);
		}
	}
	protected final Vec4uiUniform uVec4ui(String name) {return new Vec4uiUniform(name);}
	public final class Vec4uiArrayUniform extends Uniform {
		Vec4uiArrayUniform(String name) {super(name);}
		public void set(int[] values) {
			glUniform4uiv(getLocation(), values);
		}
		public void set(IntBuffer buffer) {
			glUniform4uiv(getLocation(), buffer);
		}
	}
	protected final Vec4uiArrayUniform uVec4uiArray(String name) {return new Vec4uiArrayUniform(name);}
	// endregion

	// region boolean uniforms
	public final class BoolUniform extends Uniform {
		BoolUniform(String name) {super(name);}
		public void set(boolean value) {
			glUniform1i(getLocation(), value ? 1 : 0);
		}
	}
	protected final BoolUniform uBool(String name) {return new BoolUniform(name);}
	public final class BoolArrayUniform extends Uniform {
		BoolArrayUniform(String name) {super(name);}
		public void set(boolean[] values) {
			try(MemoryStack stack = stackPush()) {
				IntBuffer buffer = stack.mallocInt(values.length);
				for(boolean bool : values) buffer.put(bool ? 1 : 0);
				glUniform1iv(getLocation(), buffer);
			}
		}
	}
	protected final BoolArrayUniform uBoolArray(String name) {return new BoolArrayUniform(name);}
	public final class Vec2bUniform extends Uniform {
		Vec2bUniform(String name) {super(name);}
		public void set(boolean a, boolean b) {
			glUniform2i(getLocation(), a ? 1 : 0, b ? 1 : 0);
		}
	}
	protected final Vec2bUniform uVec2b(String name) {return new Vec2bUniform(name);}
	public final class Vec2bArrayUniform extends Uniform {
		Vec2bArrayUniform(String name) {super(name);}
		public void set(boolean[] values) {
			try(MemoryStack stack = stackPush()) {
				IntBuffer buffer = stack.mallocInt(values.length);
				for(boolean bool : values) buffer.put(bool ? 1 : 0);
				glUniform2iv(getLocation(), buffer);
			}
		}
	}
	protected final Vec2bArrayUniform uVec2bArray(String name) {return new Vec2bArrayUniform(name);}
	public final class Vec3bUniform extends Uniform {
		Vec3bUniform(String name) {super(name);}
		public void set(boolean a, boolean b, boolean c) {
			glUniform3i(getLocation(), a ? 1 : 0, b ? 1 : 0, c ? 1 : 0);
		}
	}
	protected final Vec3bUniform uVec3b(String name) {return new Vec3bUniform(name);}
	public final class Vec3bArrayUniform extends Uniform {
		Vec3bArrayUniform(String name) {super(name);}
		public void set(boolean[] values) {
			try(MemoryStack stack = stackPush()) {
				IntBuffer buffer = stack.mallocInt(values.length);
				for(boolean bool : values) buffer.put(bool ? 1 : 0);
				glUniform3iv(getLocation(), buffer);
			}
		}
	}
	protected final Vec3bArrayUniform uVec3bArray(String name) {return new Vec3bArrayUniform(name);}
	public final class Vec4bUniform extends Uniform {
		Vec4bUniform(String name) {super(name);}
		public void set(boolean a, boolean b, boolean c, boolean d) {
			glUniform4i(getLocation(), a ? 1 : 0, b ? 1 : 0, c ? 1 : 0, d ? 1 : 0);
		}
	}
	protected final Vec4bUniform uVec4b(String name) {return new Vec4bUniform(name);}
	public final class Vec4bArrayUniform extends Uniform { // most useless GLSL type?
		Vec4bArrayUniform(String name) {super(name);}
		public void set(boolean[] values) {
			try(MemoryStack stack = stackPush()) {
				IntBuffer buffer = stack.mallocInt(values.length);
				for(boolean bool : values) buffer.put(bool ? 1 : 0);
				glUniform4iv(getLocation(), buffer);
			}
		}
	}
	protected final Vec4bArrayUniform uVec4bArray(String name) {return new Vec4bArrayUniform(name);}
	// endregion

	// region square matrix uniforms
	public final class Mat2fUniform extends Uniform {
		static final int FLOATS = 4;
		Mat2fUniform(String name) {super(name);}
		public void set(Matrix2fc value, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = value.get(stack.mallocFloat(FLOATS));
				glUniformMatrix2fv(getLocation(), transpose, buffer);
			}
		}
		public void set(Matrix2fc[] values, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(values.length * FLOATS);
				for(int i = 0; i < values.length; i++) {
					values[i].get(i * FLOATS, buffer);
				}
				glUniformMatrix2fv(getLocation(), transpose, buffer);
			}
		}
		public void set(float[] value, boolean transpose) {
			glUniformMatrix2fv(getLocation(), transpose, value);
		}
		public void set(FloatBuffer buffer, boolean transpose) {
			glUniformMatrix2fv(getLocation(), transpose, buffer);
		}
	}
	protected final Mat2fUniform uMat2f(String name) {return new Mat2fUniform(name);}
	public final class Mat3fUniform extends Uniform {
		static final int FLOATS = 9;
		Mat3fUniform(String name) {super(name);}
		public void set(Matrix3fc value, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = value.get(stack.mallocFloat(FLOATS));
				glUniformMatrix3fv(getLocation(), transpose, buffer);
			}
		}
		public void set(Matrix3fc[] values, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(values.length * FLOATS);
				for(int i = 0; i < values.length; i++) {
					values[i].get(i * FLOATS, buffer);
				}
				glUniformMatrix3fv(getLocation(), transpose, buffer);
			}
		}
		public void set(float[] value, boolean transpose) {
			glUniformMatrix3fv(getLocation(), transpose, value);
		}
		public void set(FloatBuffer buffer, boolean transpose) {
			glUniformMatrix3fv(getLocation(), transpose, buffer);
		}
	}
	protected final Mat3fUniform uMat3f(String name) {return new Mat3fUniform(name);}
	public final class Mat4fUniform extends Uniform {
		static final int FLOATS = 16;
		Mat4fUniform(String name) {super(name);}
		public void set(Matrix4fc value, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = value.get(stack.mallocFloat(FLOATS));
				glUniformMatrix4fv(getLocation(), transpose, buffer);
			}
		}
		public void set(Matrix4fc[] values, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(values.length * FLOATS);
				for(int i = 0; i < values.length; i++) {
					values[i].get(i * FLOATS, buffer);
				}
				glUniformMatrix4fv(getLocation(), transpose, buffer);
			}
		}
		public void set(float[] value, boolean transpose) {
			glUniformMatrix4fv(getLocation(), transpose, value);
		}
		public void set(FloatBuffer buffer, boolean transpose) {
			glUniformMatrix4fv(getLocation(), transpose, buffer);
		}
	}
	protected final Mat4fUniform uMat4f(String name) {return new Mat4fUniform(name);}
	// endregion

	// region non-square matrix uniforms
	public final class Mat3x2fUniform extends Uniform {
		static final int FLOATS = 6;
		Mat3x2fUniform(String name) {super(name);}
		public void set(Matrix3x2fc value, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = value.get(stack.mallocFloat(FLOATS));
				glUniformMatrix3x2fv(getLocation(), transpose, buffer);
			}
		}
		public void set(Matrix3x2fc[] values, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(values.length * FLOATS);
				for(int i = 0; i < values.length; i++) {
					values[i].get(i * FLOATS, buffer);
				}
				glUniformMatrix3x2fv(getLocation(), transpose, buffer);
			}
		}
		public void set(float[] value, boolean transpose) {
			glUniformMatrix3x2fv(getLocation(), transpose, value);
		}
		public void set(FloatBuffer buffer, boolean transpose) {
			glUniformMatrix3x2fv(getLocation(), transpose, buffer);
		}
	}
	protected final Mat3x2fUniform uMat3x2f(String name) {return new Mat3x2fUniform(name);}

	public final class Mat4x3fUniform extends Uniform {
		static final int FLOATS = 12;
		Mat4x3fUniform(String name) {super(name);}
		public void set(Matrix4x3fc value, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = value.get(stack.mallocFloat(FLOATS));
				glUniformMatrix4x3fv(getLocation(), transpose, buffer);
			}
		}
		public void set(Matrix4x3fc[] values, boolean transpose) {
			try(MemoryStack stack = stackPush()) {
				FloatBuffer buffer = stack.mallocFloat(values.length * FLOATS);
				for(int i = 0; i < values.length; i++) {
					values[i].get(i * FLOATS, buffer);
				}
				glUniformMatrix4x3fv(getLocation(), transpose, buffer);
			}
		}
		public void set(float[] value, boolean transpose) {
			glUniformMatrix4x3fv(getLocation(), transpose, value);
		}
		public void set(FloatBuffer buffer, boolean transpose) {
			glUniformMatrix4x3fv(getLocation(), transpose, buffer);
		}
	}
	protected final Mat4x3fUniform uMat4x3f(String name) {return new Mat4x3fUniform(name);}
	// endregion
}