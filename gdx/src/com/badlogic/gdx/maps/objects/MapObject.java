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

package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiles.TiledMapTile;
import com.badlogic.gdx.utils.Null;

public abstract class MapObject {
	public record ObjectProfile(
			String name,
			String theClass,
			int id,
			@Null TiledMapTile tile
	) {}

	private final ObjectProfile profile;
	private final MapProperties properties = new MapProperties();
	private boolean visible = true;

	protected MapObject(ObjectProfile profile) {
		this.profile = profile;
	}

	public ObjectProfile getProfile() {
		return profile;
	}

	/** @return whether the object is visible or not */
	public boolean isVisible() {
		return visible;
	}

	/** @param visible toggles object's visibility */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/** @return object's properties set */
	public MapProperties getProperties() {
		return properties;
	}
}