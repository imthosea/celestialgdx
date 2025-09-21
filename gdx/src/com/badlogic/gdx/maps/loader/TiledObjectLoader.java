package com.badlogic.gdx.maps.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.loader.TiledObjectLoader.ObjectParser.ObjectParseContext;
import com.badlogic.gdx.maps.objects.EllipseObject;
import com.badlogic.gdx.maps.objects.MapObject;
import com.badlogic.gdx.maps.objects.MapObject.ObjectProfile;
import com.badlogic.gdx.maps.objects.PointObject;
import com.badlogic.gdx.maps.objects.PolygonObject;
import com.badlogic.gdx.maps.objects.PolylineObject;
import com.badlogic.gdx.maps.objects.RectangleObject;
import com.badlogic.gdx.maps.objects.TextObject;
import com.badlogic.gdx.maps.objects.TextObject.TextHAlign;
import com.badlogic.gdx.maps.objects.TextObject.TextVAlign;
import com.badlogic.gdx.maps.tiles.TiledMapTile;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.badlogic.gdx.maps.loader.TiledObjectLoader.ObjectParser.parsePoints;

// https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#tmx-object
public final class TiledObjectLoader {
	private TiledObjectLoader() {}

	private static final Map<String, ObjectParser<?>> parsers = new HashMap<>(Map.of(
			// for some reason, rectangles don't have any ID
			"", ctx -> new RectangleObject(ctx.profile, ctx.x, ctx.y, ctx.width, ctx.height),
			"ellipse", ctx -> new EllipseObject(ctx.profile, ctx.x, ctx.y, ctx.width, ctx.height),
			"point", ctx -> new PointObject(ctx.profile, ctx.x, ctx.y),
			"polygon", ctx -> new PolygonObject(ctx.profile, parsePoints(ctx)),
			"polyline", ctx -> new PolylineObject(ctx.profile, parsePoints(ctx)),
			"text", ctx -> {
				Element element = ctx.element;
				TextObject obj = new TextObject(ctx.profile, ctx.x, ctx.y, ctx.width, ctx.height);
				obj.text = element.getText();
				obj.fontFamily = element.getAttribute("fontfamily");
				obj.wrap = "1".equals(element.getAttribute("wrap")); // default: true
				obj.color = Color.valueOf(TiledLoaderUtils.tiledToGdxColor(element.getAttribute("color", "#000000")));
				obj.bold = "1".equals(element.getAttribute("bold"));
				obj.italic = "1".equals(element.getAttribute("italic"));
				obj.underline = "1".equals(element.getAttribute("underline"));
				obj.strikeout = "1".equals(element.getAttribute("strikeout"));
				obj.kerning = !"0".equals(element.getAttribute("kerning")); // default: false
				obj.horizontalAlign = TextHAlign.of(element.getAttribute("halign", "left"));
				obj.verticalAlign = TextVAlign.of(element.getAttribute("valign", "top"));
				return obj;
			}
	));

	@FunctionalInterface
	public interface ObjectParser<T extends MapObject> {
		record ObjectParseContext(
				Element element, ObjectProfile profile,
				boolean flipY, float scaleX, float scaleY,
				float x, float y, float width, float height
		) {}

		T parse(ObjectParseContext ctx);

		/**
		 * Reads the "points" attribute used in polygons and polylines
		 * @return vertices
		 */
		static float[] parsePoints(ObjectParseContext ctx) {
			String[] points = ctx.element.getAttribute("points").split(" ");
			float[] vertices = new float[points.length * 2];
			for(int i = 0; i < points.length; i++) {
				String[] point = points[i].split(",");
				vertices[i * 2] = Float.parseFloat(point[0]) * ctx.scaleX;
				vertices[i * 2 + 1] = Float.parseFloat(point[1]) * ctx.scaleY * (ctx.flipY ? -1 : 1);
			}
			return vertices;
		}
	}

	public static void registerType(String name, ObjectParser<?> parser) {
		parsers.put(name, parser);
	}

	public static ObjectParser<?> getParser(String name) {
		return parsers.get(name);
	}

	public static MapObject read(
			Element element,
			float heightInPixels, boolean flipY,
			float scaleX, float scaleY,
			Function<Integer, TiledMapTile> tileSupplier
	) {
		int id = element.getIntAttribute("id");
		String name = element.getAttribute("name", "");
		String clazz = element.getAttribute("type", "");

		float x = element.getFloatAttribute("x", 0) * scaleX;
		float y = element.getFloatAttribute("y", 0);
		if(flipY) y = heightInPixels - y;
		y *= scaleY;

		float width = element.getFloatAttribute("width", 0) * scaleX;
		float height = element.getFloatAttribute("height", 0) * scaleY;

		int gid = element.getIntAttribute("gid", -1);
		TiledMapTile tile = gid != -1 ? tileSupplier.apply(gid) : null;

		// TODO template support

		ObjectParser<?> parser;
		Element subElement;
		if(element.getChildCount() <= 0) {
			// rectangles don't specify any ID
			subElement = null;
			parser = getParser("");
		} else {
			subElement = element.getChild(0);
			parser = getParser(subElement.getName());
		}

		MapObject result = parser.parse(new ObjectParseContext(
				subElement,
				new ObjectProfile(name, clazz, id, tile),
				flipY, scaleX, scaleY,
				x, y, width, height
		));
		result.setVisible(element.getAttribute("visible", "1").equals("1"));
		return result;
	}
}