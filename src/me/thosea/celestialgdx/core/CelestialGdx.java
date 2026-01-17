package me.thosea.celestialgdx.core;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import me.thosea.celestialgdx.log.GdxLogger;
import me.thosea.celestialgdx.log.GdxLoggerFactory;
import me.thosea.celestialgdx.log.PrintLogger;
import me.thosea.celestialgdx.window.Window;
import me.thosea.celestialgdx.window.WindowConfig;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.*;

public class CelestialGdx implements Application {
	private static boolean initializedGlfw = false;

	public final Thread gameThread;

	private final GdxLoggerFactory loggerFactory;
	private final GLFWErrorCallback errorCallback;

	private volatile boolean shouldClose = false;
	private volatile boolean hasRunnables;

	// we need to store as ArrayList to access clone()
	private final ArrayList<Runnable> runnables = new ArrayList<>();

	private long lastFrameTime = -1;
	private double lastDrawTime;

	protected CelestialGdx(GdxLoggerFactory loggerFactory, Supplier<GLFWErrorCallback> errorCallbackSupplier) {
		if(Gdx.app != null) {
			throw new IllegalStateException("Cannot make multiple Lwjgl3Applications");
		}

		this.loggerFactory = loggerFactory;
		this.errorCallback = errorCallbackSupplier.get();
		this.gameThread = Thread.currentThread();

		this.initGlfw();

		Gdx.app = this;
	}

	private void initGlfw() {
		if(initializedGlfw) return;
		glfwSetErrorCallback(this.errorCallback);
		if(!glfwInit()) {
			throw new GdxRuntimeException("Unable to initialize GLFW");
		}
		initializedGlfw = true;
	}

	public Window createWindow() {
		return Window.create(this, new WindowConfig());
	}

	public Window createWindow(Consumer<WindowConfig> configurator) {
		WindowConfig config = new WindowConfig();
		configurator.accept(config);
		return Window.create(this, config);
	}

	public Window createWindow(CelestialGdx gdx, WindowConfig config) {
		return Window.create(gdx, config);
	}

	public void terminate() {
		errorCallback.free();
		glfwTerminate();
	}

	/**
	 * Be sure to call this every frame!
	 */
	public void poll() {
		glfwPollEvents();
		pollRunnables();
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
	 * Updates the delta time then returns the current one. Only call this once per frame.
	 * @return the current delta time
	 */
	public float updateDeltaTime() {
		long time = System.nanoTime();

		float deltaTime = (time - lastFrameTime) / 1_000_000_000.0f;
		this.lastFrameTime = time;
		return deltaTime;
	}

	/**
	 * Caps the FPS with the specified limit.
	 */
	public void capFps(int maxFps) {
		// thanks minecraft
		double targetTime = this.lastDrawTime + (1.0 / maxFps);
		double drawTime;
		for(drawTime = glfwGetTime(); drawTime < targetTime; drawTime = glfwGetTime()) {
			glfwWaitEventsTimeout(targetTime - drawTime);
		}
		this.lastDrawTime = drawTime;
	}

	public static CelestialGdx init() {
		return init(null, null);
	}

	public static CelestialGdx init(Supplier<GLFWErrorCallback> errorCallbackSupplier) {
		return init(null, errorCallbackSupplier);
	}

	public static CelestialGdx init(GdxLoggerFactory loggerFactory) {
		return init(loggerFactory, null);
	}

	public static CelestialGdx init(
			GdxLoggerFactory loggerFactory,
			Supplier<GLFWErrorCallback> errorCallbackSupplier
	) {
		if(loggerFactory == null) loggerFactory = PrintLogger::new;
		if(errorCallbackSupplier == null) errorCallbackSupplier = () -> GLFWErrorCallback.createPrint(System.err);

		CelestialGdx gdx = new CelestialGdx(loggerFactory, errorCallbackSupplier);
		gdx.initGlfw();
		return gdx;
	}
}