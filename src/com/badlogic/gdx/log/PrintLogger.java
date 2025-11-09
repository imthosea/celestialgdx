package com.badlogic.gdx.log;

import java.io.PrintStream;

/**
 * Logger that prints to output streams.
 * Defaults to {@link System#out} and {@link System#err}
 */
public class PrintLogger implements GdxLogger {
	public final String tag;
	public final PrintStream outputStream;
	public final PrintStream errorStream;

	public int logLevel = LOG_INFO;

	public static final int LOG_NONE = 0;
	public static final int LOG_INFO = 2;
	public static final int LOG_DEBUG = 3;
	public static final int LOG_ERROR = 1;

	public PrintLogger(String tag, PrintStream outputStream, PrintStream errorStream) {
		this.outputStream = outputStream;
		this.errorStream = errorStream;
		this.tag = tag;
	}

	public PrintLogger(String tag) {
		this(tag, System.out, System.err);
	}

	@Override
	public void info(String message) {
		if(logLevel >= LOG_INFO) outputStream.println("[" + tag + "] " + message);
	}
	@Override
	public void info(String message, Throwable exception) {
		if(logLevel >= LOG_INFO) {
			outputStream.println("[" + tag + "] " + message);
			exception.printStackTrace(outputStream);
		}
	}

	@Override
	public void error(String message) {
		if(logLevel >= LOG_ERROR) errorStream.println("[" + tag + "] " + message);
	}
	@Override
	public void error(String message, Throwable exception) {
		if(logLevel >= LOG_ERROR) {
			errorStream.println("[" + tag + "] " + message);
			exception.printStackTrace(errorStream);
		}
	}

	@Override
	public void debug(String message) {
		if(logLevel >= LOG_DEBUG) outputStream.println("[" + tag + "] " + message);
	}
	@Override
	public void debug(String message, Throwable exception) {
		if(logLevel >= LOG_DEBUG) {
			outputStream.println("[" + tag + "] " + message);
			exception.printStackTrace(System.out);
		}
	}
}