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

package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.opengles.GLES32;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

class Lwjgl3GL20 implements GL20 {
	private ByteBuffer buffer = null;
	private FloatBuffer floatBuffer = null;
	private IntBuffer intBuffer = null;

	private void ensureBufferCapacity(int numBytes) {
		if(buffer == null || buffer.capacity() < numBytes) {
			buffer = BufferUtils.newByteBuffer(numBytes);
			floatBuffer = buffer.asFloatBuffer();
			intBuffer = buffer.asIntBuffer();
		}
	}

	private FloatBuffer toFloatBuffer(float v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		((Buffer) floatBuffer).clear();
		((Buffer) floatBuffer).limit(count);
		floatBuffer.put(v, offset, count);
		((Buffer) floatBuffer).position(0);
		return floatBuffer;
	}

	private IntBuffer toIntBuffer(int v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		((Buffer) intBuffer).clear();
		((Buffer) intBuffer).limit(count);
		intBuffer.put(v, offset, count);
		((Buffer) intBuffer).position(0);
		return intBuffer;
	}

	public void glActiveTexture(int texture) {
		GLES32.glActiveTexture(texture);
	}

	public void glAttachShader(int program, int shader) {
		GLES32.glAttachShader(program, shader);
	}

	public void glBindAttribLocation(int program, int index, String name) {
		GLES32.glBindAttribLocation(program, index, name);
	}

	public void glBindBuffer(int target, int buffer) {
		GLES32.glBindBuffer(target, buffer);
	}

	public void glBindFramebuffer(int target, int framebuffer) {
		GLES32.glBindFramebuffer(target, framebuffer);
	}

	public void glBindRenderbuffer(int target, int renderbuffer) {
		GLES32.glBindFramebuffer(target, renderbuffer);
	}

	public void glBindTexture(int target, int texture) {
		GLES32.glBindTexture(target, texture);
	}

	public void glBlendColor(float red, float green, float blue, float alpha) {
		GLES32.glBlendColor(red, green, blue, alpha);
	}

	public void glBlendEquation(int mode) {
		GLES32.glBlendEquation(mode);
	}

	public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
		GLES32.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFunc(int sfactor, int dfactor) {
		GLES32.glBlendFunc(sfactor, dfactor);
	}

	public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLES32.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public void glBufferData(int target, int size, Buffer data, int usage) {
		if(data == null)
			GLES32.glBufferData(target, size, usage);
		else if(data instanceof ByteBuffer)
			GLES32.glBufferData(target, (ByteBuffer) data, usage);
		else if(data instanceof IntBuffer)
			GLES32.glBufferData(target, (IntBuffer) data, usage);
		else if(data instanceof FloatBuffer)
			GLES32.glBufferData(target, (FloatBuffer) data, usage);
		else if(data instanceof ShortBuffer) //
			GLES32.glBufferData(target, (ShortBuffer) data, usage);
	}

	public void glBufferSubData(int target, int offset, int size, Buffer data) {
		if(data == null)
			throw new GdxRuntimeException("Using null for the data not possible, blame LWJGL");
		else if(data instanceof ByteBuffer)
			GLES32.glBufferSubData(target, offset, (ByteBuffer) data);
		else if(data instanceof IntBuffer)
			GLES32.glBufferSubData(target, offset, (IntBuffer) data);
		else if(data instanceof FloatBuffer)
			GLES32.glBufferSubData(target, offset, (FloatBuffer) data);
		else if(data instanceof ShortBuffer) //
			GLES32.glBufferSubData(target, offset, (ShortBuffer) data);
	}

	public int glCheckFramebufferStatus(int target) {
		return GLES32.glCheckFramebufferStatus(target);
	}

	public void glClear(int mask) {
		GLES32.glClear(mask);
	}

	public void glClearColor(float red, float green, float blue, float alpha) {
		GLES32.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf(float depth) {
		GLES32.glClearDepthf(depth);
	}

	public void glClearStencil(int s) {
		GLES32.glClearStencil(s);
	}

	public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		GLES32.glColorMask(red, green, blue, alpha);
	}

	public void glCompileShader(int shader) {
		GLES32.glCompileShader(shader);
	}

	public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
	                                   int imageSize, Buffer data) {
		if(data instanceof ByteBuffer) {
			GLES32.glCompressedTexImage2D(target, level, internalformat, width, height, border, (ByteBuffer) data);
		} else {
			throw new GdxRuntimeException("Can't use " + data.getClass().getName() + " with this method. Use ByteBuffer instead.");
		}
	}

	public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
	                                      int imageSize, Buffer data) {
		throw new GdxRuntimeException("not implemented");
	}

	public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GLES32.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GLES32.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public int glCreateProgram() {
		return GLES32.glCreateProgram();
	}

	public int glCreateShader(int type) {
		return GLES32.glCreateShader(type);
	}

	public void glCullFace(int mode) {
		GLES32.glCullFace(mode);
	}

	public void glDeleteBuffers(int n, IntBuffer buffers) {
		GLES32.glDeleteBuffers(buffers);
	}

	@Override
	public void glDeleteBuffer(int buffer) {
		GLES32.glDeleteBuffers(buffer);
	}

	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		GLES32.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteFramebuffer(int framebuffer) {
		GLES32.glDeleteFramebuffers(framebuffer);
	}

	public void glDeleteProgram(int program) {
		GLES32.glDeleteProgram(program);
	}

	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		GLES32.glDeleteRenderbuffers(renderbuffers);
	}

	public void glDeleteRenderbuffer(int renderbuffer) {
		GLES32.glDeleteRenderbuffers(renderbuffer);
	}

	public void glDeleteShader(int shader) {
		GLES32.glDeleteShader(shader);
	}

	public void glDeleteTextures(int n, IntBuffer textures) {
		GLES32.glDeleteTextures(textures);
	}

	@Override
	public void glDeleteTexture(int texture) {
		GLES32.glDeleteTextures(texture);
	}

	public void glDepthFunc(int func) {
		GLES32.glDepthFunc(func);
	}

	public void glDepthMask(boolean flag) {
		GLES32.glDepthMask(flag);
	}

	public void glDepthRangef(float zNear, float zFar) {
		GLES32.glDepthRangef(zNear, zFar);
	}

	public void glDetachShader(int program, int shader) {
		GLES32.glDetachShader(program, shader);
	}

	public void glDisable(int cap) {
		GLES32.glDisable(cap);
	}

	public void glDisableVertexAttribArray(int index) {
		GLES32.glDisableVertexAttribArray(index);
	}

	public void glDrawArrays(int mode, int first, int count) {
		GLES32.glDrawArrays(mode, first, count);
	}

	public void glDrawElements(int mode, int count, int type, Buffer indices) {
		if(indices instanceof ShortBuffer sb && type == GLES32.GL_UNSIGNED_SHORT) {
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			GLES32.glDrawElements(mode, sb);
			sb.limit(oldLimit);
		} else if(indices instanceof ByteBuffer && type == GLES32.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = ((ByteBuffer) indices).asShortBuffer();
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			GLES32.glDrawElements(mode, sb);
			sb.limit(oldLimit);
		} else if(indices instanceof ByteBuffer bb && type == GLES32.GL_UNSIGNED_BYTE) {
			int position = bb.position();
			int oldLimit = bb.limit();
			bb.limit(position + count);
			GLES32.glDrawElements(mode, bb);
			bb.limit(oldLimit);
		} else
			throw new GdxRuntimeException("Can't use " + indices.getClass().getName()
					+ " with this method. Use ShortBuffer or ByteBuffer instead. Blame LWJGL");
	}

	public void glEnable(int cap) {
		GLES32.glEnable(cap);
	}

	public void glEnableVertexAttribArray(int index) {
		GLES32.glEnableVertexAttribArray(index);
	}

	public void glFinish() {
		GLES32.glFinish();
	}

	public void glFlush() {
		GLES32.glFlush();
	}

	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLES32.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GLES32.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	public void glFrontFace(int mode) {
		GLES32.glFrontFace(mode);
	}

	public void glGenBuffers(int n, IntBuffer buffers) {
		GLES32.glGenBuffers(buffers);
	}

	public int glGenBuffer() {
		return GLES32.glGenBuffers();
	}

	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		GLES32.glGenFramebuffers(framebuffers);
	}

	public int glGenFramebuffer() {
		return GLES32.glGenFramebuffers();
	}

	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		GLES32.glGenRenderbuffers(renderbuffers);
	}

	public int glGenRenderbuffer() {
		return GLES32.glGenRenderbuffers();
	}

	public void glGenTextures(int n, IntBuffer textures) {
		GLES32.glGenTextures(textures);
	}

	public int glGenTexture() {
		return GLES32.glGenTextures();
	}

	public void glGenerateMipmap(int target) {
		GLES32.glGenerateMipmap(target);
	}

	public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type) {
		return GLES32.glGetActiveAttrib(program, index, 256, size, type);
	}

	public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type) {
		return GLES32.glGetActiveUniform(program, index, 256, size, type);
	}

	public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {
		GLES32.glGetAttachedShaders(program, (IntBuffer) count, shaders);
	}

	public int glGetAttribLocation(int program, String name) {
		return GLES32.glGetAttribLocation(program, name);
	}

	public void glGetBooleanv(int pname, Buffer params) {
		GLES32.glGetBooleanv(pname, (ByteBuffer) params);
	}

	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glGetBufferParameteriv(target, pname, params);
	}

	public int glGetError() {
		return GLES32.glGetError();
	}

	public void glGetFloatv(int pname, FloatBuffer params) {
		GLES32.glGetFloatv(pname, params);
	}

	public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
		GLES32.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	public void glGetIntegerv(int pname, IntBuffer params) {
		GLES32.glGetIntegerv(pname, params);
	}

	public String glGetProgramInfoLog(int program) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLES32.glGetProgramInfoLog(program, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	public void glGetProgramiv(int program, int pname, IntBuffer params) {
		GLES32.glGetProgramiv(program, pname, params);
	}

	public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glGetRenderbufferParameteriv(target, pname, params);
	}

	public String glGetShaderInfoLog(int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLES32.glGetShaderInfoLog(shader, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glGetShaderiv(int shader, int pname, IntBuffer params) {
		GLES32.glGetShaderiv(shader, pname, params);
	}

	public String glGetString(int name) {
		return GLES32.glGetString(name);
	}

	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
		GLES32.glGetTexParameterfv(target, pname, params);
	}

	public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glGetTexParameteriv(target, pname, params);
	}

	public int glGetUniformLocation(int program, String name) {
		return GLES32.glGetUniformLocation(program, name);
	}

	public void glGetUniformfv(int program, int location, FloatBuffer params) {
		GLES32.glGetUniformfv(program, location, params);
	}

	public void glGetUniformiv(int program, int location, IntBuffer params) {
		GLES32.glGetUniformiv(program, location, params);
	}

	public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
		GLES32.glGetVertexAttribfv(index, pname, params);
	}

	public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
		GLES32.glGetVertexAttribiv(index, pname, params);
	}

	public void glHint(int target, int mode) {
		GLES32.glHint(target, mode);
	}

	public boolean glIsBuffer(int buffer) {
		return GLES32.glIsBuffer(buffer);
	}

	public boolean glIsEnabled(int cap) {
		return GLES32.glIsEnabled(cap);
	}

	public boolean glIsFramebuffer(int framebuffer) {
		return GLES32.glIsFramebuffer(framebuffer);
	}

	public boolean glIsProgram(int program) {
		return GLES32.glIsProgram(program);
	}

	public boolean glIsRenderbuffer(int renderbuffer) {
		return GLES32.glIsRenderbuffer(renderbuffer);
	}

	public boolean glIsShader(int shader) {
		return GLES32.glIsShader(shader);
	}

	public boolean glIsTexture(int texture) {
		return GLES32.glIsTexture(texture);
	}

	public void glLineWidth(float width) {
		GLES32.glLineWidth(width);
	}

	public void glLinkProgram(int program) {
		GLES32.glLinkProgram(program);
	}

	public void glPixelStorei(int pname, int param) {
		GLES32.glPixelStorei(pname, param);
	}

	public void glPolygonOffset(float factor, float units) {
		GLES32.glPolygonOffset(factor, units);
	}

	public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
		if(pixels instanceof ByteBuffer)
			GLES32.glReadPixels(x, y, width, height, format, type, (ByteBuffer) pixels);
		else if(pixels instanceof ShortBuffer)
			GLES32.glReadPixels(x, y, width, height, format, type, (ShortBuffer) pixels);
		else if(pixels instanceof IntBuffer)
			GLES32.glReadPixels(x, y, width, height, format, type, (IntBuffer) pixels);
		else if(pixels instanceof FloatBuffer)
			GLES32.glReadPixels(x, y, width, height, format, type, (FloatBuffer) pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL");
	}

	public void glReleaseShaderCompiler() {
		// nothing to do here
	}

	public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
		GLES32.glRenderbufferStorage(target, internalformat, width, height);
	}

	public void glSampleCoverage(float value, boolean invert) {
		GLES32.glSampleCoverage(value, invert);
	}

	public void glScissor(int x, int y, int width, int height) {
		GLES32.glScissor(x, y, width, height);
	}

	public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glShaderSource(int shader, String string) {
		GLES32.glShaderSource(shader, string);
	}

	public void glStencilFunc(int func, int ref, int mask) {
		GLES32.glStencilFunc(func, ref, mask);
	}

	public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
		GLES32.glStencilFuncSeparate(face, func, ref, mask);
	}

	public void glStencilMask(int mask) {
		GLES32.glStencilMask(mask);
	}

	public void glStencilMaskSeparate(int face, int mask) {
		GLES32.glStencilMaskSeparate(face, mask);
	}

	public void glStencilOp(int fail, int zfail, int zpass) {
		GLES32.glStencilOp(fail, zfail, zpass);
	}

	public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
		GLES32.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type,
	                         Buffer pixels) {
		if(pixels == null)
			GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer) null);
		else if(pixels instanceof ByteBuffer)
			GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer) pixels);
		else if(pixels instanceof ShortBuffer)
			GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ShortBuffer) pixels);
		else if(pixels instanceof IntBuffer)
			GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, (IntBuffer) pixels);
		else if(pixels instanceof FloatBuffer)
			GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, (FloatBuffer) pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glTexParameterf(int target, int pname, float param) {
		GLES32.glTexParameterf(target, pname, param);
	}

	public void glTexParameterfv(int target, int pname, FloatBuffer params) {
		GLES32.glTexParameterfv(target, pname, params);
	}

	public void glTexParameteri(int target, int pname, int param) {
		GLES32.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glTexParameteriv(target, pname, params);
	}

	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
	                            Buffer pixels) {
		if(pixels instanceof ByteBuffer)
			GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer) pixels);
		else if(pixels instanceof ShortBuffer)
			GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer) pixels);
		else if(pixels instanceof IntBuffer)
			GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer) pixels);
		else if(pixels instanceof FloatBuffer)
			GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer) pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glUniform1f(int location, float x) {
		GLES32.glUniform1f(location, x);
	}

	public void glUniform1fv(int location, int count, FloatBuffer v) {
		GLES32.glUniform1fv(location, v);
	}

	public void glUniform1fv(int location, int count, float[] v, int offset) {
		GLES32.glUniform1fv(location, toFloatBuffer(v, offset, count));
	}

	public void glUniform1i(int location, int x) {
		GLES32.glUniform1i(location, x);
	}

	public void glUniform1iv(int location, int count, IntBuffer v) {
		GLES32.glUniform1iv(location, v);
	}

	@Override
	public void glUniform1iv(int location, int count, int[] v, int offset) {
		GLES32.glUniform1iv(location, toIntBuffer(v, offset, count));
	}

	public void glUniform2f(int location, float x, float y) {
		GLES32.glUniform2f(location, x, y);
	}

	public void glUniform2fv(int location, int count, FloatBuffer v) {
		GLES32.glUniform2fv(location, v);
	}

	public void glUniform2fv(int location, int count, float[] v, int offset) {
		GLES32.glUniform2fv(location, toFloatBuffer(v, offset, count << 1));
	}

	public void glUniform2i(int location, int x, int y) {
		GLES32.glUniform2i(location, x, y);
	}

	public void glUniform2iv(int location, int count, IntBuffer v) {
		GLES32.glUniform2iv(location, v);
	}

	public void glUniform2iv(int location, int count, int[] v, int offset) {
		GLES32.glUniform2iv(location, toIntBuffer(v, offset, count << 1));
	}

	public void glUniform3f(int location, float x, float y, float z) {
		GLES32.glUniform3f(location, x, y, z);
	}

	public void glUniform3fv(int location, int count, FloatBuffer v) {
		GLES32.glUniform3fv(location, v);
	}

	public void glUniform3fv(int location, int count, float[] v, int offset) {
		GLES32.glUniform3fv(location, toFloatBuffer(v, offset, count * 3));
	}

	public void glUniform3i(int location, int x, int y, int z) {
		GLES32.glUniform3i(location, x, y, z);
	}

	public void glUniform3iv(int location, int count, IntBuffer v) {
		GLES32.glUniform3iv(location, v);
	}

	public void glUniform3iv(int location, int count, int[] v, int offset) {
		GLES32.glUniform3iv(location, toIntBuffer(v, offset, count * 3));
	}

	public void glUniform4f(int location, float x, float y, float z, float w) {
		GLES32.glUniform4f(location, x, y, z, w);
	}

	public void glUniform4fv(int location, int count, FloatBuffer v) {
		GLES32.glUniform4fv(location, v);
	}

	public void glUniform4fv(int location, int count, float[] v, int offset) {
		GLES32.glUniform4fv(location, toFloatBuffer(v, offset, count << 2));
	}

	public void glUniform4i(int location, int x, int y, int z, int w) {
		GLES32.glUniform4i(location, x, y, z, w);
	}

	public void glUniform4iv(int location, int count, IntBuffer v) {
		GLES32.glUniform4iv(location, v);
	}

	public void glUniform4iv(int location, int count, int[] v, int offset) {
		GLES32.glUniform4iv(location, toIntBuffer(v, offset, count << 2));
	}

	public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix2fv(location, transpose, value);
	}

	public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {
		GLES32.glUniformMatrix2fv(location, transpose, toFloatBuffer(value, offset, count << 2));
	}

	public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix3fv(location, transpose, value);
	}

	public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {
		GLES32.glUniformMatrix3fv(location, transpose, toFloatBuffer(value, offset, count * 9));
	}

	public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix4fv(location, transpose, value);
	}

	public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
		GLES32.glUniformMatrix4fv(location, transpose, toFloatBuffer(value, offset, count << 4));
	}

	public void glUseProgram(int program) {
		GLES32.glUseProgram(program);
	}

	public void glValidateProgram(int program) {
		GLES32.glValidateProgram(program);
	}

	public void glVertexAttrib1f(int indx, float x) {
		GLES32.glVertexAttrib1f(indx, x);
	}

	public void glVertexAttrib1fv(int indx, FloatBuffer values) {
		GLES32.glVertexAttrib1f(indx, values.get());
	}

	public void glVertexAttrib2f(int indx, float x, float y) {
		GLES32.glVertexAttrib2f(indx, x, y);
	}

	public void glVertexAttrib2fv(int indx, FloatBuffer values) {
		GLES32.glVertexAttrib2f(indx, values.get(), values.get());
	}

	public void glVertexAttrib3f(int indx, float x, float y, float z) {
		GLES32.glVertexAttrib3f(indx, x, y, z);
	}

	public void glVertexAttrib3fv(int indx, FloatBuffer values) {
		GLES32.glVertexAttrib3f(indx, values.get(), values.get(), values.get());
	}

	public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
		GLES32.glVertexAttrib4f(indx, x, y, z, w);
	}

	public void glVertexAttrib4fv(int indx, FloatBuffer values) {
		GLES32.glVertexAttrib4f(indx, values.get(), values.get(), values.get(), values.get());
	}

	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer buffer) {
		if(buffer instanceof ByteBuffer) {
			if(type == GL_BYTE)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer) buffer);
			else if(type == GL_UNSIGNED_BYTE)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer) buffer);
			else if(type == GL_SHORT)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer) buffer).asShortBuffer());
			else if(type == GL_UNSIGNED_SHORT)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer) buffer).asShortBuffer());
			else if(type == GL_FLOAT)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer) buffer).asFloatBuffer());
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
						+ " with this method. Use ByteBuffer and one of GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT or GL_FLOAT for type. Blame LWJGL");
		} else if(buffer instanceof FloatBuffer) {
			if(type == GL_FLOAT)
				GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, (FloatBuffer) buffer);
			else
				throw new GdxRuntimeException(
						"Can't use " + buffer.getClass().getName() + " with type " + type + " with this method.");
		} else
			throw new GdxRuntimeException(
					"Can't use " + buffer.getClass().getName() + " with this method. Use ByteBuffer instead. Blame LWJGL");
	}

	public void glViewport(int x, int y, int width, int height) {
		GLES32.glViewport(x, y, width, height);
	}

	public void glDrawElements(int mode, int count, int type, int indices) {
		GLES32.glDrawElements(mode, count, type, indices);
	}

	public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GLES32.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
}