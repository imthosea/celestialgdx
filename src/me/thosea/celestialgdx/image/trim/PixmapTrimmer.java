package me.thosea.celestialgdx.image.trim;

import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteOrder;

/**
 * A reasonably fast pixmap trimmer.
 * It calculates the non-empty edges of a specified pixmap and returns a
 * {@link PixmapTrim} with the range.
 * @author thosea
 */
public final class PixmapTrimmer {
	private PixmapTrimmer() {}

	/** on any modern machine this is true */
	private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	/** alpha mask when reading two pixels */
	private static final long DOUBLE_ALPHA_MASK = IS_LITTLE_ENDIAN
			? 0xFF000000FF000000L
			: 0x000000FF000000FFL;
	/** alpha mask when reading one pixel */
	private static final int SINGLE_ALPHA_MASK = IS_LITTLE_ENDIAN
			? 0xFF000000
			: 0x000000FF;

	private static final long PIXEL_SIZE = 4; // RGBA

	public static PixmapTrim trim(Pixmap pixmap) {
		if(pixmap.format != PixelFormat.RGBA) {
			throw new IllegalArgumentException("Only RGBA is supported");
		} else if(pixmap.width < 2 || pixmap.height < 2) {
			throw new IllegalArgumentException("This pixmap is too small");
		}

		long address = MemoryUtil.memAddress(pixmap.getBuffer());
		int width = pixmap.width;
		int height = pixmap.height;

		int top;
		int bottom;
		for(top = 0; top < height; top++) {
			if(!isEmptyRow(address, width, /*y*/ top)) break;
		}
		for(bottom = height; bottom > top + 1; bottom--) {
			if(!isEmptyRow(address, width, bottom - 1)) break;
		}

		int left;
		int right;
		for(left = 0; left < width - 1; left += 2) {
			TwoColumnResult result = findTwoColumn(address, width, height, /*x*/ left);
			if(result == null) continue;
			long firstPixel = IS_LITTLE_ENDIAN ? result.data() << 32 : result.data() >> 32;
			if(firstPixel == 0 && result.y() + 1 < pixmap.height) {
				// there's only a pixel on the second column
				int y = result.y() + 1;
				if(isEmptySingleColumn(address, width, height, /*x*/ left, /*startY*/ y)) {
					// if the rest of the first column was empty, then we use the second column
					left++;
				}
			}
			break;
		}
		for(right = width; right >= left + 2; right -= 2) {
			TwoColumnResult result = findTwoColumn(address, width, height, /*x*/ right - 2);
			if(result == null) continue;
			long secondPixel = IS_LITTLE_ENDIAN ? result.data() >> 32 : result.data() << 32;
			if(secondPixel == 0 && result.y() + 1 < pixmap.height) {
				int y = result.y() + 1;
				if(isEmptySingleColumn(address, width, height, /*x*/ right - 1, y)) {
					right--;
				}
			}
			break;
		}
		if((width & 1) != 0) { // uneven
			if(left == width - 1) {
				// check last column
				if(isEmptySingleColumn(address, width, height, /*x*/ left))
					left++;
			}
			if(right == 1) {
				// check first column
				if(isEmptySingleColumn(address, width, height, /*x*/ 0))
					right--;
			}
		}

		return new PixmapTrim(width, height, top, bottom, left, right);
	}

	/** checks if all pixels in the row at the Y value are zero */
	private static boolean isEmptyRow(long address, int width, int y) {
		long yOffset = width * y * PIXEL_SIZE;
		// each pixel is 4 bytes, read two pixels at once
		int x;
		for(x = 0; x + 1 < width; x += 2) {
			long xOffset = x * PIXEL_SIZE;
			long data = MemoryUtil.memGetLong(address + yOffset + xOffset);
			if((data & DOUBLE_ALPHA_MASK) != 0) return false;
		}
		if((width & 1) != 0 && x == width - 1) {
			// for pixmaps with a not even width, make sure to check the final pixel
			long xOffset = x * PIXEL_SIZE;
			int data = MemoryUtil.memGetInt(address + yOffset + xOffset);
			if((data & SINGLE_ALPHA_MASK) != 0) return false;
		}
		return true;
	}

	/**
	 * finds the first non-zero pixel in two columns starting at the X value,
	 * returns the alpha data of two pixels with the Y it was found at
	 * or null if none found
	 */
	@Nullable
	private static TwoColumnResult findTwoColumn(long address, int width, int height, int x) {
		long xOffset = x * PIXEL_SIZE;
		for(int y = 0; y < height; y++) {
			long yOffset = (long) y * width * PIXEL_SIZE;
			long data = MemoryUtil.memGetLong(address + yOffset + xOffset) & DOUBLE_ALPHA_MASK;
			if(data != 0) return new TwoColumnResult(data, y);
		}
		return null;
	}

	private record TwoColumnResult(long data, int y) {}

	/** checks if all pixels in the column at the X value are zero */
	private static boolean isEmptySingleColumn(long address, int width, int height, int x) {
		return isEmptySingleColumn(address, width, height, x, /*startY*/ 0);
	}

	private static boolean isEmptySingleColumn(long address, int width, int height, int x, int startY) {
		long xOffset = x * PIXEL_SIZE;
		for(int y = startY; y < height; y++) {
			long yOffset = (long) y * width * PIXEL_SIZE;
			int data = MemoryUtil.memGetInt(address + yOffset + xOffset);
			if((data & SINGLE_ALPHA_MASK) != 0) return false;
		}
		return true;
	}
}