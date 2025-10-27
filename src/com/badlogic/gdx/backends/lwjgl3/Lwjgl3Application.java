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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengles.GLES;
import org.lwjgl.system.Platform;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengles.GLES32.*;

public class Lwjgl3Application implements Lwjgl3ApplicationBase {
	public final Lwjgl3ApplicationConfiguration config;
	public final Lwjgl3Window window;
	public final Lwjgl3Files files;
	public final Lwjgl3Clipboard clipboard;

	private int logLevel = LOG_INFO;
	private ApplicationLogger applicationLogger;

	private final Thread gameThread;

	private volatile boolean running = true;
	private volatile boolean hasRunnables;

	private final Array<LifecycleListener> lifecycleListeners = new Array<>();
	private final Array<Runnable> runnables = new Array<>();
	private final Array<Runnable> executedRunnables = new Array<>();
	private final GLFWErrorCallback errorCallback;
	private final GLVersion glVersion;
	private final Sync sync;

	protected Lwjgl3Application(ApplicationCreator creator) {
		this(creator, new Lwjgl3ApplicationConfiguration());
	}

	protected Lwjgl3Application(ApplicationCreator creator, Lwjgl3ApplicationConfiguration config) {
		if(config.glEmulation != GLEmulation.GL32) {
			throw new IllegalStateException("Only GL32 is supported in CelestialGDX. These options will be removed soon");
		}
		if(Gdx.app != null) {
			throw new IllegalStateException("Cannot make multiple Lwjgl3Applications");
		}

		Lwjgl3NativesLoader.load();
		this.errorCallback = GLFWErrorCallback.createPrint(Lwjgl3ApplicationConfiguration.errorStream);
		glfwSetErrorCallback(this.errorCallback);
		if(Platform.get() == Platform.MACOSX)
			glfwInitHint(GLFW_ANGLE_PLATFORM_TYPE, GLFW_ANGLE_PLATFORM_TYPE_METAL);
		glfwInitHint(GLFW_JOYSTICK_HAT_BUTTONS, GLFW_FALSE);
		if(!glfwInit()) {
			throw new GdxRuntimeException("Unable to initialize GLFW");
		}

		setApplicationLogger(new Lwjgl3ApplicationLogger());

		this.config = config = Lwjgl3ApplicationConfiguration.copy(config);
		this.gameThread = Thread.currentThread();

		if(config.title == null) config.title = "game";

		Gdx.app = this;
		Gdx.files = this.files = createFiles();
		this.clipboard = new Lwjgl3Clipboard();

		this.sync = new Sync();

		long windowHandle = createGlfwWindow(config);
		this.glVersion = new GLVersion(
				ApplicationType.Desktop,
				glGetString(GL_VERSION),
				glGetString(GL_VENDOR),
				glGetString(GL_RENDERER)
		);
		this.window = new Lwjgl3Window(windowHandle, creator, config, this);

		try {
			loop();

			window.dispose();
			lifecycleListeners.forEach(LifecycleListener::dispose);
			lifecycleListeners.clear();
		} catch(RuntimeException t) {
			throw t;
		} catch(Throwable t) {
			throw new GdxRuntimeException(t);
		} finally {
			cleanup();
		}
	}

	protected void loop() {
		while(running && !window.shouldClose()) {
			glfwPollEvents();
			window.update();

			pollRunnables();

			int targetFramerate = window.getConfig().foregroundFPS;
			if(targetFramerate > 0) {
				sync.sync(targetFramerate); // sleep as needed to meet the target framerate
			}
		}
	}

	protected void cleanup() {
		Lwjgl3Cursor.disposeSystemCursors();
		errorCallback.free();
		glfwTerminate();
	}

	@Override
	public ApplicationListener getApplicationListener() {
		return window.getListener();
	}

	@Override
	public Graphics getGraphics() {
		return window.getGraphics();
	}

	@Override
	public Input getInput() {
		return window.getInput();
	}

	@Override
	public Files getFiles() {
		return files;
	}

	@Override
	public void debug(String tag, String message) {
		if(logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		if(logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
	}

	@Override
	public void log(String tag, String message) {
		if(logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		if(logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
	}

	@Override
	public void error(String tag, String message) {
		if(logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		if(logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
	}

	@Override
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel() {
		return logLevel;
	}

	@Override
	public void setApplicationLogger(ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger() {
		return applicationLogger;
	}

	@Override
	public ApplicationType getType() {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public long getJavaHeap() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap() {
		return getJavaHeap();
	}

	@Override
	public Clipboard getClipboard() {
		return clipboard;
	}

	@Override
	public void postRunnable(Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
		hasRunnables = true;
	}

	@Override
	public boolean isGameThread() {
		return Thread.currentThread() == this.gameThread;
	}

	@Override
	public void pollRunnables() {
		if(!hasRunnables) return;
		executedRunnables.clear();
		synchronized(runnables) {
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		for(Runnable runnable : executedRunnables) {
			runnable.run();
		}
		hasRunnables = false;
	}

	@Override
	public void exit() {
		running = false;
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycleListeners.add(listener);
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycleListeners.removeValue(listener, true);
	}

	@Override
	public Lwjgl3Input createInput(Lwjgl3Window window) {
		return new DefaultLwjgl3Input(window);
	}

	protected Lwjgl3Files createFiles() {
		return new Lwjgl3Files();
	}

	static long createGlfwWindow(Lwjgl3ApplicationConfiguration config) {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, config.windowResizable ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_MAXIMIZED, config.windowMaximized ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_AUTO_ICONIFY, config.autoIconify ? GLFW_TRUE : GLFW_FALSE);

		glfwWindowHint(GLFW_RED_BITS, config.r);
		glfwWindowHint(GLFW_GREEN_BITS, config.g);
		glfwWindowHint(GLFW_BLUE_BITS, config.b);
		glfwWindowHint(GLFW_ALPHA_BITS, config.a);
		glfwWindowHint(GLFW_STENCIL_BITS, config.stencil);
		glfwWindowHint(GLFW_DEPTH_BITS, config.depth);
		glfwWindowHint(GLFW_SAMPLES, config.samples);

		if(config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL30
				|| config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL31
				|| config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL32) {
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion);
			glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion);
			if(Platform.get() == Platform.MACOSX) {
				// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
				// WGL_ARB_create_context extension is not available
				// see: http://www.org/docs/latest/compat.html
				glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
				glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			}
		} else {
			if(config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
				glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
				glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
				glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
				glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
			}
		}

		if(config.transparentFramebuffer) {
			glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
		}

		if(config.debug) {
			glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		}

		long windowHandle;

		if(config.fullscreenMode != null) {
			glfwWindowHint(GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
			windowHandle = glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title,
					config.fullscreenMode.getMonitor(), 0);

			// On Ubuntu >= 22.04 with Nvidia GPU drivers and X11 display server there's a bug with EGL Context API
			// If the windows creation has failed for this reason try to create it again with the native context
			if(windowHandle == 0 && config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
				glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
				windowHandle = glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title,
						config.fullscreenMode.getMonitor(), 0);
			}
		} else {
			// glfwWindowHint(GLFW_DECORATED, config.windowDecorated ? GLFW_TRUE : GLFW_FALSE);
			windowHandle = glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, 0);

			// On Ubuntu >= 22.04 with Nvidia GPU drivers and X11 display server there's a bug with EGL Context API
			// If the windows creation has failed for this reason try to create it again with the native context
			if(windowHandle == 0 && config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
				glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
				windowHandle = glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, 0);
			}
		}
		if(windowHandle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}
		Lwjgl3Window.setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth,
				config.windowMaxHeight);
		if(config.fullscreenMode == null) {
			if(glfwGetPlatform() != GLFW_PLATFORM_WAYLAND) {
				if(config.windowX == -1 && config.windowY == -1) { // i.e., center the window
					int windowWidth = Math.max(config.windowWidth, config.windowMinWidth);
					int windowHeight = Math.max(config.windowHeight, config.windowMinHeight);
					if(config.windowMaxWidth > -1) windowWidth = Math.min(windowWidth, config.windowMaxWidth);
					if(config.windowMaxHeight > -1) windowHeight = Math.min(windowHeight, config.windowMaxHeight);

					long monitorHandle = glfwGetPrimaryMonitor();
					if(config.windowMaximized && config.maximizedMonitor != null) {
						monitorHandle = config.maximizedMonitor.monitorHandle;
					}

					GridPoint2 newPos = Lwjgl3ApplicationConfiguration.calculateCenteredWindowPosition(
							Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(monitorHandle), windowWidth, windowHeight);
					glfwSetWindowPos(windowHandle, newPos.x, newPos.y);
				} else {
					glfwSetWindowPos(windowHandle, config.windowX, config.windowY);
				}
			}

			if(config.windowMaximized) {
				glfwMaximizeWindow(windowHandle);
			}
		}
		if(config.pixmaps != null) {
			Lwjgl3Window.setIcon(windowHandle, config.pixmaps);
		}
		glfwMakeContextCurrent(windowHandle);
		glfwSwapInterval(config.vSyncEnabled ? 1 : 0);

		// some platforms (notably macOS) don't support creating an GLES context
		// instead, we setup GLES using a desktop GL context
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_ES_API);
		glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
		GLES.createCapabilities();

		if(config.debug) {
			// TODO celestialgdx find GLES replacement for what GDX had
		}

		return windowHandle;
	}

	public static void create(ApplicationCreator creator, Lwjgl3ApplicationConfiguration config) {
		new Lwjgl3Application(creator, config);
	}
}