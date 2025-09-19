package com.badlogic.gdx.maps.loader.element;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * celestialgdx - abstraction between XML and JSON for tiled property loading
 */
public interface MapElement {
	String getAttribute(String name);

	List<MapElement> getChildren();

	Map<String, ClassElement> getClassElements();

	static MapElement xml(XmlReader.Element element) {
		return element == null ? null : new MapElement() {
			@Override
			public String getAttribute(String name) {
				return element.getAttribute(name);
			}

			@Override
			public List<MapElement> getChildren() {
				return Stream.of(element.getChildren().items)
						.map(MapElement::xml)
						.toList();
			}

			@Override
			public Map<String, ClassElement> getClassElements() {
				var properties = element.getChildByName("properties");
				if(properties == null) return null;

				var result = new HashMap<String, ClassElement>();
				properties.getChildren().forEach(element -> {
					result.put(element.getAttribute("name"), ClassElement.xml(element));
				});
				return result;
			}
		};
	}

	static MapElement json(JsonValue element) {
		return element == null ? null : new MapElement() {
			@Override
			public String getAttribute(String name) {
				return element.getString(name);
			}

			@Override
			public List<MapElement> getChildren() {
				List<MapElement> result = new ArrayList<>();
				element.iterator().forEach(value -> result.add(json(value)));
				return result;
			}

			@Override
			public Map<String, ClassElement> getClassElements() {
				JsonValue value = element.get("value");
				var result = new HashMap<String, ClassElement>();
				value.iterator().forEach(element -> {
					result.put(element.name, ClassElement.json(element));
				});
				return result;
			}
		};
	}
}