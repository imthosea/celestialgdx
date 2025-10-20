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

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.opengles.GLES32;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

class Lwjgl3GL30 extends Lwjgl3GL20 implements GL30 {
	@Override
	public void glReadBuffer(int mode) {
		GLES32.glReadBuffer(mode);
	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices) {
		if(indices instanceof ByteBuffer)
			GLES32.glDrawRangeElements(mode, start, end, (ByteBuffer) indices);
		else if(indices instanceof ShortBuffer)
			GLES32.glDrawRangeElements(mode, start, end, (ShortBuffer) indices);
		else if(indices instanceof IntBuffer)
			GLES32.glDrawRangeElements(mode, start, end, (IntBuffer) indices);
		else
			throw new GdxRuntimeException("indices must be byte, short or int buffer");
	}

	@Override
	public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset) {
		GLES32.glDrawRangeElements(mode, start, end, count, type, offset);
	}

	@Override
	public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type,
	                         int offset) {
		GLES32.glTexImage2D(target, level, internalformat, width, height, border, format, type, offset);
	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format,
	                         int type, Buffer pixels) {
		if(pixels == null)
			GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer) null);
		else if(pixels instanceof ByteBuffer)
			GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer) pixels);
		else if(pixels instanceof ShortBuffer)
			GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ShortBuffer) pixels);
		else if(pixels instanceof IntBuffer)
			GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (IntBuffer) pixels);
		else if(pixels instanceof FloatBuffer)
			GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (FloatBuffer) pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	@Override
	public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format,
	                         int type, int offset) {
		GLES32.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

	@Override
	public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
	                            int offset) {
		GLES32.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, offset);
	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
	                            int format, int type, Buffer pixels) {
		if(pixels instanceof ByteBuffer)
			GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ByteBuffer) pixels);
		else if(pixels instanceof ShortBuffer)
			GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ShortBuffer) pixels);
		else if(pixels instanceof IntBuffer)
			GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (IntBuffer) pixels);
		else if(pixels instanceof FloatBuffer)
			GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (FloatBuffer) pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
					+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	@Override
	public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
	                            int format, int type, int offset) {
		GLES32.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

	@Override
	public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
	                                int height) {
		GLES32.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	@Override
	public void glGenQueries(int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GLES32.glGenQueries();
		}
	}

	@Override
	public void glGenQueries(int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			ids.put(GLES32.glGenQueries());
		}
	}

	@Override
	public void glDeleteQueries(int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GLES32.glDeleteQueries(ids[i]);
		}
	}

	@Override
	public void glDeleteQueries(int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			GLES32.glDeleteQueries(ids.get());
		}
	}

	@Override
	public boolean glIsQuery(int id) {
		return GLES32.glIsQuery(id);
	}

	@Override
	public void glBeginQuery(int target, int id) {
		GLES32.glBeginQuery(target, id);
	}

	@Override
	public void glEndQuery(int target) {
		GLES32.glEndQuery(target);
	}

	@Override
	public void glGetQueryiv(int target, int pname, IntBuffer params) {
		GLES32.glGetQueryiv(target, pname, params);
	}

	@Override
	public void glGetQueryObjectuiv(int id, int pname, IntBuffer params) {
		GLES32.glGetQueryObjectuiv(id, pname, params);
	}

	@Override
	public boolean glUnmapBuffer(int target) {
		return GLES32.glUnmapBuffer(target);
	}

	@Override
	public Buffer glGetBufferPointerv(int target, int pname) {
		// FIXME glGetBufferPointerv needs a proper translation
		// return GLES32.glGetBufferPointer(target, pname);
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void glDrawBuffers(int n, IntBuffer bufs) {
		int limit = bufs.limit();
		((Buffer) bufs).limit(n);
		GLES32.glDrawBuffers(bufs);
		((Buffer) bufs).limit(limit);
	}

	@Override
	public void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix2x3fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix3x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix2x4fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix4x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix3x4fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value) {
		GLES32.glUniformMatrix4x3fv(location, transpose, value);
	}

	@Override
	public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
	                              int mask, int filter) {
		GLES32.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public void glBindFramebuffer(int target, int framebuffer) {
		GLES32.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer(int target, int renderbuffer) {
		GLES32.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public int glCheckFramebufferStatus(int target) {
		return GLES32.glCheckFramebufferStatus(target);
	}

	@Override
	public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
		GLES32.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteFramebuffer(int framebuffer) {
		GLES32.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
		GLES32.glDeleteRenderbuffers(renderbuffers);
	}

	@Override
	public void glDeleteRenderbuffer(int renderbuffer) {
		GLES32.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void glGenerateMipmap(int target) {
		GLES32.glGenerateMipmap(target);
	}

	@Override
	public void glGenFramebuffers(int n, IntBuffer framebuffers) {
		GLES32.glGenFramebuffers(framebuffers);
	}

	@Override
	public int glGenFramebuffer() {
		return GLES32.glGenFramebuffers();
	}

	@Override
	public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
		GLES32.glGenRenderbuffers(renderbuffers);
	}

	@Override
	public int glGenRenderbuffer() {
		return GLES32.glGenRenderbuffers();
	}

	@Override
	public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public boolean glIsFramebuffer(int framebuffer) {
		return GLES32.glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsRenderbuffer(int renderbuffer) {
		return GLES32.glIsRenderbuffer(renderbuffer);
	}

	@Override
	public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
		GLES32.glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {
		GLES32.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	@Override
	public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		GLES32.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLES32.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
		GLES32.glFramebufferTextureLayer(target, attachment, texture, level, layer);
	}

	@Override
	public java.nio.Buffer glMapBufferRange(int target, int offset, int length, int access) {
		return GLES32.glMapBufferRange(target, offset, length, access, null);
	}

	@Override
	public void glFlushMappedBufferRange(int target, int offset, int length) {
		GLES32.glFlushMappedBufferRange(target, offset, length);
	}

	@Override
	public void glBindVertexArray(int array) {
		GLES32.glBindVertexArray(array);
	}

	@Override
	public void glDeleteVertexArrays(int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GLES32.glDeleteVertexArrays(arrays[i]);
		}
	}

	@Override
	public void glDeleteVertexArrays(int n, IntBuffer arrays) {
		GLES32.glDeleteVertexArrays(arrays);
	}

	@Override
	public void glGenVertexArrays(int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			arrays[i] = GLES32.glGenVertexArrays();
		}
	}

	@Override
	public void glGenVertexArrays(int n, IntBuffer arrays) {
		GLES32.glGenVertexArrays(arrays);
	}

	@Override
	public boolean glIsVertexArray(int array) {
		return GLES32.glIsVertexArray(array);
	}

	@Override
	public void glBeginTransformFeedback(int primitiveMode) {
		GLES32.glBeginTransformFeedback(primitiveMode);
	}

	@Override
	public void glEndTransformFeedback() {
		GLES32.glEndTransformFeedback();
	}

	@Override
	public void glBindBufferRange(int target, int index, int buffer, int offset, int size) {
		GLES32.glBindBufferRange(target, index, buffer, offset, size);
	}

	@Override
	public void glBindBufferBase(int target, int index, int buffer) {
		GLES32.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode) {
		GLES32.glTransformFeedbackVaryings(program, varyings, bufferMode);
	}

	@Override
	public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset) {
		GLES32.glVertexAttribIPointer(index, size, type, stride, offset);
	}

	@Override
	public void glGetVertexAttribIiv(int index, int pname, IntBuffer params) {
		GLES32.glGetVertexAttribIiv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params) {
		GLES32.glGetVertexAttribIuiv(index, pname, params);
	}

	@Override
	public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
		GLES32.glVertexAttribI4i(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
		GLES32.glVertexAttribI4ui(index, x, y, z, w);
	}

	@Override
	public void glGetUniformuiv(int program, int location, IntBuffer params) {
		GLES32.glGetUniformuiv(program, location, params);
	}

	@Override
	public int glGetFragDataLocation(int program, String name) {
		return GLES32.glGetFragDataLocation(program, name);
	}

	@Override
	public void glUniform1uiv(int location, int count, IntBuffer value) {
		GLES32.glUniform1uiv(location, value);
	}

	@Override
	public void glUniform3uiv(int location, int count, IntBuffer value) {
		GLES32.glUniform3uiv(location, value);
	}

	@Override
	public void glUniform4uiv(int location, int count, IntBuffer value) {
		GLES32.glUniform4uiv(location, value);
	}

	@Override
	public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value) {
		GLES32.glClearBufferiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value) {
		GLES32.glClearBufferuiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value) {
		GLES32.glClearBufferfv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {
		GLES32.glClearBufferfi(buffer, drawbuffer, depth, stencil);
	}

	@Override
	public String glGetStringi(int name, int index) {
		return GLES32.glGetStringi(name, index);
	}

	@Override
	public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		GLES32.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	@Override
	public void glGetUniformIndices(int program, String[] uniformNames, IntBuffer uniformIndices) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		GLES32.glGetActiveUniformsiv(program, uniformIndices, pname, params);
	}

	@Override
	public int glGetUniformBlockIndex(int program, String uniformBlockName) {
		return GLES32.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
	public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params) {
		GLES32.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
	}

	@Override
	public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		GLES32.glGetActiveUniformBlockName(program, uniformBlockIndex, (IntBuffer) length, (ByteBuffer) uniformBlockName);
	}

	@Override
	public String glGetActiveUniformBlockName(int program, int uniformBlockIndex) {
		return GLES32.glGetActiveUniformBlockName(program, uniformBlockIndex, 1024);
	}

	@Override
	public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
		GLES32.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount) {
		GLES32.glDrawArraysInstanced(mode, first, count, instanceCount);
	}

	@Override
	public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount) {
		GLES32.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);

	}

	@Override
	public void glGetInteger64v(int pname, LongBuffer params) {
		GLES32.glGetInteger64v(pname, params);
	}

	@Override
	public void glGetBufferParameteri64v(int target, int pname, LongBuffer params) {
		params.put(GLES32.glGetBufferParameteri64(target, pname));
	}

	@Override
	public void glGenSamplers(int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			samplers[i] = GLES32.glGenSamplers();
		}
	}

	@Override
	public void glGenSamplers(int count, IntBuffer samplers) {
		GLES32.glGenSamplers(samplers);
	}

	@Override
	public void glDeleteSamplers(int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			GLES32.glDeleteSamplers(samplers[i]);
		}
	}

	@Override
	public void glDeleteSamplers(int count, IntBuffer samplers) {
		GLES32.glDeleteSamplers(samplers);
	}

	@Override
	public boolean glIsSampler(int sampler) {
		return GLES32.glIsSampler(sampler);
	}

	@Override
	public void glBindSampler(int unit, int sampler) {
		GLES32.glBindSampler(unit, sampler);
	}

	@Override
	public void glSamplerParameteri(int sampler, int pname, int param) {
		GLES32.glSamplerParameteri(sampler, pname, param);
	}

	@Override
	public void glSamplerParameteriv(int sampler, int pname, IntBuffer param) {
		GLES32.glSamplerParameteriv(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterf(int sampler, int pname, float param) {
		GLES32.glSamplerParameterf(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param) {
		GLES32.glSamplerParameterfv(sampler, pname, param);
	}

	@Override
	public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params) {
		GLES32.glGetSamplerParameterIiv(sampler, pname, params);
	}

	@Override
	public void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params) {
		GLES32.glGetSamplerParameterfv(sampler, pname, params);
	}

	@Override
	public void glVertexAttribDivisor(int index, int divisor) {
		GLES32.glVertexAttribDivisor(index, divisor);
	}

	@Override
	public void glBindTransformFeedback(int target, int id) {
		GLES32.glBindTransformFeedback(target, id);
	}

	@Override
	public void glDeleteTransformFeedbacks(int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GLES32.glDeleteTransformFeedbacks(ids[i]);
		}
	}

	@Override
	public void glDeleteTransformFeedbacks(int n, IntBuffer ids) {
		GLES32.glDeleteTransformFeedbacks(ids);
	}

	@Override
	public void glGenTransformFeedbacks(int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GLES32.glGenTransformFeedbacks();
		}
	}

	@Override
	public void glGenTransformFeedbacks(int n, IntBuffer ids) {
		GLES32.glGenTransformFeedbacks(ids);
	}

	@Override
	public boolean glIsTransformFeedback(int id) {
		return GLES32.glIsTransformFeedback(id);
	}

	@Override
	public void glPauseTransformFeedback() {
		GLES32.glPauseTransformFeedback();
	}

	@Override
	public void glResumeTransformFeedback() {
		GLES32.glResumeTransformFeedback();
	}

	@Override
	public void glProgramParameteri(int program, int pname, int value) {
		GLES32.glProgramParameteri(program, pname, value);
	}

	@Override
	public void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments) {
		GLES32.glInvalidateFramebuffer(target, attachments);
	}

	@Override
	public void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
	                                       int height) {
		GLES32.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
	}
}