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
import me.thosea.celestialgdx.image.TextureRegion;
import me.thosea.celestialgdx.maps.layers.TileLayer;
import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import static com.badlogic.gdx.graphics.g2d.Batch.*;

public class OrthogonalTileMapRenderer extends BatchTileMapRenderer {
	public OrthogonalTileMapRenderer(Batch batch) {
		super(batch);
	}
	public OrthogonalTileMapRenderer(float unitScale, Batch batch) {
		super(unitScale, batch);
	}

	@Override
	public void renderTileLayer(TileLayer layer, long time) {
		final Color batchColor = batch.getColor();
		final float color = getTileLayerColor(layer, batchColor);

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getOffsetX() * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getOffsetY() * unitScale - viewBounds.y * (layer.getParallaxY() - 1);

		final int col1 = Math.max(0, (int) ((viewBounds.x - layerOffsetX) / layerTileWidth));
		final int col2 = Math.min(layerWidth,
				(int) ((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

		final int row1 = Math.max(0, (int) ((viewBounds.y - layerOffsetY) / layerTileHeight));
		final int row2 = Math.min(layerHeight,
				(int) ((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

		float y = row2 * layerTileHeight + layerOffsetY;
		float xStart = col1 * layerTileWidth + layerOffsetX;
		final float[] vertices = this.vertices;

		for(int row = row2; row >= row1; row--) {
			float x = xStart;
			for(int col = col1; col < col2; col++) {
				TileLayer.Cell cell = layer.getCell(col, row);
				if(cell == null) {
					x += layerTileWidth;
					continue;
				}
				TiledMapTile tile = cell.tile();

				TextureRegion region = tile.texture(time);

				float x1 = x + layer.getOffsetX() * unitScale;
				float y1 = y + layer.getOffsetY() * unitScale;
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

				batch.draw(region.texture, vertices, 0, 20);
				x += layerTileWidth;
			}
			y -= layerTileHeight;
		}
	}
}