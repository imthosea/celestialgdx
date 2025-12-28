package me.thosea.celestialgdx.cursor;

import com.badlogic.gdx.utils.Disposable;
import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import me.thosea.celestialgdx.window.Window;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.glfwCreateCursor;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyCursor;

/**
 * A GLFW cursor.
 * Can be created from an RGBA image using {@link #create}
 * or from the system cursor theme using {@link #createSystem(SystemCursor)}.
 * Both methods may fail and return null.
 * <p>
 * To set the active cursor, use {@link Window#setCursor(Cursor)}.
 * </p>
 * <p>
 * Cursors are {@link Disposable} and must be {@link #dispose()}d of when you're done.
 * </p>
 */
public final class Cursor implements Disposable {
	private final long handle;
	private boolean disposed = false;

	private Cursor(long handle) {
		this.handle = handle;
	}

	public long getHandle() {
		requireNotDisposed();
		return handle;
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		glfwDestroyCursor(handle);
		this.disposed = true;
	}

	public boolean isDisposed() {
		return disposed;
	}
	public void requireNotDisposed() {
		if(disposed) throw new IllegalStateException("already disposed");
	}

	/**
	 * Attempts to create a cursor from an existing image buffer.
	 * The hotspot coordinates determines the center of the image, where the origin is the top-left.
	 * @param pixmap the image buffer. must be in {@link PixelFormat#RGBA}
	 * @param xHotspot the hotspot pixel X coordinate
	 * @param yHotspot the hotspot pixel Y coordinate
	 * @return the new cursor or null if an unknown error occurred
	 */
	public static Cursor create(Pixmap pixmap, int xHotspot, int yHotspot) {
		if(pixmap.format != PixelFormat.RGBA) {
			throw new IllegalArgumentException("The pixmap must be in RGBA format");
		}

		try(MemoryStack stack = MemoryStack.stackPush()) {
			GLFWImage image = GLFWImage.malloc(stack)
					.width(pixmap.width)
					.height(pixmap.height)
					.pixels(pixmap.getBuffer());
			long handle = glfwCreateCursor(image, xHotspot, yHotspot);
			return handle != 0 ? new Cursor(handle) : null;
		}
	}

	/**
	 * Attempts to create a cursor from a system shape.
	 * If the system doesn't support the cursor shape, null will be returned.
	 * See {@link GLFW#glfwCreateStandardCursor(int)} to see what systems support what cursors.
	 */
	@Nullable
	public static Cursor createSystem(SystemCursor shape) {
		long handle = glfwCreateStandardCursor(shape.glfwType);
		return handle != 0 ? new Cursor(handle) : null;
	}
}