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

import com.badlogic.gdx.math.Polyline;

/** @brief Represents {@link Polyline} map objects */
public class PolylineObject extends MapObject {
	private final Polyline polyline;

	public PolylineObject(ObjectProfile profile, Polyline polyline) {
		super(profile);
		this.polyline = polyline;
	}

	public PolylineObject(ObjectProfile profile, float[] verticies) {
		this(profile, new Polyline(verticies));
	}

	/** @return polyline shape */
	public Polyline getPolyline() {
		return polyline;
	}
}