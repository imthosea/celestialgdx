package me.thosea.celestialgdx.log;

@FunctionalInterface
public interface GdxLoggerFactory {
	GdxLogger createLogger(String name);
}