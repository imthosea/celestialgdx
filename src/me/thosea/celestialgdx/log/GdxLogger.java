package me.thosea.celestialgdx.log;

/**
 * Provides an interface for CelestialGDX to log messages.
 * The default implementation prints to System.out/System.err.
 */
public interface GdxLogger {
	void info(String message);
	void info(String message, Throwable exception);
	void error(String message);
	void error(String message, Throwable exception);
	void debug(String message);
	void debug(String message, Throwable exception);
}