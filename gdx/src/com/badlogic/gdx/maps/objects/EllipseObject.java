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

import com.badlogic.gdx.math.Ellipse;

/** @brief Represents {@link Ellipse} map objects. */
public class EllipseObject extends MapObject {
	private final Ellipse ellipse;

	/**
	 * Creates an {@link Ellipse} object with the given X and Y coordinates along with a specified width and height.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param width Width in pixels
	 * @param height Height in pixels
	 */
	public EllipseObject(
			ObjectProfile profile,
			float x, float y, float width, float height
	) {
		super(profile);
		this.ellipse = new Ellipse(x, y, width, height);
	}

	public Ellipse getEllipse() {
		return ellipse;
	}
}