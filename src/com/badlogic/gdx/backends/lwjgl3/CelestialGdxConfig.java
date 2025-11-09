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

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.log.GdxLoggerFactory;
import com.badlogic.gdx.log.PrintLogger;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.function.Supplier;

public class CelestialGdxConfig {
	public int glMajorVersion = 3;
	public int glMinorVersion = 3;

	public String title = "game";
	public int windowWidth = 640;
	public int windowHeight = 540;
	public boolean windowResizable = true;
	public boolean windowDecorated = true;
	public boolean maximized = false;

	public boolean autoIconify = true;
	public boolean windowVisible = true;
	public boolean vsync = true;
	public Pixmap[] icon;

	/** Default window listener for new windows. Can still be changed for individual windows. */
	public WindowListener defaultWindowListener = new WindowListener() {
		@Override public void minimized(Window window, boolean isMinimized) {}
		@Override public void maximized(Window window, boolean isMaximized) {}
		@Override public void resized(Window window, int width, int height) {}
		@Override
		public void closeRequested(Window window) {
			window.gdx.markShouldClose();
		}
	};
	public GdxLoggerFactory loggerFactory = PrintLogger::new;

	public boolean debugContext = false;

	public int r = 8, g = 8, b = 8, a = 8;
	public int depth = 16, stencil = 8;
	public int samples = 0;
	public boolean transparentFramebuffer;

	/**
	 * Supplier for error stream. Only called once. Defaults to {@link GLFWErrorCallback#createPrint()}
	 * going to {@link System#err}
	 */
	public Supplier<GLFWErrorCallback> errorCallbackSupplier = () -> GLFWErrorCallback.createPrint(System.err);
}