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

package me.thosea.celestialgdx.maps.layers;

import com.badlogic.gdx.graphics.Color;
import me.thosea.celestialgdx.maps.TiledMap;

import java.util.ArrayList;
import java.util.List;

/** Map layer containing a set of MapLayers, objects and properties */
public class GroupLayer extends MapLayer {
	private final List<MapLayer> layers = new ArrayList<>();

	public GroupLayer(MapLayer parent, TiledMap map) {
		super(parent, map);
	}

	/** @return the {@link MapLayer}s owned by this group */
	public List<MapLayer> getLayers() {
		return layers;
	}

	@Override
	public void setTint(Color tintColor) {
		super.setTint(tintColor);
		layers.forEach(MapLayer::updateEffectiveTint);
	}
}