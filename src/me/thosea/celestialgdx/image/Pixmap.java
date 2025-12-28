package me.thosea.celestialgdx.image;

import me.thosea.celestialgdx.utils.Disposable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBIWriteCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.*;

/**
 * An in-memory image loaded usually from STBI.
 * <p>
 * To create one, use a variant {@link #load} and pass a buffer containing the
 * data of an image file or use {@link #create} to make a new blank pixmap.
 * Supported file formats match what STBI supports:
 * JPEG, PNG, TGA, BMP, HDR, PSD, GIF, PIC and PNM as of now.
 * </p>
 * <p>
 * The only pixel formats supported are the ones STBI supports: gray, gray/alpha, RGB and RGBA.
 * Only RGB and RGBA can be uploaded to the GPU; the other ones will throw an exception.
 * If a pixel format is specified at creation, the returned buffer will always use that format.
 * Otherwise, the format will match what the image file contained.
 * </p>
 * <p>
 * Unlike LibGDX's pixmap, drawing one pixmap onto another doesn't handle blending, filtering
 * or conversion between formats. The advantage of this is that drawing one pixmap onto another
 * is implemented simply by copying memory as opposed to looping over every pixel.
 * </p>
 * <p>
 * To write to a file, use one of the write methods.
 * Ones taking a string will write to that file on disk.
 * Ones taking {@link ImageWriter} will invoke your callback possibly many times to write data.
 * </p>
 * <p>
 * Pixmaps store an off-heap buffer and thus must be {@link #dispose()}d of when you're done.
 * Most methods will throw an exception if called after the pixmap is disposed.
 * Use {@link #isDisposed()} to check.
 * </p>
 * @author thosea
 */
public final class Pixmap implements Disposable {
	private final ByteBuffer buffer;

	public final PixelFormat format;

	public final int width;
	public final int height;

	private boolean disposed = false;

	private Pixmap(ByteBuffer buffer, PixelFormat format, int width, int height) {
		Objects.requireNonNull(buffer);
		Objects.requireNonNull(format);
		if(width < 0 || height < 0) {
			throw new IllegalArgumentException("Cannot have negative width/height");
		}
		this.buffer = buffer;
		this.format = format;
		this.width = width;
		this.height = height;
	}

	public ByteBuffer getBuffer() {
		this.requireNotDisposed();
		return buffer;
	}

	/**
	 * Copies memory from the other pixmap into this one. Both pixmaps must be in the same {@link PixelFormat}.
	 * If this pixmap isn't big enough, an exception will be thrown.
	 * @param pixmap the other pixmap
	 * @param targetX X value to start writing at in this pixmap (origin is top-left)
	 * @param targetY Y value to start writing at in this pixmap (origin is top-left)
	 */
	public void copyFrom(Pixmap pixmap, int targetX, int targetY) {
		this.copyFrom(pixmap, targetX, targetY, 0, 0, pixmap.width, pixmap.height);
	}

	/**
	 * Copies memory from the other pixmap into this one. Both pixmaps must be in the same {@link PixelFormat}.
	 * If this pixmap isn't big enough, an exception will be thrown.
	 * @param pixmap the other pixmap
	 * @param targetX X value to start writing at in this pixmap (origin is top-left)
	 * @param targetY Y value to start writing at in this pixmap (origin is top-left)
	 * @param srcX X value to start reading at from the other pixmap (origin is top-left)
	 * @param srcY Y value to start reading at from the other pixmap (origin is top-left)
	 * @param width the width of the area to copy from the other pixmap
	 * @param height the height of the area to copy from the other pixmap
	 */
	public void copyFrom(
			Pixmap pixmap,
			int targetX, int targetY,
			int srcX, int srcY,
			int width, int height
	) {
		if(this.disposed) {
			throw new IllegalStateException("This pixmap is disposed");
		} else if(pixmap.disposed) {
			throw new IllegalArgumentException("Other pixmap is disposed");
		} else if(this.format != pixmap.format) {
			throw new IllegalArgumentException(
					"This pixmap uses the " + this.format + " while the other uses " + pixmap.format + "."
							+ " The easiest way to fix this is to manually specify a format when constructing the pixmap."
			);
		} else if(targetX < 0 || targetY < 0) {
			throw new IllegalArgumentException("Cannot have negative target X/Y");
		} else if(srcX < 0 || srcY < 0) {
			throw new IllegalArgumentException("Cannot have negative source X/Y");
		} else if(width < 0 || height < 0) {
			throw new IllegalArgumentException("Cannot have negative width/height");
		} else if(targetX + width > this.width || targetY + height > this.height) {
			throw new IllegalArgumentException("This pixmap is not big enough");
		} else if(srcX + width > pixmap.width || srcY + height > pixmap.height) {
			throw new IllegalArgumentException("The other pixmap is not big enough");
		}

		int pixelSize = format.components;

		long srcWidth = pixmap.width;
		long targetWidth = this.width;

		long srcBase = MemoryUtil.memAddress(pixmap.buffer);
		long targetBase = MemoryUtil.memAddress(this.buffer);
		long copySize = (long) width * pixelSize;

		long srcMax = srcBase + pixmap.buffer.remaining();
		long targetMax = targetBase + this.buffer.remaining();

		for(int y = 0; y < height; y++) {
			long src = srcBase + pixelSize * (srcX + (y + srcY) * srcWidth);
			long target = targetBase + pixelSize * (targetX + (y + targetY) * targetWidth);
			if(src >= srcMax || target >= targetMax) {
				throw new IllegalStateException("buffer overflow");
			}
			MemoryUtil.memCopy(src, target, copySize);
		}
	}

	public boolean writePng(String filePath) {
		return stbi_write_png(filePath, width, height, format.components, getBuffer(), /*stride*/ 0);
	}
	public boolean writeBmp(String filePath) {
		return stbi_write_bmp(filePath, width, height, format.components, getBuffer());
	}
	public boolean writeTga(String filePath) {
		return stbi_write_tga(filePath, width, height, format.components, getBuffer());
	}
	/** Quality is between 1 and 100 (higher means better) */
	public boolean writeJpg(int quality, String filePath) {
		return stbi_write_jpg(filePath, width, height, format.components, getBuffer(), quality);
	}

	@FunctionalInterface
	public interface ImageWriter {
		void write(ByteBuffer buffer);
	}

	public void writePng(ImageWriter writer) {
		this.doWrite(writer, handler -> {
			stbi_write_png_to_func(handler, /*context*/ 0L, width, height, format.components, getBuffer(), /*stride*/ 0);
		});
	}
	public void writeBmp(ImageWriter writer) {
		this.doWrite(writer, handler -> {
			stbi_write_bmp_to_func(handler, /*context*/ 0L, width, height, format.components, getBuffer());
		});
	}
	public void writeTga(ImageWriter writer) {
		this.doWrite(writer, handler -> {
			stbi_write_tga_to_func(handler, /*context*/ 0L, width, height, format.components, getBuffer());
		});
	}
	/** Quality is between 1 and 100 (higher means better) */
	public void writeJpg(int quality, ImageWriter writer) {
		this.doWrite(writer, handler -> {
			stbi_write_jpg_to_func(handler, /*context*/ 0L, width, height, format.components, getBuffer(), quality);
		});
	}

	private void doWrite(ImageWriter writer, Consumer<STBIWriteCallbackI> handler) {
		try(var callback = STBIWriteCallback.create((context, data, size) -> {
			writer.write(MemoryUtil.memByteBuffer(data, size));
		})) {
			handler.accept(callback);
		}
	}

	public boolean isDisposed() {
		return disposed;
	}
	public void requireNotDisposed() {
		if(disposed) throw new IllegalStateException("already disposed");
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		stbi_image_free(buffer);
		this.disposed = true;
	}

	/**
	 * Creates a pixmap from an existing image buffer
	 */
	public static Pixmap fromExisting(ByteBuffer buffer, PixelFormat format, int width, int height) {
		return new Pixmap(buffer, format, width, height);
	}

	/**
	 * Creates a new blank pixmap in the specified format.
	 * All pixels with be blank and have 0 opacity if the format has an alpha channel.
	 */
	public static Pixmap create(PixelFormat format, int width, int height) {
		return new Pixmap(
				MemoryUtil.memCalloc(format.components * width * height),
				format, width, height
		);
	}

	/**
	 * Loads a pixmap from the buffer. The format will be whatever the file uses.
	 */
	public static Pixmap load(ByteBuffer buffer) {
		return load(buffer, false);
	}

	/**
	 * Loads a pixmap from the buffer. The format will be whatever the file uses.
	 */
	public static Pixmap load(ByteBuffer buffer, boolean flipVertically) {
		Objects.requireNonNull(buffer);
		stbi_set_flip_vertically_on_load_thread(flipVertically ? 1 : 0);
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			IntBuffer format = stack.mallocInt(1);
			ByteBuffer image = stbi_load_from_memory(buffer, width, height, format, /*desired_channels*/ 0);
			checkStbi(image);
			return new Pixmap(
					image, PixelFormat.byComponentCount(format.get()),
					width.get(), height.get()
			);
		}
	}

	/**
	 * Loads a pixmap from the buffer. The format will always be the one provided.
	 */
	public static Pixmap load(ByteBuffer buffer, PixelFormat format) {
		return load(buffer, format, false);
	}

	/**
	 * Loads a pixmap from the buffer. The format will always be the one provided.
	 */
	public static Pixmap load(ByteBuffer buffer, PixelFormat format, boolean flipVertically) {
		Objects.requireNonNull(buffer);
		Objects.requireNonNull(format);
		stbi_set_flip_vertically_on_load_thread(flipVertically ? 1 : 0);
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			ByteBuffer image = stbi_load_from_memory(buffer, width, height, stack.mallocInt(1), format.components);
			checkStbi(image);
			return new Pixmap(
					image, format,
					width.get(), height.get()
			);
		}
	}

	private static void checkStbi(ByteBuffer buffer) {
		if(buffer == null) {
			String reason = stbi_failure_reason();
			throw new IllegalStateException("Failed to load image: " + reason);
		}
	}
}