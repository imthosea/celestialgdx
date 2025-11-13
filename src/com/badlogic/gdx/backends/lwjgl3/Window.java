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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import static org.lwjgl.glfw.GLFW.*;

public class Window implements Disposable {
	public final long handle;
	public final Lwjgl3Graphics graphics;
	public final Lwjgl3Input input;

	public final CelestialGdx gdx;
	public final CelestialGdxConfig config;

	@Nullable
	public WindowListener windowListener;

	private volatile boolean minimized = false;
	private volatile boolean focused = false;

	private final GLFWWindowFocusCallback focusCallback = GLFWWindowFocusCallback.create((handle, focused) -> {
		Window.this.focused = focused;
	});
	private final GLFWWindowIconifyCallback iconifyCallback = GLFWWindowIconifyCallback.create((handle, iconified) -> {
		if(windowListener != null) {
			windowListener.minimized(this, iconified);
		}
		Window.this.minimized = iconified;
	});
	private final GLFWWindowMaximizeCallback maximizeCallback = GLFWWindowMaximizeCallback.create((handle, maximized) -> {
		if(windowListener != null) {
			windowListener.maximized(this, maximized);
		}
	});
	private final GLFWWindowCloseCallback closeCallback = GLFWWindowCloseCallback.create(handle -> {
		if(windowListener != null) {
			windowListener.closeRequested(this);
		}
	});
	private final GLFWFramebufferSizeCallback resizeCallback = GLFWFramebufferSizeCallback.create((handle, width, height) -> {
		if(windowListener != null) {
			windowListener.resized(this, width, height);
		}
	});

	public Window(CelestialGdx gdx, CelestialGdxConfig config) {
		this.gdx = gdx;
		this.windowListener = config.defaultWindowListener;
		this.config = config;
		this.handle = createWindow(config);

		this.input = new DefaultLwjgl3Input(this);
		this.graphics = new Lwjgl3Graphics(this);

		glfwSetWindowFocusCallback(handle, focusCallback);
		glfwSetWindowIconifyCallback(handle, iconifyCallback);
		glfwSetWindowMaximizeCallback(handle, maximizeCallback);
		glfwSetWindowCloseCallback(handle, closeCallback);

		Gdx.input = this.input;
		Gdx.graphics = graphics;

		setVisible(config.windowVisible);
	}

	public void bind() {
		glfwMakeContextCurrent(this.handle);
	}

	private static long createWindow(CelestialGdxConfig config) {
		glfwWindowHint(GLFW_RESIZABLE, config.windowResizable ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_MAXIMIZED, config.maximized ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_AUTO_ICONIFY, config.autoIconify ? GLFW_TRUE : GLFW_FALSE);

		glfwWindowHint(GLFW_RED_BITS, config.r);
		glfwWindowHint(GLFW_GREEN_BITS, config.g);
		glfwWindowHint(GLFW_BLUE_BITS, config.b);
		glfwWindowHint(GLFW_ALPHA_BITS, config.a);
		glfwWindowHint(GLFW_STENCIL_BITS, config.stencil);
		glfwWindowHint(GLFW_DEPTH_BITS, config.depth);
		glfwWindowHint(GLFW_SAMPLES, config.samples);

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, config.glMajorVersion);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, config.glMinorVersion);
		if(Platform.get() == Platform.MACOSX) {
			// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
			// WGL_ARB_create_context extension is not available
			// see: http://www.org/docs/latest/compat.html
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		}

		if(config.transparentFramebuffer) {
			glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
		}

		if(config.debugContext) {
			glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		}

		glfwWindowHint(GLFW_DECORATED, config.windowDecorated ? GLFW_TRUE : GLFW_FALSE);
		long handle = glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, 0);
		if(handle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}

		glfwMakeContextCurrent(handle);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
		glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);

		GL.createCapabilities();

		glfwSwapInterval(config.vsync ? 1 : 0);
		return handle;
	}

	public void setWindowListener(WindowListener listener) {
		this.windowListener = listener;
	}

	/**
	 * Sets the position of the window in logical coordinates. All monitors span a virtual surface together. The coordinates are
	 * relative to the first monitor in the virtual surface.
	 **/
	public void setPosition(int x, int y) {
		if(glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) return;
		glfwSetWindowPos(handle, x, y);
	}

	public void setSize(int width, int height) {
		glfwSetWindowSize(handle, width, height);
	}

	public void setDecorated(boolean decoreated) {
		glfwSetWindowAttrib(handle, GLFW_DECORATED, decoreated ? GLFW_TRUE : GLFW_FALSE);
	}

	public void setResizeable(boolean resizeable) {
		glfwSetWindowAttrib(handle, GLFW_RESIZABLE, resizeable ? GLFW_TRUE : GLFW_FALSE);
	}

	public void setVSync(boolean vsync) {
		glfwSwapInterval(vsync ? 1 : 0);
	}

	public boolean isFullscreen() {
		return glfwGetWindowMonitor(handle) != 0;
	}

	// TODO celestialgdx: make better classes for this in the big vector overhaul
	public record WindowPos(int x, int y) {}

	public WindowPos getPositionX() {
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetWindowPos(this.handle, x, y);
		return new WindowPos(x[0], y[0]);
	}

	/**
	 * Invisible windows will still call their {@link WindowListener}
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			glfwShowWindow(handle);
		} else {
			glfwHideWindow(handle);
		}
	}

	public void setShouldClose(boolean shouldClose) {
		glfwSetWindowShouldClose(handle, shouldClose);
	}

	public void setMinimized(boolean minimized) {
		if(minimized) {
			glfwIconifyWindow(handle);
		} else {
			glfwRestoreWindow(handle);
		}
	}

	public boolean isMinimized() {
		return minimized;
	}

	public void maximizeWindow() {
		glfwMaximizeWindow(handle);
	}

	public void focusWindow() {
		glfwFocusWindow(handle);
	}

	public boolean isFocused() {
		return focused;
	}

	/**
	 * Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
	 * @param images One or more images. The one closest to the system's desired size will be scaled. Good sizes include 16x16,
	 * 32x32 and 48x48. Pixmap format {@link Format#RGBA8888 RGBA8888} is preferred so
	 * the images will not have to be copied and converted. The chosen image is copied, and the provided Pixmaps are not
	 * disposed.
	 */
	public void setIcon(Pixmap... images) {
		if(Platform.get() == Platform.MACOSX) return;

		GLFWImage.Buffer buffer = GLFWImage.malloc(images.length);
		Pixmap[] pixmaps = new Pixmap[images.length];

		for(int i = 0; i < images.length; i++) {
			Pixmap pixmap = images[i];

			if(pixmap.getFormat() != Pixmap.Format.RGBA8888) {
				Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
				rgba.setBlending(Pixmap.Blending.None);
				rgba.drawPixmap(pixmap, 0, 0);
				pixmaps[i] = rgba;
				pixmap = rgba;
			}

			GLFWImage icon = GLFWImage.malloc();
			icon.set(pixmap.getWidth(), pixmap.getHeight(), pixmap.getPixels());
			buffer.put(icon);

			icon.free();
		}

		buffer.position(0);
		glfwSetWindowIcon(this.handle, buffer);
		buffer.free();

		for(Pixmap pixmap : pixmaps) {
			if(pixmap != null) {
				pixmap.dispose();
			}
		}
	}

	public void setTitle(CharSequence title) {
		glfwSetWindowTitle(handle, title);
	}

	public boolean isCloseRequested() {
		return glfwWindowShouldClose(handle);
	}

	public void swapBuffers() {
		glfwSwapBuffers(handle);
	}

	public void flash() {
		glfwRequestWindowAttention(handle);
	}


	public CelestialGdxConfig getConfig() {
		return config;
	}

	@Override
	public void dispose() {
		Lwjgl3Cursor.dispose(this);
		input.dispose();
		glfwDestroyWindow(handle);

		focusCallback.free();
		iconifyCallback.free();
		maximizeCallback.free();
		closeCallback.free();
		resizeCallback.free();
	}
}