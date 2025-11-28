package me.thosea.celestialgdx.maps.layers;

import me.thosea.celestialgdx.maps.TiledMap;
import me.thosea.celestialgdx.maps.objects.MapObject;

import java.util.ArrayList;
import java.util.List;

/**
 * celestialgdx - map layer with objects
 */
// https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#objectgroup
public class ObjectLayer extends MapLayer {
	private final List<MapObject> objects = new ArrayList<>();

	public ObjectLayer(MapLayer parent, TiledMap map) {
		super(parent, map);
	}

	public List<MapObject> getObjects() {
		return objects;
	}
}