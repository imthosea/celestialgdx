package com.badlogic.gdx.log;

@FunctionalInterface
public interface GdxLoggerFactory {
	GdxLogger createLogger(String name);
}