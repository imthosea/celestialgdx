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

package me.thosea.celestialgdx.maps.objects;

import com.badlogic.gdx.math.Rectangle;

/** @brief Represents a rectangle shaped map object */
public class RectangleObject extends MapObject {
	private final Rectangle rectangle;

	/**
	 * Creates a {@link Rectangle} object with the given X and Y coordinates along with a given width and height.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param width Width of the {@link Rectangle} to be created.
	 * @param height Height of the {@link Rectangle} to be created.
	 */
	public RectangleObject(
			ObjectProfile profile,
			float x, float y, float width, float height
	) {
		super(profile);
		this.rectangle = new Rectangle(x, y, width, height);
	}

	/** @return rectangle shape */
	public Rectangle getRectangle() {
		return rectangle;
	}

}