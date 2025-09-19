/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.maps.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.layers.GroupLayer;
import com.badlogic.gdx.maps.layers.ImageLayer;
import com.badlogic.gdx.maps.layers.MapLayer;
import com.badlogic.gdx.maps.layers.TileLayer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

// TODO celestialgdx my game is ortho and all the other renderers look like
// a total hassle, so maybe remove the other ones
public abstract class BatchTileMapRenderer implements TileMapRenderer {
	static protected final int NUM_VERTICES = 20;

	protected final Batch batch;
	protected final float unitScale;

	protected final Rectangle viewBounds;
	protected final Rectangle imageBounds = new Rectangle();
	protected final Rectangle repeatedImageBounds = new Rectangle();

	protected final float[] vertices = new float[NUM_VERTICES];

	protected BatchTileMapRenderer(Batch batch) {
		this(1.0f, batch);
	}

	protected BatchTileMapRenderer(float unitScale, Batch batch) {
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.batch = batch;
	}

	public float getUnitScale() {
		return unitScale;
	}

	public Batch getBatch() {
		return batch;
	}

	public Rectangle getViewBounds() {
		return viewBounds;
	}

	public void setView(OrthographicCamera camera) {
		batch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
		float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
		viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);
	}

	public void setView(Matrix4 projection, float x, float y, float width, float height) {
		batch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}

	public void renderLayer(MapLayer layer, long time) {
		if(!layer.isVisible()) return;

		switch(layer) {
			case GroupLayer group -> {
				for(MapLayer child : group.getLayers()) {
					renderLayer(child, time);
				}
			}
			case TileLayer tile -> renderTileLayer(tile, time);
			case ImageLayer image -> renderImageLayer(image);
			default -> {}
		}
	}

	protected abstract void renderTileLayer(TileLayer layer, long time);

	protected void renderImageLayer(ImageLayer layer) {
		final Color batchColor = batch.getColor();

		final float color = getImageLayerColor(layer, batchColor);

		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTexture();

		final float x = layer.getOffsetX();
		final float y = layer.getOffsetY();
		final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1);
		final float x2 = x1 + region.getRegionWidth() * unitScale;
		final float y2 = y1 + region.getRegionHeight() * unitScale;

		imageBounds.set(x1, y1, x2 - x1, y2 - y1);

		if(!layer.isRepeatX() && !layer.isRepeatY()) {
			if(viewBounds.contains(imageBounds) || viewBounds.overlaps(imageBounds)) {
				final float u1 = region.getU();
				final float v1 = region.getV2();
				final float u2 = region.getU2();
				final float v2 = region.getV();

				vertices[X1] = x1;
				vertices[Y1] = y1;
				vertices[C1] = color;
				vertices[U1] = u1;
				vertices[V1] = v1;

				vertices[X2] = x1;
				vertices[Y2] = y2;
				vertices[C2] = color;
				vertices[U2] = u1;
				vertices[V2] = v2;

				vertices[X3] = x2;
				vertices[Y3] = y2;
				vertices[C3] = color;
				vertices[U3] = u2;
				vertices[V3] = v2;

				vertices[X4] = x2;
				vertices[Y4] = y1;
				vertices[C4] = color;
				vertices[U4] = u2;
				vertices[V4] = v1;

				batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
			}
		} else {

			// Determine number of times to repeat image across X and Y, + 4 for padding to avoid pop in/out
			int repeatX = layer.isRepeatX() ? (int) Math.ceil((viewBounds.width / imageBounds.width) + 4) : 0;
			int repeatY = layer.isRepeatY() ? (int) Math.ceil((viewBounds.height / imageBounds.height) + 4) : 0;

			// Calculate the offset of the first image to align with the camera
			float startX = viewBounds.x;
			float startY = viewBounds.y;
			startX = startX - (startX % imageBounds.width);
			startY = startY - (startY % imageBounds.height);

			for(int i = 0; i <= repeatX; i++) {
				for(int j = 0; j <= repeatY; j++) {
					float rx1 = x1;
					float ry1 = y1;
					float rx2 = x2;
					float ry2 = y2;

					// Use (i -2)/(j-2) to begin placing our repeating images outside the camera.
					// In case the image is offset, we must negate this using + (x1% imageBounds.width)
					// It's a way to get the remainder of how many images would fit between its starting position and 0
					if(layer.isRepeatX()) {
						rx1 = startX + ((i - 2) * imageBounds.width) + (x1 % imageBounds.width);
						rx2 = rx1 + imageBounds.width;
					}

					if(layer.isRepeatY()) {
						ry1 = startY + ((j - 2) * imageBounds.height) + (y1 % imageBounds.height);
						ry2 = ry1 + imageBounds.height;
					}

					repeatedImageBounds.set(rx1, ry1, rx2 - rx1, ry2 - ry1);

					if(viewBounds.contains(repeatedImageBounds) || viewBounds.overlaps(repeatedImageBounds)) {
						final float ru1 = region.getU();
						final float rv1 = region.getV2();
						final float ru2 = region.getU2();
						final float rv2 = region.getV();

						vertices[X1] = rx1;
						vertices[Y1] = ry1;
						vertices[C1] = color;
						vertices[U1] = ru1;
						vertices[V1] = rv1;

						vertices[X2] = rx1;
						vertices[Y2] = ry2;
						vertices[C2] = color;
						vertices[U2] = ru1;
						vertices[V2] = rv2;

						vertices[X3] = rx2;
						vertices[Y3] = ry2;
						vertices[C3] = color;
						vertices[U3] = ru2;
						vertices[V3] = rv2;

						vertices[X4] = rx2;
						vertices[Y4] = ry1;
						vertices[C4] = color;
						vertices[U4] = ru2;
						vertices[V4] = rv1;

						batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
					}
				}
			}
		}
	}

	/**
	 * Calculates the float color for rendering an image layer, taking into account the layer's tint color, opacity, and whether
	 * the image format supports transparency then multiplying is against the batchColor
	 * @param layer The layer to render.
	 * @param batchColor The current color of the batch.
	 * @return The float color value to use for rendering.
	 */
	protected float getImageLayerColor(ImageLayer layer, Color batchColor) {

		final Color combinedTint = layer.getBaseTint();

		// Check if layer supports transparency
		boolean supportsTransparency = layer.supportsTransparency();

		// If the Image Layer supports transparency we do not want to modify the combined tint during rendering
		// and if the Image Layer does not support transparency, we want to multiply the combined tint values, by its alpha
		float alphaMultiplier = supportsTransparency ? 1f : combinedTint.a;
		// Only modify opacity by combinedTint.b if Image Layer supports transparency
		float opacityMultiplier = supportsTransparency ? combinedTint.a : 1f;

		// For image layer rendering multiply all by alpha
		// except for opacity when image layer does not support transparency
		return Color.toFloatBits(batchColor.r * (combinedTint.r * alphaMultiplier),
				batchColor.g * (combinedTint.g * alphaMultiplier), batchColor.b * (combinedTint.b * alphaMultiplier),
				batchColor.a * (layer.getOpacity() * opacityMultiplier));
	}

	/**
	 * Calculates the float color for rendering a tile layer, taking into account the layer's tint color and opacity, then
	 * multiplying is against the batchColor
	 * @param layer
	 * @param batchColor
	 */
	protected float getTileLayerColor(TileLayer layer, Color batchColor) {
		return Color.toFloatBits(batchColor.r * layer.getBaseTint().r, batchColor.g * layer.getBaseTint().g,
				batchColor.b * layer.getBaseTint().b, batchColor.a * layer.getBaseTint().a * layer.getOpacity());
	}
}