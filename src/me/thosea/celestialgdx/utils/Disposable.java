package me.thosea.celestialgdx.utils;

/**
 * A resource that needs to be manually cleaned, i.e. because it's backed by a GPU or native-code buffer.
 */
public interface Disposable {
	/** Releases the resource. Calling this twice will throw an exception. */
	void dispose();
	boolean isDisposed();
	default void requireNotDisposed() {
		if(isDisposed()) throw new IllegalStateException("already disposed");
	}
}