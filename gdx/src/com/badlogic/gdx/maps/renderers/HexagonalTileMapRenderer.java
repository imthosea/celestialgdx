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

package com.badlogic.gdx.maps.tiled.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.TiledMap;
import com.badlogic.gdx.maps.layers.ImageLayer;
import com.badlogic.gdx.maps.layers.TileLayer;
import com.badlogic.gdx.maps.renderers.BatchTileMapRenderer;
import com.badlogic.gdx.maps.tiles.TiledMapTile;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

public class HexagonalTileMapRenderer extends BatchTileMapRenderer {

	/** true for X-Axis, false for Y-Axis */
	private boolean staggerAxisX = true;
	/** true for even StaggerIndex, false for odd */
	private boolean staggerIndexEven = false;
	/**
	 * the parameter defining the shape of the hexagon from tiled. more specifically it represents the length of the sides that
	 * are parallel to the stagger axis. e.g. with respect to the stagger axis a value of 0 results in a rhombus shape, while a
	 * value equal to the tile length/height represents a square shape and a value of 0.5 represents a regular hexagon if tile
	 * length equals tile height
	 */
	private float hexSideLength = 0f;

	private TiledMap lastMap;

	public HexagonalTileMapRenderer(Batch batch) {
		super(batch);
	}
	public HexagonalTileMapRenderer(float unitScale, Batch batch) {
		super(unitScale, batch);
	}

	private void init(TiledMap map) {
		String axis = map.getProperties().get("staggeraxis", String.class);
		if(axis != null) {
			if(axis.equals("x")) {
				staggerAxisX = true;
			} else {
				staggerAxisX = false;
			}
		}

		String index = map.getProperties().get("staggerindex", String.class);
		if(index != null) {
			if(index.equals("even")) {
				staggerIndexEven = true;
			} else {
				staggerIndexEven = false;
			}
		}

		// due to y-axis being different we need to change stagger index in even map height situations as else it would render
		// differently.
		if(!staggerAxisX && map.getProperties().get("height", Integer.class) % 2 == 0)
			staggerIndexEven = !staggerIndexEven;

		Integer length = map.getProperties().get("hexsidelength", Integer.class);
		if(length != null) {
			hexSideLength = length;
		} else {
			if(staggerAxisX) {
				length = map.getProperties().get("tilewidth", Integer.class);
				if(length != null) {
					hexSideLength = 0.5f * length;
				} else {
					TileLayer tmtl = (TileLayer) map.getLayers().get(0);
					hexSideLength = 0.5f * tmtl.getTileWidth();
				}
			} else {
				length = map.getProperties().get("tileheight", Integer.class);
				if(length != null) {
					hexSideLength = 0.5f * length;
				} else {
					TileLayer tmtl = (TileLayer) map.getLayers().get(0);
					hexSideLength = 0.5f * tmtl.getTileHeight();
				}
			}
		}
	}

	@Override
	public void renderTileLayer(TileLayer layer, long time) {
		// TODO celestialgdx this is a hack, either remove this or remove this class entirely
		TiledMap map = layer.getMap();
		if(map != lastMap) {
			lastMap = map;
			init(map);
		}

		final Color batchColor = batch.getColor();
		final float color = getTileLayerColor(layer, batchColor);

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getOffsetX() * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getOffsetY() * unitScale - viewBounds.y * (layer.getParallaxY() - 1);

		final float layerHexLength = hexSideLength * unitScale;

		if(staggerAxisX) {
			final float tileWidthLowerCorner = (layerTileWidth - layerHexLength) / 2;
			final float tileWidthUpperCorner = (layerTileWidth + layerHexLength) / 2;
			final float layerTileHeight50 = layerTileHeight * 0.50f;

			final int row1 = Math.max(0, (int) ((viewBounds.y - layerTileHeight50 - layerOffsetY) / layerTileHeight));
			final int row2 = Math.min(layerHeight,
					(int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

			final int col1 = Math.max(0, (int) (((viewBounds.x - tileWidthLowerCorner - layerOffsetX) / tileWidthUpperCorner)));
			final int col2 = Math.min(layerWidth,
					(int) ((viewBounds.x + viewBounds.width + tileWidthUpperCorner - layerOffsetX) / tileWidthUpperCorner));

			// depending on the stagger index either draw all even before the odd or vice versa
			final int colA = (staggerIndexEven == (col1 % 2 == 0)) ? col1 + 1 : col1;
			final int colB = (staggerIndexEven == (col1 % 2 == 0)) ? col1 : col1 + 1;

			for(int row = row2 - 1; row >= row1; row--) {
				for(int col = colA; col < col2; col += 2) {
					renderCell(layer, layer.getCell(col, row), tileWidthUpperCorner * col + layerOffsetX,
							layerTileHeight50 + (layerTileHeight * row) + layerOffsetY, color, time);
				}
				for(int col = colB; col < col2; col += 2) {
					renderCell(layer, layer.getCell(col, row), tileWidthUpperCorner * col + layerOffsetX,
							layerTileHeight * row + layerOffsetY, color, time);
				}
			}
		} else {
			final float tileHeightLowerCorner = (layerTileHeight - layerHexLength) / 2;
			final float tileHeightUpperCorner = (layerTileHeight + layerHexLength) / 2;
			final float layerTileWidth50 = layerTileWidth * 0.50f;

			final int row1 = Math.max(0, (int)(((viewBounds.y - tileHeightLowerCorner - layerOffsetY) / tileHeightUpperCorner)));
			final int row2 = Math.min(layerHeight,
					(int)((viewBounds.y + viewBounds.height + tileHeightUpperCorner - layerOffsetY) / tileHeightUpperCorner));

			final int col1 = Math.max(0, (int)(((viewBounds.x - layerTileWidth50 - layerOffsetX) / layerTileWidth)));
			final int col2 = Math.min(layerWidth,
					(int)((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

			float shiftX = 0;
			for(int row = row2 - 1; row >= row1; row--) {
				// depending on the stagger index either shift for even or uneven indexes
				if((row % 2 == 0) == staggerIndexEven)
					shiftX = layerTileWidth50;
				else
					shiftX = 0;
				for(int col = col1; col < col2; col++) {
					renderCell(layer, layer.getCell(col, row), layerTileWidth * col + shiftX + layerOffsetX,
							tileHeightUpperCorner * row + layerOffsetY, color, time);
				}
			}
		}
	}

	private void renderCell(TileLayer layer, TileLayer.Cell cell, float x, float y, float color, long time) {
		TiledMapTile tile = cell.tile();
		if(tile == null) return;

		TextureRegion region = tile.texture(time);

		float x1 = x + layer.getOffsetX() * unitScale;
		float y1 = y + layer.getOffsetY() * unitScale;
		float x2 = x1 + region.getRegionWidth() * unitScale;
		float y2 = y1 + region.getRegionHeight() * unitScale;

		float u1 = region.getU();
		float v1 = region.getV2();
		float u2 = region.getU2();
		float v2 = region.getV();

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
		batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
	}

	@Override
	public void renderImageLayer(ImageLayer layer) {
		final Color batchColor = batch.getColor();

		final float color = getImageLayerColor(layer, batchColor);

		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTexture();
		if(region == null) {
			return;
		}

		int tileHeight = layer.getMap().getTileHeight();
		int mapHeight = layer.getMap().getHeight();
		float layerHexLength = hexSideLength;
		// Map height if it were tiles
		float totalHeightPixels = (mapHeight * tileHeight) * unitScale;
		// To determine size of Hex map height we use (mapHeight * tileHeight(3/4)) + (layerHexLength * 0.5f)
		float hexMapHeightPixels = ((mapHeight * tileHeight * (3f / 4f)) + (layerHexLength * 0.5f)) * unitScale;

		float imageLayerYOffset = 0;
		float layerTileHeight = tileHeight * unitScale;
		float halfTileHeight = layerTileHeight * 0.5f;

		if(staggerAxisX) {
			/** If X axis staggered, must offset imagelayer y position by adding half of tileHeight to match position */
			imageLayerYOffset = halfTileHeight;
		} else {
			/** ImageLayer's y position seems to be placed at an offset determined the total height if this were a normal tile map
			 * minus the height as calculated for a hexmap. We get this number and use it to counter offset our Y. Then we will have
			 * our imagelayer matching its position in Tiled. */
			imageLayerYOffset = -(totalHeightPixels - hexMapHeightPixels);
		}

		final float x = layer.getOffsetX();
		final float y = layer.getOffsetY();
		final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1) + imageLayerYOffset;
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

}