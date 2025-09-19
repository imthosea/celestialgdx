package com.badlogic.gdx.maps.layers;

import com.badlogic.gdx.maps.TiledMap;
import com.badlogic.gdx.maps.objects.MapObject;

import java.util.ArrayList;
import java.util.List;

/**
 * celestialgdx - map layer with objects
 */
// https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#objectgroup
public class ObjectLayer extends MapLayer {
	private final List<MapObject> objects = new ArrayList<>();

	public ObjectLayer(String name, MapLayer parent, TiledMap map) {
		super(name, parent, map);
	}

	public List<MapObject> getObjects() {
		return objects;
	}
}