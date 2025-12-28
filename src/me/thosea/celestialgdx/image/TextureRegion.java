package me.thosea.celestialgdx.image;

import java.util.Objects;

/**
 * A rectangular region of a {@link Texture}. The origin of the coordinate system is the top-left.
 */
public final class TextureRegion {
	public final Texture texture;
	public final float u;
	public final float v;
	public final float u2;
	public final float v2;

	public final int width;
	public final int height;

	public TextureRegion(Texture texture) {
		Objects.requireNonNull(texture);
		this.texture = texture;
		this.u = 0f;
		this.v = 1f;
		this.u2 = 0f;
		this.v2 = 1f;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
	}

	public TextureRegion(Texture texture, float u, float v, float u2, float v2) {
		Objects.requireNonNull(texture);
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.u2 = u2;
		this.v2 = v2;
		this.width = Math.round(Math.abs(u2 - u) * texture.getWidth());
		this.height = Math.round(Math.abs(v2 - v) * texture.getHeight());
	}

	public TextureRegion(Texture texture, int x, int y, int width, int height) {
		Objects.requireNonNull(texture);
		this.texture = texture;

		float texWidthRcp = 1f / texture.getWidth();
		float texHeightRcp = 1f / texture.getHeight();
		this.u = x * texWidthRcp;
		this.v = y * texHeightRcp;
		this.u2 = (x + width) * texWidthRcp;
		this.v2 = (y + height) * texHeightRcp;

		this.width = width;
		this.height = height;
	}

	public TextureRegion(TextureRegion region, int x, int y, int width, int height) {
		this(region.texture, x + region.getRegionX(), y + region.getRegionY(), width, height);
	}

	public int getRegionX() {
		return Math.round(this.u * this.texture.getWidth());
	}
	public int getRegionY() {
		return Math.round(this.v * this.texture.getHeight());
	}

	public TextureRegion flipped(boolean flipX, boolean flipY) {
		if(!flipX && !flipY) return this;
		return new TextureRegion(
				texture,
				/*u*/ flipX ? u2 : u,
				/*v*/ flipY ? v2 : v,
				/*u2*/ flipX ? u : u2,
				/*v2*/ flipY ? v : u2
		);
	}

	public TextureRegion scrolled(float xPercent, float yPercent) {
		return new TextureRegion(
				texture,
				(u + xPercent) % 1f,
				(v + yPercent) % 1f,
				(u2 + xPercent) % 1f,
				(v2 + yPercent) % 1f
		);
	}
}