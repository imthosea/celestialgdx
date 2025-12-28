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

package me.thosea.celestialgdx.window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import me.thosea.celestialgdx.core.CelestialGdx;
import me.thosea.celestialgdx.cursor.Cursor;
import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import me.thosea.celestialgdx.input.InputController;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A GLFW window.
 * When using multiple windows, call {@link #bind()} to change the active OpenGL context
 */
public class Window implements Disposable {
	public final long handle;
	public final Lwjgl3Graphics graphics;
	public final InputController input;

	public final CelestialGdx gdx;

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

	protected Window(CelestialGdx gdx, WindowConfig config) {
		this.gdx = gdx;
		this.windowListener = config.listener;
		this.handle = createWindow(config);

		this.graphics = new Lwjgl3Graphics(this);

		glfwSetWindowFocusCallback(handle, focusCallback);
		glfwSetWindowIconifyCallback(handle, iconifyCallback);
		glfwSetWindowMaximizeCallback(handle, maximizeCallback);
		glfwSetWindowCloseCallback(handle, closeCallback);

		this.input = Gdx.input = new InputController(this);
		Gdx.graphics = graphics;
		setVisible(config.initialVisible);
	}

	public void bind() {
		glfwMakeContextCurrent(this.handle);
	}

	public void setCursor(Cursor cursor) {
		glfwSetCursor(this.handle, cursor.getHandle());
	}

	private static long createWindow(WindowConfig config) {
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, config.glMajorVersion);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, config.glMinorVersion);

		glfwWindowHint(GLFW_RESIZABLE, glfwBool(config.windowResizable));
		glfwWindowHint(GLFW_MAXIMIZED, glfwBool(config.maximized));
		glfwWindowHint(GLFW_AUTO_ICONIFY, glfwBool(config.autoIconify));
		glfwWindowHint(GLFW_DECORATED, glfwBool(config.windowDecorated));
		glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, glfwBool(config.transparentFramebuffer));

		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, glfwBool(config.debugContext));

		glfwWindowHint(GLFW_RED_BITS, config.r);
		glfwWindowHint(GLFW_GREEN_BITS, config.g);
		glfwWindowHint(GLFW_BLUE_BITS, config.b);
		glfwWindowHint(GLFW_ALPHA_BITS, config.a);
		glfwWindowHint(GLFW_STENCIL_BITS, config.stencil);
		glfwWindowHint(GLFW_DEPTH_BITS, config.depth);
		glfwWindowHint(GLFW_SAMPLES, config.samples);

		if(Platform.get() == Platform.MACOSX) {
			// hints mandatory on macOS for GL 3.2+ context creation
			// see: http://www.org/docs/latest/compat.html
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		}

		long handle = glfwCreateWindow(
				config.windowWidth, config.windowHeight,
				config.title, 0, config.shareWindow == null ? 0 : config.shareWindow.handle
		);
		if(handle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}

		glfwMakeContextCurrent(handle);
		GL.createCapabilities();

		glfwSwapInterval(config.vsync ? 1 : 0);

		return handle;
	}

	private static int glfwBool(boolean value) {
		return value ? GLFW_TRUE : GLFW_FALSE;
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
	 * Set the window's icon. Has no effect in macOS, see
	 * <a href="https://developer.apple.com/library/content/documentation/CoreFoundation/Conceptual/CFBundles/">Bundle Programming Guide</a>.
	 * <p>
	 * The pixmaps must be in {@link PixelFormat#RGBA} or an exception will be thrown.
	 * The system will pick the best size from the pixmaps based on its settings and rescale as needed.
	 * Good sizes to target are 16x16, 32x32 and 48x48.
	 * </p>
	 * The pixmaps can be disposed of after calling this method.
	 */
	public void setIcon(Pixmap... images) {
		if(Platform.get() == Platform.MACOSX) return;
		if(images.length == 0) return;

		try(MemoryStack stack = MemoryStack.stackPush()) {
			GLFWImage.Buffer buffer = GLFWImage.malloc(images.length, stack);
			for(int i = 0; i < images.length; i++) {
				Pixmap pixmap = images[i];
				buffer.position(i);
				buffer.width(pixmap.width);
				buffer.height(pixmap.height);
				buffer.pixels(pixmap.getBuffer());
			}
			buffer.flip();
			glfwSetWindowIcon(this.handle, buffer);
		}
	}

	/** Resets the window's icon to the default */
	public void resetIcon() {
		glfwSetWindowIcon(this.handle, null);
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

	@Override
	public void dispose() {
		Gdx.input.dispose();
		glfwDestroyWindow(handle);

		focusCallback.free();
		iconifyCallback.free();
		maximizeCallback.free();
		closeCallback.free();
		resizeCallback.free();
	}

	public static Window create(CelestialGdx gdx) {
		return create(gdx, new WindowConfig());
	}

	public static Window create(CelestialGdx gdx, Consumer<WindowConfig> configurator) {
		WindowConfig config = new WindowConfig();
		configurator.accept(config);
		return create(gdx, config);
	}

	public static Window create(CelestialGdx gdx, WindowConfig config) {
		return new Window(gdx, config);
	}
}