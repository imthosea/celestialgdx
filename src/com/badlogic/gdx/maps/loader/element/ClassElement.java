package com.badlogic.gdx.maps.loader.element;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlElement;

import java.util.HashMap;
import java.util.Map;

// unlike in TMX, JSON properties change their format
// to a much more basic direct key-value pair as opposed to
// an array of objects when in the context of a class value

// subsequently we need this abomination of a lowest-common-denominator for parsing class values
// - thosea

public interface ClassElement {
	String value();

	Map<String, ClassElement> getClassElements();

	static ClassElement xml(XmlElement element) {
		MapElement delegate = MapElement.xml(element);
		return new ClassElement() {
			@Override
			public String value() {
				return delegate.getAttribute("value");
			}

			@Override
			public Map<String, ClassElement> getClassElements() {
				return delegate.getClassElements();
			}
		};
	}

	static ClassElement json(JsonValue value) {
		return new ClassElement() {
			@Override
			public String value() {
				return value.asString();
			}

			@Override
			public Map<String, ClassElement> getClassElements() {
				var result = new HashMap<String, ClassElement>();
				value.iterator().forEach(element -> {
					result.put(element.name, json(element));
				});
				return result;
			}
		};
	}
}