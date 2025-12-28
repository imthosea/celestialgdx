package me.thosea.celestialgdx.image.trim;

/**
 * The region of a pixmap after its empty space was trimmed
 * @param originalWidth the original width of the pixmap before trimming
 * @param originalHeight the original height of the pixmap before trimming
 * @param top the Y coordinate of the first non-empty row
 * @param bottom the Y coordinate of the last empty row, can be out of bounds by 1
 * @param left the X coordinate of the first non-empty column
 * @param right the X coordinate of the last empty row, can be out of bounds by 1
 * @see PixmapTrimmer
 * @see PixmapTrimmer#trim
 */
public record PixmapTrim(
		int originalWidth, int originalHeight,
		int top, int bottom,
		int left, int right
) {
	public int width() {
		return right - left;
	}

	public int height() {
		return bottom - top;
	}
}