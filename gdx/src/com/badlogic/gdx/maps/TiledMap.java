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

package com.badlogic.gdx.maps;

import com.badlogic.gdx.maps.layers.MapLayer;

import java.util.List;

/**
 * Represents a tiled map
 */
// celestialgdx -
// when trying to maintain, keeping
// https://doc.mapeditor.org/en/stable/reference/tmx-map-format/
// open is very useful!!
public class TiledMap {
	private final MapProperties properties;
	private final List<MapLayer> layers;

	private final int width;
	private final int height;

	private final int tileWidth;
	private final int tileHeight;

	public TiledMap(
			MapProperties properties, List<MapLayer> layers,
			int width, int height, int tileWidth, int tileHeight
	) {
		this.properties = properties;
		this.layers = layers;
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	public MapProperties getProperties() {
		return properties;
	}

	public List<MapLayer> getLayers() {
		return layers;
	}

	/**
	 * Width in tiles of the map
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * Height in tiles of the map
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * Width of each tile on the map
	 */
	public int getTileWidth() {
		return tileWidth;
	}
	/**
	 * Height of each tile on the map
	 */
	public int getTileHeight() {
		return tileHeight;
	}
}