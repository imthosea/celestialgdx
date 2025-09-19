package com.badlogic.gdx.maps.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.loader.element.ClassElement;
import com.badlogic.gdx.maps.loader.element.MapElement;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * celestialgdx - utilities for tmx, tsx and tiled project file loading
 */
public final class TiledLoaderUtils {
	private TiledLoaderUtils() {}

	/**
	 * Representation of a single Tiled class property. A property has:
	 * <ul>
	 * <li>a property {@code name}</li>
	 * <li>a property {@code type} like string, int, ...</li>
	 * <li>an optional {@code propertyType} for class and enum types to refer to a specific class/enum</li>
	 * <li>a {@code defaultValue}</li>
	 * </ul>
	 */
	public record ProjectClassMember(
			String type,
			String name,
			String propertyType,
			JsonValue defaultValue
	) {}

	@FunctionalInterface
	public interface TiledPropParser {
		Object parse(String value);
	}

	@FunctionalInterface
	public interface ClassSupplier {
		@Nullable
		Iterable<TiledLoaderUtils.ProjectClassMember> getClassMembers(String name);
	}

	private static final Map<String, TiledPropParser> parsers = new HashMap<>(Map.of(
			"string", val -> val,
			"file", val -> val,
			"int", Integer::parseInt,
			"object", Integer::parseInt, // celestialgdx - objects are no longer resolved
			"float", Float::parseFloat,
			"bool", Boolean::parseBoolean,
			"color", val -> Color.valueOf(tiledToGdxColor(val))
	));

	public static void registerParser(String typeName, TiledPropParser parser) {
		parsers.put(typeName, parser);
	}

	public static TiledPropParser getParser(String typeName) {
		return parsers.get(typeName != null ? typeName : "string");
	}

	public static void loadPropertiesFor(
			MapProperties properties,
			XmlReader.Element xml,
			@Nullable ClassSupplier classSupplier
	) {
		XmlReader.Element element = xml.getChildByName("properties");
		if(element != null) {
			loadProperties(properties, MapElement.xml(element), classSupplier);
		}
	}

	public static void loadProperties(
			MapProperties properties,
			MapElement element,
			@Nullable ClassSupplier classSupplier
	) {
		if(element == null) return;

		for(MapElement property : element.getChildren()) {
			String name = property.getAttribute("name");
			String type = property.getAttribute("type");

			if("class".equals(type)) {
				properties.put(name, loadClassProperties(
						property.getAttribute("propertype"),
						property.getClassElements(),
						classSupplier
				));
				continue;
			}

			TiledPropParser parser = getParser(type);
			if(parser == null) {
				throw new GdxRuntimeException(
						"Unknown property type \"%s\" for element \"%s\"".formatted(name, type)
				);
			}

			String value = property.getAttribute("value");
			properties.put(name, parser.parse(value));
		}
	}

	private static MapProperties loadClassProperties(
			String className,
			@Nullable Map<String, ClassElement> elements,
			ClassSupplier classSupplier
	) {
		Iterable<ProjectClassMember> members;
		if(classSupplier == null || (members = classSupplier.getClassMembers(className)) == null) {
			throw new GdxRuntimeException(
					"Class \"%s\" not found. Did you set a tiled project?".formatted(className)
			);
		}

		MapProperties result = new MapProperties();
		result.put("type", className);

		for(ProjectClassMember member : members) {
			String name = member.name();
			String type = member.type();
			ClassElement prop = elements != null
					? elements.get(name)
					: ClassElement.json(member.defaultValue());

			if("class".equals(type)) {
				result.put(name, loadClassProperties(
						member.propertyType(),
						prop.getClassElements(),
						classSupplier
				));
				continue;
			}

			TiledPropParser parser = getParser(member.type());
			if(parser == null) {
				throw new GdxRuntimeException(String.format(
						"Class \"%s\" has unknown property type \"%s\" for element \"%s\"",
						className, member.type(), name
				));
			}

			result.put(name, prop.value());
		}

		return result;
	}

	/**
	 * Converts Tiled's color format #AARRGGBB to a libGDX appropriate #RRGGBBAA The Tiled Map Editor uses the color format
	 * #AARRGGBB But note, if the alpha of the color is set to 255, Tiled does not include it as part of the color code in the .tmx
	 * ex. Red (r:255,g:0,b:0,a:255) becomes #ff0000, Red (r:255,g:0,b:0,a:127) becomes #7fff0000
	 * @param tiledColor A String representing a color in Tiled's #AARRGGBB format
	 * @return A String representing the color in the #RRGGBBAA format
	 */
	public static String tiledToGdxColor(String tiledColor) {
		String alpha = tiledColor.length() == 9 ? tiledColor.substring(1, 3) : "ff";
		String color = tiledColor.length() == 9 ? tiledColor.substring(3) : tiledColor.substring(1);
		return color + alpha;
	}
}