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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.thosea.celestialgdx.image.TextureRegion;
import me.thosea.celestialgdx.maps.layers.ImageLayer;
import me.thosea.celestialgdx.maps.layers.TileLayer;
import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

public class IsometricTileMapRenderer extends BatchTileMapRenderer {
	private final Matrix4 invIsotransform;
	private final Vector3 screenPos = new Vector3();

	private final Vector2 topRight = new Vector2();
	private final Vector2 bottomLeft = new Vector2();
	private final Vector2 topLeft = new Vector2();
	private final Vector2 bottomRight = new Vector2();

	public IsometricTileMapRenderer(Batch batch) {
		super(batch);
		this.invIsotransform = init();
	}

	public IsometricTileMapRenderer(float unitScale, Batch batch) {
		super(unitScale, batch);
		this.invIsotransform = init();
	}

	private Matrix4 init() {
		// create the isometric transform
		Matrix4 isoTransform = new Matrix4();
		isoTransform.idt();

		// isoTransform.translate(0, 32, 0);
		isoTransform.scale((float) (Math.sqrt(2.0) / 2.0), (float) (Math.sqrt(2.0) / 4.0), 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

		// ... and the inverse matrix
		return isoTransform.inv();
	}

	private Vector3 translateScreenToIso(Vector2 vec) {
		screenPos.set(vec.x, vec.y, 0);
		screenPos.mul(invIsotransform);

		return screenPos;
	}

	@Override
	protected void renderTileLayer(TileLayer layer, long time) {
		final Color batchColor = batch.getColor();
		final float color = getTileLayerColor(layer, batchColor);

		float tileWidth = layer.getTileWidth() * unitScale;
		float tileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getOffsetX() * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getOffsetY() * unitScale - viewBounds.y * (layer.getParallaxY() - 1);

		float halfTileWidth = tileWidth * 0.5f;
		float halfTileHeight = tileHeight * 0.5f;

		// setting up the screen points
		// COL1
		topRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y - layerOffsetY);
		// COL2
		bottomLeft.set(viewBounds.x - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);
		// ROW1
		topLeft.set(viewBounds.x - layerOffsetX, viewBounds.y - layerOffsetY);
		// ROW2
		bottomRight.set(viewBounds.x + viewBounds.width - layerOffsetX, viewBounds.y + viewBounds.height - layerOffsetY);

		// transforming screen coordinates to iso coordinates
		int row1 = (int) (translateScreenToIso(topLeft).y / tileWidth) - 2;
		int row2 = (int) (translateScreenToIso(bottomRight).y / tileWidth) + 2;

		int col1 = (int) (translateScreenToIso(bottomLeft).x / tileWidth) - 2;
		int col2 = (int) (translateScreenToIso(topRight).x / tileWidth) + 2;

		for(int row = row2; row >= row1; row--) {
			for(int col = col1; col <= col2; col++) {
				float x = (col * halfTileWidth) + (row * halfTileWidth);
				float y = (row * halfTileHeight) - (col * halfTileHeight);

				TileLayer.Cell cell = layer.getCell(col, row);
				if(cell == null) continue;
				TiledMapTile tile = cell.tile();

				TextureRegion region = tile.texture(time);

				float x1 = x + layerOffsetX * unitScale + layerOffsetX;
				float y1 = y + layerOffsetY * unitScale + layerOffsetY;
				float x2 = x1 + region.width * unitScale;
				float y2 = y1 + region.height * unitScale;

				float u1 = region.u;
				float v1 = region.v2;
				float u2 = region.u2;
				float v2 = region.v;

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

				if(cell.isFlippedHorizontally()) {
					float temp = vertices[U1];
					vertices[U1] = vertices[U3];
					vertices[U3] = temp;
					temp = vertices[U2];
					vertices[U2] = vertices[U4];
					vertices[U4] = temp;
				}
				if(cell.isFlippedVertically()) {
					float temp = vertices[V1];
					vertices[V1] = vertices[V3];
					vertices[V3] = temp;
					temp = vertices[V2];
					vertices[V2] = vertices[V4];
					vertices[V4] = temp;
				}
				batch.draw(region.texture, vertices, 0, NUM_VERTICES);
			}
		}
	}

	@Override
	protected void renderImageLayer(ImageLayer layer) {
		final Color batchColor = batch.getColor();

		final float color = getImageLayerColor(layer, batchColor);

		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTexture();

		if(region == null) {
			return;
		}
		/** Because of the way libGDX handles the isometric coordinates. The leftmost tile of the map begins rendering at world
		 * position 0,0, while in Tiled the y position is actually totalHeight/2 ex. Map 800px in height, leftmost tile edge starts
		 * rendering at 0,400 in tiled To compensate for that we need to subtract half the map's height in pixels then add half of
		 * the tile's height in order to position it properly in order to get a 1 to 1 rendering as to where the imagelayer renders
		 * in tiled. */
		int tileHeight = layer.getMap().getTileHeight();
		int mapHeight = layer.getMap().getHeight();
		float mapHeightPixels = (mapHeight * tileHeight) * unitScale;
		float halfTileHeight = (tileHeight * 0.5f) * unitScale;

		float x = layer.getOffsetX();
		float y = layer.getOffsetY();
		final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1) - (mapHeightPixels * 0.5f) + halfTileHeight;
		float x2 = x1 + region.width * unitScale;
		float y2 = y1 + region.height * unitScale;

		imageBounds.set(x1, y1, x2 - x1, y2 - y1);

		if(!layer.isRepeatX() && !layer.isRepeatY()) {
			if(viewBounds.contains(imageBounds) || viewBounds.overlaps(imageBounds)) {
				final float u1 = region.u;
				final float v1 = region.v2;
				final float u2 = region.u2;
				final float v2 = region.v;

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

				batch.draw(region.texture, vertices, 0, NUM_VERTICES);
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
						final float ru1 = region.u;
						final float rv1 = region.v2;
						final float ru2 = region.u2;
						final float rv2 = region.v;

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

						batch.draw(region.texture, vertices, 0, NUM_VERTICES);
					}
				}
			}
		}
	}
}