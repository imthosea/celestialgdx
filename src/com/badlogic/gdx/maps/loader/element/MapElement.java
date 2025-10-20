package com.badlogic.gdx.maps.loader.element;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * celestialgdx - abstraction between XML and JSON for tiled property loading
 */
public interface MapElement {
	String getAttribute(String name);

	List<MapElement> getChildren();

	Map<String, ClassElement> getClassElements();

	static MapElement xml(XmlElement element) {
		return element == null ? null : new MapElement() {
			@Override
			public String getAttribute(String name) {
				return element.getAttribute(name);
			}

			@Override
			public List<MapElement> getChildren() {
				return element.getChildren().stream()
						.map(MapElement::xml)
						.toList();
			}

			@Override
			public Map<String, ClassElement> getClassElements() {
				var properties = element.getChildByName("properties");
				if(properties == null) return null;

				List<XmlElement> elements = properties.getChildren();
				var result = new HashMap<String, ClassElement>(elements.size());
				for(XmlElement element : elements) {
					result.put(element.expectAttribute("name"), ClassElement.xml(element));
				}
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
				if(value == null) return Map.of();

				var result = new HashMap<String, ClassElement>();
				value.iterator().forEach(element -> {
					result.put(element.name, ClassElement.json(element));
				});
				return result;
			}
		};
	}
}