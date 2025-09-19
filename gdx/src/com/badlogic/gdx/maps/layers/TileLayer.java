/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.maps.layers;

import com.badlogic.gdx.maps.TiledMap;
import com.badlogic.gdx.maps.tiles.TiledMapTile;

/** @brief Layer for a TiledMap */
public class TileLayer extends MapLayer {
	private final int width;
	private final int height;

	private final int tileWidth;
	private final int tileHeight;

	private final Cell[][] cells;

	/** @return layer's width in tiles */
	public int getWidth() {
		return width;
	}

	/** @return layer's height in tiles */
	public int getHeight() {
		return height;
	}

	/** @return tiles' width in pixels */
	public int getTileWidth() {
		return tileWidth;
	}

	/** @return tiles' height in pixels */
	public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * Creates TiledMap layer
	 * @param width layer width in tiles
	 * @param height layer height in tiles
	 * @param tileWidth tile width in pixels
	 * @param tileHeight tile height in pixels
	 */
	public TileLayer(
			String name, MapLayer parent, TiledMap map,
			int width, int height, int tileWidth, int tileHeight
	) {
		super(name, parent, map);
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.cells = new Cell[width][height];
	}

	/**
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return {@link Cell} at (x, y)
	 */
	public Cell getCell(int x, int y) {
		if(x < 0 || x >= width) return null;
		if(y < 0 || y >= height) return null;
		return cells[x][y];
	}

	/**
	 * Sets the {@link Cell} at the given coordinates.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param cell the {@link Cell} to set at the given coordinates.
	 */
	public void setCell(int x, int y, Cell cell) {
		if(x < 0 || x >= width) return;
		if(y < 0 || y >= height) return;
		cells[x][y] = cell;
		// this.renderCache = null;
	}

	public record Cell(TiledMapTile tile, byte rotationFlags) {
		public static final byte FLIPPED_HORIZONTALLY = 1;
		public static final byte FLIPPED_VERTICALLY = 1 << 1;
		public static final byte FLIPPED_DIAGONALL = 1 << 2;
		public static final byte ROTATED_HEXAGONAL_120 = 1 << 3;

		public static byte rotation(
				boolean flipHorizontally,
				boolean flipVertically,
				boolean flipDiagonally,
				boolean rotatedhex120
		) {
			byte result = 0;
			if(flipHorizontally) result |= FLIPPED_HORIZONTALLY;
			if(flipVertically) result |= FLIPPED_VERTICALLY;
			if(flipDiagonally) result |= FLIPPED_DIAGONALL;
			if(rotatedhex120) result |= ROTATED_HEXAGONAL_120;
			return result;
		}

		public boolean isFlippedHorizontally() {
			return (rotationFlags & FLIPPED_HORIZONTALLY) != 0;
		}
		public boolean isFlippedVertically() {
			return (rotationFlags & FLIPPED_VERTICALLY) != 0;
		}
		public boolean isFlippedDiagonally() {
			return (rotationFlags & FLIPPED_DIAGONALL) != 0;
		}
		public boolean isRotatedHex120() {
			return (rotationFlags & ROTATED_HEXAGONAL_120) != 0;
		}
	}
}