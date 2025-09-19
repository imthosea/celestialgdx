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

package com.badlogic.gdx.maps.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Null;

/** @brief Represents a non changing {@link TiledMapTile} (can be cached) */
public record StaticMapTile(
		int id,
		boolean isBlended,
		@Null MapProperties properties,
		TextureRegion texture
) implements TiledMapTile {
	public StaticMapTile(TiledMapTile base, MapProperties prop) {
		this(base.id(), base.isBlended(), prop, base.texture(0));
	}

	@Override
	public boolean isStaticTexture() {
		return true;
	}

	@Override
	public TextureRegion texture(long time) {
		return texture;
	}
}