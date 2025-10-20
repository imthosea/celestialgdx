/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.utils.BufferUtils;
import org.lwjgl.opengles.GLES32;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Lwjgl3GL31 extends Lwjgl3GL30 implements GL31 {

	private final static ByteBuffer tmpByteBuffer = BufferUtils.newByteBuffer(16);

	@Override
	public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
		GLES32.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
	}

	@Override
	public void glDispatchComputeIndirect(long indirect) {
		GLES32.glDispatchComputeIndirect(indirect);
	}

	@Override
	public void glDrawArraysIndirect(int mode, long indirect) {
		GLES32.glDrawArraysIndirect(mode, indirect);
	}

	@Override
	public void glDrawElementsIndirect(int mode, int type, long indirect) {
		GLES32.glDrawElementsIndirect(mode, type, indirect);
	}

	@Override
	public void glFramebufferParameteri(int target, int pname, int param) {
		GLES32.glFramebufferParameteri(target, pname, param);
	}

	@Override
	public void glGetFramebufferParameteriv(int target, int pname, IntBuffer params) {
		GLES32.glGetFramebufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params) {
		GLES32.glGetProgramInterfaceiv(program, programInterface, pname, params);
	}

	@Override
	public int glGetProgramResourceIndex(int program, int programInterface, String name) {
		return GLES32.glGetProgramResourceIndex(program, programInterface, name);
	}

	@Override
	public String glGetProgramResourceName(int program, int programInterface, int index) {
		return GLES32.glGetProgramResourceName(program, programInterface, index);
	}

	@Override
	public void glGetProgramResourceiv(int program, int programInterface, int index, IntBuffer props, IntBuffer length,
	                                   IntBuffer params) {
		GLES32.glGetProgramResourceiv(program, programInterface, index, props, length, params);
	}

	@Override
	public int glGetProgramResourceLocation(int program, int programInterface, String name) {
		return GLES32.glGetProgramResourceLocation(program, programInterface, name);
	}

	@Override
	public void glUseProgramStages(int pipeline, int stages, int program) {
		GLES32.glUseProgramStages(pipeline, stages, program);
	}

	@Override
	public void glActiveShaderProgram(int pipeline, int program) {
		GLES32.glActiveShaderProgram(pipeline, program);
	}

	@Override
	public int glCreateShaderProgramv(int type, String[] strings) {
		return GLES32.glCreateShaderProgramv(type, strings);
	}

	@Override
	public void glBindProgramPipeline(int pipeline) {
		GLES32.glBindProgramPipeline(pipeline);
	}

	@Override
	public void glDeleteProgramPipelines(int count, IntBuffer pipelines) {
		int oldLimit = pipelines.limit();
		pipelines.limit(count);
		GLES32.glDeleteProgramPipelines(pipelines);
		pipelines.limit(oldLimit);
	}

	@Override
	public void glGenProgramPipelines(int count, IntBuffer pipelines) {
		int oldLimit = pipelines.limit();
		pipelines.limit(count);
		GLES32.glGenProgramPipelines(pipelines);
		pipelines.limit(oldLimit);
	}

	@Override
	public boolean glIsProgramPipeline(int pipeline) {
		return GLES32.glIsProgramPipeline(pipeline);
	}

	@Override
	public void glGetProgramPipelineiv(int pipeline, int pname, IntBuffer params) {
		GLES32.glGetProgramPipelineiv(pipeline, pname, params);
	}

	@Override
	public void glProgramUniform1i(int program, int location, int v0) {
		GLES32.glProgramUniform1i(program, location, v0);
	}

	@Override
	public void glProgramUniform2i(int program, int location, int v0, int v1) {
		GLES32.glProgramUniform2i(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3i(int program, int location, int v0, int v1, int v2) {
		GLES32.glProgramUniform3i(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4i(int program, int location, int v0, int v1, int v2, int v3) {
		GLES32.glProgramUniform4i(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1ui(int program, int location, int v0) {
		GLES32.glProgramUniform1ui(program, location, v0);
	}

	@Override
	public void glProgramUniform2ui(int program, int location, int v0, int v1) {
		GLES32.glProgramUniform2ui(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3ui(int program, int location, int v0, int v1, int v2) {
		GLES32.glProgramUniform3ui(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4ui(int program, int location, int v0, int v1, int v2, int v3) {
		GLES32.glProgramUniform4ui(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1f(int program, int location, float v0) {
		GLES32.glProgramUniform1f(program, location, v0);
	}

	@Override
	public void glProgramUniform2f(int program, int location, float v0, float v1) {
		GLES32.glProgramUniform2f(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3f(int program, int location, float v0, float v1, float v2) {
		GLES32.glProgramUniform3f(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4f(int program, int location, float v0, float v1, float v2, float v3) {
		GLES32.glProgramUniform4f(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1iv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform1iv(program, location, value);
	}

	@Override
	public void glProgramUniform2iv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform2iv(program, location, value);
	}

	@Override
	public void glProgramUniform3iv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform3iv(program, location, value);
	}

	@Override
	public void glProgramUniform4iv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform4iv(program, location, value);
	}

	@Override
	public void glProgramUniform1uiv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform1uiv(program, location, value);
	}

	@Override
	public void glProgramUniform2uiv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform2uiv(program, location, value);
	}

	@Override
	public void glProgramUniform3uiv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform3uiv(program, location, value);
	}

	@Override
	public void glProgramUniform4uiv(int program, int location, IntBuffer value) {
		GLES32.glProgramUniform4uiv(program, location, value);
	}

	@Override
	public void glProgramUniform1fv(int program, int location, FloatBuffer value) {
		GLES32.glProgramUniform1fv(program, location, value);
	}

	@Override
	public void glProgramUniform2fv(int program, int location, FloatBuffer value) {
		GLES32.glProgramUniform2fv(program, location, value);
	}

	@Override
	public void glProgramUniform3fv(int program, int location, FloatBuffer value) {
		GLES32.glProgramUniform3fv(program, location, value);
	}

	@Override
	public void glProgramUniform4fv(int program, int location, FloatBuffer value) {
		GLES32.glProgramUniform4fv(program, location, value);
	}

	@Override
	public void glProgramUniformMatrix2fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix2fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix3fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix4fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix2x3fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix2x3fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3x2fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix3x2fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix2x4fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix2x4fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4x2fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix4x2fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3x4fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix3x4fv(program, location, transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4x3fv(int program, int location, boolean transpose, FloatBuffer value) {
		GLES32.glProgramUniformMatrix4x3fv(program, location, transpose, value);
	}

	@Override
	public void glValidateProgramPipeline(int pipeline) {
		GLES32.glValidateProgramPipeline(pipeline);
	}

	@Override
	public String glGetProgramPipelineInfoLog(int program) {
		return GLES32.glGetProgramPipelineInfoLog(program);
	}

	@Override
	public void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		GLES32.glBindImageTexture(unit, texture, level, layered, layer, access, format);
	}

	@Override
	public void glGetBooleani_v(int target, int index, IntBuffer data) {
		GLES32.glGetBooleani_v(target, index, tmpByteBuffer);
		data.put(tmpByteBuffer.asIntBuffer());
	}

	@Override
	public void glMemoryBarrier(int barriers) {
		GLES32.glMemoryBarrier(barriers);
	}

	@Override
	public void glMemoryBarrierByRegion(int barriers) {
		GLES32.glMemoryBarrierByRegion(barriers);
	}

	@Override
	public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height,
	                                      boolean fixedsamplelocations) {
		GLES32.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
	}

	@Override
	public void glGetMultisamplefv(int pname, int index, FloatBuffer val) {
		GLES32.glGetMultisamplefv(pname, index, val);
	}

	@Override
	public void glSampleMaski(int maskNumber, int mask) {
		GLES32.glSampleMaski(maskNumber, mask);
	}

	@Override
	public void glGetTexLevelParameteriv(int target, int level, int pname, IntBuffer params) {
		GLES32.glGetTexLevelParameteriv(target, level, pname, params);
	}

	@Override
	public void glGetTexLevelParameterfv(int target, int level, int pname, FloatBuffer params) {
		GLES32.glGetTexLevelParameterfv(target, level, pname, params);
	}

	@Override
	public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
		GLES32.glBindVertexBuffer(bindingindex, buffer, offset, stride);
	}

	@Override
	public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
		GLES32.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
	}

	@Override
	public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
		GLES32.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
	}

	@Override
	public void glVertexAttribBinding(int attribindex, int bindingindex) {
		GLES32.glVertexAttribBinding(attribindex, bindingindex);
	}

	@Override
	public void glVertexBindingDivisor(int bindingindex, int divisor) {
		GLES32.glVertexBindingDivisor(bindingindex, divisor);
	}

}