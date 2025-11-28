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

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap;
import me.thosea.celestialgdx.window.Window;

import static org.lwjgl.glfw.GLFW.glfwExtensionSupported;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;

/**
 * CelestialGDX - this class will be removed soon!
 */
@Deprecated(forRemoval = true)
public class Lwjgl3Graphics implements Graphics {
	final Window window;
	private int backBufferWidth;
	private int backBufferHeight;

	public Lwjgl3Graphics(Window window) {
		this.window = window;
		updateFramebufferInfo();
	}

	public Window getWindow() {
		return window;
	}

	void updateFramebufferInfo() {
		int[] width = new int[1];
		int[] height = new int[1];
		glfwGetFramebufferSize(window.handle, width, height);
		this.backBufferWidth = width[0];
		this.backBufferHeight = height[0];
	}

	@Override
	public int getWidth() {
		return backBufferWidth;
	}

	@Override
	public int getHeight() {
		return backBufferHeight;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return glfwExtensionSupported(extension);
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		return new Lwjgl3Cursor(getWindow(), pixmap, xHotspot, yHotspot);
	}

	@Override
	public void setCursor(Cursor cursor) {
		glfwSetCursor(getWindow().handle, ((Lwjgl3Cursor) cursor).glfwCursor);
	}

	@Override
	public void setSystemCursor(SystemCursor systemCursor) {
		Lwjgl3Cursor.setSystemCursor(getWindow().handle, systemCursor);
	}
}