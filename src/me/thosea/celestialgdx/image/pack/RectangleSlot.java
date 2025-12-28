package me.thosea.celestialgdx.image.pack;

import me.thosea.celestialgdx.image.Texture;
import me.thosea.celestialgdx.image.TextureRegion;

/**
 * A slot in a rectangle.
 * Can be obtained from {@link RectPacker} or {@link PixmapPacker}.
 * @param x the start X
 * @param y the start Y
 * @param width the width of the region
 * @param height the height of the region
 */
public record RectangleSlot(int x, int y, int width, int height) {
	public RectangleSlot withSize(int width, int height) {
		return new RectangleSlot(this.x, this.y, width, height);
	}

	public TextureRegion toTextureRegion(Texture texture) {
		return new TextureRegion(texture, x, y, width, height);
	}
}