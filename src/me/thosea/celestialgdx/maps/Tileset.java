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

package me.thosea.celestialgdx.maps;

import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import java.util.Iterator;

/** @brief Set of {@link TiledMapTile} instances used to compose a TiledMapLayer */
public final class Tileset implements Iterable<TiledMapTile> {
	private final String name;
	private final TiledMapTile[] tiles;
	private final MapProperties properties;
	private final float offsetX, offsetY;

	public Tileset(
			String name, TiledMapTile[] tiles,
			MapProperties properties,
			float offsetX, float offsetY
	) {
		this.name = name;
		this.tiles = tiles;
		this.properties = properties;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return tileset's properties set
	 */
	public MapProperties getProperties() {
		return properties;
	}

	/**
	 * Gets the {@link TiledMapTile} that has the given id.
	 * @param id the id of the {@link TiledMapTile} to retrieve.
	 * @return tile matching id, null if it doesn't exist
	 */
	public TiledMapTile getTile(int id) {
		return tiles[id];
	}

	/** @return iterator to tiles in this tileset */
	@Override
	public Iterator<TiledMapTile> iterator() {
		return new Iterator<>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < tiles.length;
			}

			@Override
			public TiledMapTile next() {
				var result = tiles[index];
				index++;
				return result;
			}
		};
	}

	/** @return the size of this Tileset. */
	public int size() {
		return tiles.length;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}
}