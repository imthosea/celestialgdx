package me.thosea.celestialgdx.maps.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.XmlElement;
import me.thosea.celestialgdx.maps.loader.TiledObjectLoader.ObjectParser.ObjectParseContext;
import me.thosea.celestialgdx.maps.objects.EllipseObject;
import me.thosea.celestialgdx.maps.objects.MapObject;
import me.thosea.celestialgdx.maps.objects.MapObject.ObjectProfile;
import me.thosea.celestialgdx.maps.objects.PointObject;
import me.thosea.celestialgdx.maps.objects.PolygonObject;
import me.thosea.celestialgdx.maps.objects.PolylineObject;
import me.thosea.celestialgdx.maps.objects.RectangleObject;
import me.thosea.celestialgdx.maps.objects.TextObject;
import me.thosea.celestialgdx.maps.objects.TextObject.TextHAlign;
import me.thosea.celestialgdx.maps.objects.TextObject.TextVAlign;
import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static me.thosea.celestialgdx.maps.loader.TiledObjectLoader.ObjectParser.parsePoints;

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
				XmlElement element = ctx.element;
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
				XmlElement element, ObjectProfile profile,
				boolean flipY, float scaleX, float scaleY,
				float x, float y, float width, float height
		) {}

		T parse(ObjectParseContext ctx);

		/**
		 * Reads the "points" attribute used in polygons and polylines
		 * @return vertices
		 */
		static float[] parsePoints(ObjectParseContext ctx) {
			String[] points = ctx.element.expectAttribute("points").split(" ");
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
			XmlElement xml,
			float heightInPixels, boolean flipY,
			float scaleX, float scaleY,
			Function<Integer, TiledMapTile> tileSupplier
	) {
		int id = xml.getIntAttribute("id");
		String name = xml.getAttribute("name", "");
		String clazz = xml.getAttribute("type", "");

		float x = xml.getFloatAttribute("x", 0) * scaleX;
		float y = xml.getFloatAttribute("y", 0);
		if(flipY) y = heightInPixels - y;
		y *= scaleY;

		float width = xml.getFloatAttribute("width", 0) * scaleX;
		float height = xml.getFloatAttribute("height", 0) * scaleY;

		int gid = xml.getIntAttribute("gid", -1);
		TiledMapTile tile = gid != -1 ? tileSupplier.apply(gid) : null;

		// TODO template support

		List<XmlElement> children = xml.getChildren();
		ObjectParser<?> parser;
		XmlElement subElement;
		if(children.isEmpty()) {
			// rectangles don't specify any ID
			subElement = null;
			parser = getParser("");
		} else {
			subElement = children.getFirst();
			parser = getParser(subElement.getName());
		}

		MapObject result = parser.parse(new ObjectParseContext(
				subElement,
				new ObjectProfile(name, clazz, id, tile),
				flipY, scaleX, scaleY,
				x, y, width, height
		));
		result.setVisible(xml.getAttribute("visible", "1").equals("1"));
		return result;
	}
}