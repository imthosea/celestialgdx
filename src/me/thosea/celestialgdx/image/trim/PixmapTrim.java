package me.thosea.celestialgdx.image.trim;

/**
 * The region of a pixmap after its empty space was trimmed.
 * @param originalWidth the original width of the pixmap before trimming
 * @param originalHeight the original height of the pixmap before trimming
 * @param top the Y coordinate of the first non-empty row. between 0 and {@link #originalHeight}.
 * @param bottom the Y coordinate of the last empty row, between {@link #originalHeight} and 0.
 * @param left the X coordinate of the first non-empty column. between 0 and {@link #originalWidth}.
 * @param right the X coordinate of the last empty column. between {@link #originalWidth} and 0.
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