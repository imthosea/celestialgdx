/**
 *
 */

package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.math.Polygon;

/** @brief Represents {@link Polygon} map objects */
public class PolygonObject extends MapObject {
	private final Polygon polygon;

	public PolygonObject(ObjectProfile profile, Polygon polygon) {
		super(profile);
		this.polygon = polygon;
	}

	public PolygonObject(ObjectProfile profile, float[] verticies) {
		this(profile, new Polygon(verticies));
	}

	/** @return polygon shape */
	public Polygon getPolygon() {
		return polygon;
	}
}