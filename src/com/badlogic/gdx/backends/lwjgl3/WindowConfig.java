package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.graphics.Pixmap;
import org.jetbrains.annotations.Nullable;

public final class WindowConfig implements Cloneable {
	public String title = "game";
	public int windowWidth = 640;
	public int windowHeight = 540;
	public boolean windowResizable = false;
	public boolean windowDecorated = true;
	public boolean maximized = false;

	public int glMajorVersion = 3;
	public int glMinorVersion = 3;
	public boolean debugContext = false;

	public int r = 8, g = 8, b = 8, a = 8;
	public int depth = 16, stencil = 8;
	public int samples = 0;
	public boolean transparentFramebuffer;

	public boolean autoIconify = true;
	public boolean initialVisible = true;
	public boolean vsync = true;
	@Nullable public Pixmap[] icon = null;

	/** Specifies a window to share resources with (i.e. textures, vertex buffers) */
	@Nullable public Window shareWindow = null;

	public WindowListener listener = new WindowListener() {
		@Override public void minimized(Window window, boolean isMinimized) {}
		@Override public void maximized(Window window, boolean isMaximized) {}
		@Override public void resized(Window window, int width, int height) {}
		@Override
		public void closeRequested(Window window) {
			window.gdx.markShouldClose();
		}
	};

	@Override
	public WindowConfig clone() {
		try {
			return (WindowConfig) super.clone();
		} catch(CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}