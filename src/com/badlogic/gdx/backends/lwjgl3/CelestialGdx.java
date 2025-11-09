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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.log.GdxLogger;
import com.badlogic.gdx.log.GdxLoggerFactory;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class CelestialGdx implements Application {
	public final Window window;
	public final Thread gameThread;

	private final GdxLoggerFactory loggerFactory;
	private final GLFWErrorCallback errorCallback;

	private volatile boolean shouldClose = false;
	private volatile boolean hasRunnables;

	// we need to store as ArrayList to access clone()
	private final ArrayList<Runnable> runnables = new ArrayList<>();

	private long lastFrameTime = -1;

	protected CelestialGdx(CelestialGdxConfig config) {
		if(Gdx.app != null) {
			throw new IllegalStateException("Cannot make multiple Lwjgl3Applications");
		}

		this.loggerFactory = config.loggerFactory;
		this.errorCallback = config.errorCallbackSupplier.get();
		glfwSetErrorCallback(this.errorCallback);

		this.initGlfw();

		this.gameThread = Thread.currentThread();

		this.window = new Window(this, config);

		Gdx.app = this;
	}

	protected void initGlfw() {
		Lwjgl3NativesLoader.load();
		glfwInitHint(GLFW_JOYSTICK_HAT_BUTTONS, GLFW_FALSE);
		if(!glfwInit()) {
			throw new GdxRuntimeException("Unable to initialize GLFW");
		}
	}

	public void terminate() {
		Lwjgl3Cursor.disposeSystemCursors();
		errorCallback.free();
		window.dispose();
		glfwTerminate();
	}

	/**
	 * Be sure to call this every frame!
	 */
	public void pollEvents() {
		glfwPollEvents();
		pollRunnables();
	}

	@Override
	public Graphics getGraphics() {
		return window.graphics;
	}

	@Override
	public Input getInput() {
		return window.input;
	}

	@Override
	public ApplicationType getType() {
		return ApplicationType.Desktop;
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
		if(!isGameThread()) {
			throw new IllegalStateException("Cannot pull events from a thread that isn't the main one");
		}
		hasRunnables = false;
		List<Runnable> actions;
		synchronized(runnables) {
			actions = (List<Runnable>) runnables.clone();
			runnables.clear();
		}
		for(Runnable runnable : actions) {
			runnable.run();
		}
	}

	/**
	 * sets the result of {@link #shouldClose()} to true
	 */
	public void markShouldClose() {
		shouldClose = true;
	}

	/**
	 * @return if the program is marked to close. be sure to check this in your game loop
	 */
	public boolean shouldClose() {
		return shouldClose;
	}

	public GdxLogger createLogger(String tag) {
		return loggerFactory.createLogger(tag);
	}

	/**
	 * updates the delta time then returns the current one. only call this once per frame.
	 * @return the current delta time
	 */
	public float updateDeltaTime() {
		long time = System.nanoTime();

		float deltaTime = (time - lastFrameTime) / 1_000_000_000.0f;
		this.lastFrameTime = time;
		return deltaTime;
	}

	public static CelestialGdx init(Consumer<CelestialGdxConfig> configurator) {
		CelestialGdxConfig config = new CelestialGdxConfig();
		configurator.accept(config);
		CelestialGdx gdx = new CelestialGdx(config);
		gdx.initGlfw();
		return gdx;
	}
}