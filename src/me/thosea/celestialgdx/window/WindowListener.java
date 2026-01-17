package me.thosea.celestialgdx.window;

import me.thosea.celestialgdx.core.CelestialGdx;

/**
 * @see CelestialGdx#postRunnable(Runnable)
 */
public interface WindowListener {
	/**
	 * called when the window is minimized or unminimized
	 */
	void minimized(Window window, boolean isMinimized);

	/**
	 * called when the window is maximized or unmazimized
	 */
	void maximized(Window window, boolean isMaximized);

	/**
	 * invoked when the window is resized
	 */
	void resized(Window window, int width, int height);

	/**
	 * called when the user requested to close the window.
	 * by default, {@link CelestialGdx#markShouldClose()} will be called
	 * @see CelestialGdx#markShouldClose()
	 */
	void closeRequested(Window window);
}