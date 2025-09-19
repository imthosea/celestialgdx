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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.TiledMap;

/** Map layer containing a set of objects and properties */
public class MapLayer {
	private final String name;
	private final MapLayer parent;
	private final TiledMap map;

	private final MapProperties properties = new MapProperties();

	private final Color baseTint = new Color(Color.WHITE);
	private final Color effectiveTint = new Color(Color.WHITE);

	private float offsetX;
	private float offsetY;

	private float opacity = 1.0f;
	private boolean visible = true;
	private float parallaxX = 1;
	private float parallaxY = 1;

	public MapLayer(String name, MapLayer parent, TiledMap map) {
		this.name = name;
		this.parent = parent;
		this.map = map;
	}

	/** @return layer's name */
	public String getName() {
		return name;
	}

	/** @return layer's opacity */
	public float getOpacity() {
		return parent != null ? opacity * parent.getOpacity() : opacity;
	}

	/** @param opacity new opacity for the layer */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	/** @return tint before combining with the parent layer */
	public Color getBaseTint() {
		return baseTint;
	}

	/** @return tint combined with its parent */
	public Color getEffectiveTint() {
		return effectiveTint;
	}

	protected void updateEffectiveTint() {
		if(parent != null) {
			var parentColor = parent.effectiveTint;
			effectiveTint.set(
					baseTint.r * parentColor.r,
					baseTint.g * parentColor.g,
					baseTint.b * parentColor.b,
					baseTint.a * parentColor.a
			);
		} else {
			effectiveTint.set(baseTint);
		}
	}

	/** @param tintColor new tint color for the layer */
	public void setTint(Color tintColor) {
		this.baseTint.set(tintColor);
		updateEffectiveTint();
	}

	/** @return layer's x offset */
	public float getOffsetX() {
		return parent != null ? offsetX + parent.getOffsetX() : offsetX;
	}

	/** @param offsetX new x offset for the layer */
	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	/** @return layer's y offset */
	public float getOffsetY() {
		return parent != null ? offsetY + parent.getOffsetY() : offsetY;
	}

	/** @param offsetY new y offset for the layer */
	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	/** @return layer's parallax scrolling factor for x-axis */
	public float getParallaxX() {
		return parent != null ? parallaxX * parent.getParallaxX() : parallaxX;
	}

	public void setParallaxX(float parallaxX) {
		this.parallaxX = parallaxX;
	}

	/** @return layer's parallax scrolling factor for y-axis */
	public float getParallaxY() {
		return parent != null ? parallaxY * parent.getParallaxY() : parallaxY;
	}

	public void setParallaxY(float parallaxY) {
		this.parallaxY = parallaxY;
	}

	/** @return the layer's parent {@link MapLayer}, or null if the layer does not have a parent **/
	public MapLayer getParent() {
		return parent;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public TiledMap getMap() {
		return map;
	}

	public MapProperties getProperties() {
		return properties;
	}
}