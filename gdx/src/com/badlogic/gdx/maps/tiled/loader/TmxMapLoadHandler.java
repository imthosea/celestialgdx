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

package com.badlogic.gdx.maps.tiled.loader;

import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import static com.badlogic.gdx.maps.ImageResolver.getRelativeFileHandle;

// TODO celestialgdx
// what the HELL is going on here??
// i spent 3 weeks trying to figure this out and patch it ... i still don't understand it
// i should really rewrite this in the future, but i need to stop procrastinating and make my game already
public class TmxMapLoadHandler<P extends BaseTiledMapLoadHandler.Parameters> extends BaseTiledMapLoadHandler<P> {

	protected final XmlReader xml = new XmlReader();
	protected final Element root;

	public TmxMapLoadHandler(FileHandle file, char[] fileData,
	                         @Null char[] projectFileData,
	                         P parameter) {
		super(file, fileData, projectFileData, parameter);
		this.root = xml.parse(fileData, 0, fileData.length);
	}

	public static MapLoader<BaseTiledMapLoadHandler.Parameters> createLoader(FileHandleResolver resolver) {
		return new MapLoader<>(resolver, TmxMapLoadHandler::new);
	}

	/**
	 * Loads the map data, given the XML root element
	 */
	@Override
	protected void parseMap() {
		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		int hexSideLength = root.getIntAttribute("hexsidelength", 0);
		String staggerAxis = root.getAttribute("staggeraxis", null);
		String staggerIndex = root.getAttribute("staggerindex", null);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

		MapProperties prop = map.getProperties();
		prop.put("width", mapWidth);
		prop.put("height", mapHeight);
		prop.put("tilewidth", tileWidth);
		prop.put("tileheight", tileHeight);
		prop.put("hexsidelength", hexSideLength);
		if(staggerAxis != null) {
			prop.put("staggeraxis", staggerAxis);
		}
		if(staggerIndex != null) {
			prop.put("staggerindex", staggerIndex);
		}
		if(mapBackgroundColor != null) {
			prop.put("backgroundcolor", mapBackgroundColor);
		}

		this.mapTileWidth = tileWidth;
		this.mapTileHeight = tileHeight;
		this.mapWidthInPixels = mapWidth * tileWidth;
		this.mapHeightInPixels = mapHeight * tileHeight;

		if(mapOrientation != null) {
			prop.put("orientation", mapOrientation);
			if("staggered".equals(mapOrientation)) {
				if(mapHeight > 1) {
					this.mapWidthInPixels += tileWidth / 2;
					this.mapHeightInPixels = mapHeightInPixels / 2 + tileHeight / 2;
				}
			}
		}

		Element properties = root.getChildByName("properties");
		if(properties != null) {
			loadProperties(map.getProperties(), properties);
		}

		Array<Element> tilesets = root.getChildrenByName("tileset");
		for(Element element : tilesets) {
			loadTileSet(element);
			root.removeChild(element);
		}

		for(int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			loadLayer(map.getLayers(), element);
		}

		// update hierarchical parallax scrolling factors
		// in Tiled the final parallax scrolling factor of a layer is the multiplication of its factor with all its parents
		// 1) get top level groups
		final Array<MapGroupLayer> groups = map.getLayers().getByType(MapGroupLayer.class);
		while(groups.notEmpty()) {
			final MapGroupLayer group = groups.first();
			groups.removeIndex(0);

			for(MapLayer child : group.getLayers()) {
				child.setParallaxX(child.getParallaxX() * group.getParallaxX());
				child.setParallaxY(child.getParallaxY() * group.getParallaxY());
				if(child instanceof MapGroupLayer layer) {
					// 2) handle any child groups
					groups.add(layer);
				}
			}
		}

		for(Runnable runnable : runAfterParse) {
			runnable.run();
		}
	}

	@Override
	protected void loadTilesets(AssetLoadingContext<TiledMap> ctx) {
		Function<String, TextureRegion> supplier = name -> imageResolver.getImage(ctx, file, name);
		tilesetHandlers.forEach(action -> action.handle(supplier));
	}

	protected void loadLayer(MapLayers parentLayers, Element element) {
		String name = element.getName();
		switch(name) {
			case "group" -> loadLayerGroup(parentLayers, element);
			case "layer" -> loadTileLayer(parentLayers, element);
			case "objectgroup" -> loadObjectGroup(parentLayers, element);
			case "imagelayer" -> loadImageLayer(parentLayers, element);
			default -> {} // TODO celestialgdx - should this throw?
		}
	}

	protected void loadLayerGroup(MapLayers parentLayers, Element element) {
		MapGroupLayer groupLayer = new MapGroupLayer();
		loadBasicLayerInfo(groupLayer, element);

		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(groupLayer.getProperties(), properties);
		}

		for(int i = 0; i < element.getChildCount(); i++) {
			Element child = element.getChild(i);
			loadLayer(groupLayer.getLayers(), child);
		}

		for(MapLayer layer : groupLayer.getLayers()) {
			layer.setParent(groupLayer);
		}

		parentLayers.add(groupLayer);
	}

	protected void loadTileLayer(MapLayers parentLayers, Element element) {
		int width = element.getIntAttribute("width", 0);
		int height = element.getIntAttribute("height", 0);
		int tileWidth = map.getProperties().get("tilewidth", Integer.class);
		int tileHeight = map.getProperties().get("tileheight", Integer.class);

		TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
		loadBasicLayerInfo(layer, element);

		int[] ids = getTileIds(element, width, height);
		TiledMapTileSets tilesets = map.getTileSets();
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int id = ids[y * width + x];
				boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
				boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
				boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

				TiledMapTile tile = tilesets.getTile(id & ~MASK_CLEAR);
				if(tile != null) {
					Cell cell = createTileLayerCell(flipHorizontally, flipVertically, flipDiagonally);
					cell.setTile(tile);
					layer.setCell(x, flipY ? height - 1 - y : y, cell);
				}
			}
		}

		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(layer.getProperties(), properties);
		}
		parentLayers.add(layer);
	}

	protected void loadObjectGroup(MapLayers parentLayers, Element element) {
		MapLayer layer = new MapLayer();
		loadBasicLayerInfo(layer, element);
		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(layer.getProperties(), properties);
		}

		for(Element objectElement : element.getChildrenByName("object")) {
			loadObject(map, layer, objectElement);
		}

		parentLayers.add(layer);
	}

	protected void loadImageLayer(MapLayers parentLayers, Element element) {
		float x;
		float y;
		if(element.hasAttribute("offsetx")) {
			x = Float.parseFloat(element.getAttribute("offsetx", "0"));
		} else {
			x = Float.parseFloat(element.getAttribute("x", "0"));
		}
		if(element.hasAttribute("offsety")) {
			y = Float.parseFloat(element.getAttribute("offsety", "0"));
		} else {
			y = Float.parseFloat(element.getAttribute("y", "0"));
		}
		if(flipY) y = mapHeightInPixels - y;

		boolean repeatX = element.getIntAttribute("repeatx", 0) == 1;
		boolean repeatY = element.getIntAttribute("repeaty", 0) == 1;

		TiledMapImageLayer layer = new TiledMapImageLayer(x, y, repeatX, repeatY);

		Element image = element.getChildByName("image");
		if(image != null) {
			String source = image.getAttribute("source");
			tilesetHandlers.add(supplier -> {
				TextureRegion texture = supplier.apply(source);
				layer.setTextureRegion(texture);
				layer.setY(layer.getY() - texture.getRegionHeight());
			});
		}

		loadBasicLayerInfo(layer, element);

		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(layer.getProperties(), properties);
		}

		parentLayers.add(layer);
	}

	protected void loadBasicLayerInfo(MapLayer layer, Element element) {
		String name = element.getAttribute("name", null);
		float opacity = Float.parseFloat(element.getAttribute("opacity", "1.0"));
		String tintColor = element.getAttribute("tintcolor", "#ffffffff");
		boolean visible = element.getIntAttribute("visible", 1) == 1;
		float offsetX = element.getFloatAttribute("offsetx", 0);
		float offsetY = element.getFloatAttribute("offsety", 0);
		float parallaxX = element.getFloatAttribute("parallaxx", 1f);
		float parallaxY = element.getFloatAttribute("parallaxy", 1f);

		layer.setName(name);
		layer.setOpacity(opacity);
		layer.setVisible(visible);
		layer.setOffsetX(offsetX);
		layer.setOffsetY(offsetY);
		layer.setParallaxX(parallaxX);
		layer.setParallaxY(parallaxY);

		// set layer tint color after converting from #AARRGGBB to #RRGGBBAA
		layer.setTintColor(Color.valueOf(tiledColorToLibGDXColor(tintColor)));
	}

	protected void loadObject(TiledMap map, MapLayer layer, Element element) {
		loadObject(map, layer.getObjects(), element, mapHeightInPixels);
	}

	protected void loadObject(TiledMap map, TiledMapTile tile, Element element) {
		loadObject(map, tile.getObjects(), element, tile.getTextureRegion().getRegionHeight());
	}

	protected void loadObject(TiledMap map, MapObjects objects, Element element, float heightInPixels) {
		MapObject object = null;

		float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
		float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

		float x = element.getFloatAttribute("x", 0) * scaleX;
		float y = (flipY ? (heightInPixels - element.getFloatAttribute("y", 0)) : element.getFloatAttribute("y", 0)) * scaleY;

		float width = element.getFloatAttribute("width", 0) * scaleX;
		float height = element.getFloatAttribute("height", 0) * scaleY;

		if(element.getChildCount() > 0) {
			Element child = null;
			if((child = element.getChildByName("polygon")) != null) {
				String[] points = child.getAttribute("points").split(" ");
				float[] vertices = new float[points.length * 2];
				for(int i = 0; i < points.length; i++) {
					String[] point = points[i].split(",");
					vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
					vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
				}
				Polygon polygon = new Polygon(vertices);
				polygon.setPosition(x, y);
				object = new PolygonMapObject(polygon);
			} else if((child = element.getChildByName("polyline")) != null) {
				String[] points = child.getAttribute("points").split(" ");
				float[] vertices = new float[points.length * 2];
				for(int i = 0; i < points.length; i++) {
					String[] point = points[i].split(",");
					vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
					vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
				}
				Polyline polyline = new Polyline(vertices);
				polyline.setPosition(x, y);
				object = new PolylineMapObject(polyline);
			} else if((child = element.getChildByName("ellipse")) != null) {
				object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
			} else if((child = element.getChildByName("point")) != null) {
				object = new PointMapObject(x, flipY ? y - height : y);
			} else if((child = element.getChildByName("text")) != null) {
				TextMapObject textMapObject = new TextMapObject(x, flipY ? y - height : y, width, height, child.getText());
				textMapObject.setRotation(child.getFloatAttribute("rotation", 0));
				textMapObject.setFontFamily(child.getAttribute("fontfamily", ""));
				textMapObject.setPixelSize(child.getIntAttribute("pixelSize", 16));
				textMapObject.setHorizontalAlign(child.getAttribute("halign", "left"));
				textMapObject.setVerticalAlign(child.getAttribute("valign", "top"));
				textMapObject.setBold(child.getIntAttribute("bold", 0) == 1);
				textMapObject.setItalic(child.getIntAttribute("italic", 0) == 1);
				textMapObject.setUnderline(child.getIntAttribute("underline", 0) == 1);
				textMapObject.setStrikeout(child.getIntAttribute("strikeout", 0) == 1);
				textMapObject.setWrap(child.getIntAttribute("wrap", 0) == 1);
				// When kerning is true, it won't be added as an attribute, it's true by default
				textMapObject.setKerning(child.getIntAttribute("kerning", 1) == 1);
				// Default color is #000000, not added as attribute
				String textColor = child.getAttribute("color", "#000000");
				textMapObject.setColor(Color.valueOf(tiledColorToLibGDXColor(textColor)));
				object = textMapObject;
			}
		}
		if(object == null) {
			String gid = null;
			if((gid = element.getAttribute("gid", null)) != null) {
				int id = (int) Long.parseLong(gid);
				boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
				boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

				TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
				TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally, flipVertically);
				TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
				tiledMapTileMapObject.getProperties().put("gid", id);
				tiledMapTileMapObject.setX(x);
				tiledMapTileMapObject.setY(flipY ? y : y - height);
				float objectWidth = element.getFloatAttribute("width", textureRegion.getRegionWidth());
				float objectHeight = element.getFloatAttribute("height", textureRegion.getRegionHeight());
				tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
				tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
				tiledMapTileMapObject.setRotation(element.getFloatAttribute("rotation", 0));
				object = tiledMapTileMapObject;
			} else {
				object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
			}
		}
		object.setName(element.getAttribute("name", null));
		String rotation = element.getAttribute("rotation", null);
		if(rotation != null) {
			object.getProperties().put("rotation", Float.parseFloat(rotation));
		}
		String type = element.getAttribute("type", null);
		if(type != null) {
			object.getProperties().put("type", type);
		}
		int id = element.getIntAttribute("id", 0);
		if(id != 0) {
			object.getProperties().put("id", id);
		}
		object.getProperties().put("x", x);

		if(object instanceof TiledMapTileMapObject) {
			object.getProperties().put("y", y);
		} else {
			object.getProperties().put("y", (flipY ? y - height : y));
		}
		object.getProperties().put("width", width);
		object.getProperties().put("height", height);
		object.setVisible(element.getIntAttribute("visible", 1) == 1);
		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(object.getProperties(), properties);
		}

		// if there is a 'type' (=class) specified, then check if there are any other
		// class properties available and put their default values into the properties.
		loadMapPropertiesClassDefaults(type, object.getProperties());

		idToObject.put(id, object);
		objects.add(object);
	}

	protected void loadProperties(MapProperties properties, Element element) {
		for(Element property : element.getChildrenByName("property")) {
			String name = property.getAttribute("name", null);
			String value = getPropertyValue(property);
			String type = property.getAttribute("type", null);
			if("object".equals(type)) {
				loadObjectProperty(properties, name, value);
			} else if("class".equals(type)) {
				// A 'class' property is a property which is itself a set of properties
				MapProperties classProperties = new MapProperties();
				String className = property.getAttribute("propertytype");
				classProperties.put("type", className);
				// the actual properties of a 'class' property are stored as a new properties tag
				properties.put(name, classProperties);
				loadClassProperties(className, classProperties, property.getChildByName("properties"));
			} else {
				loadBasicProperty(properties, name, value, type);
			}
		}
	}

	protected void loadClassProperties(String className, MapProperties classProperties, XmlReader.Element classElement) {
		if(projectClassInfo == null) {
			throw new GdxRuntimeException(
					"No class information loaded to support class properties. Did you set the 'projectFilePath' parameter?");
		}
		if(projectClassInfo.isEmpty()) {
			throw new GdxRuntimeException(
					"No class information available. Did you set the correct Tiled project path in the 'projectFilePath' parameter?");
		}
		Array<ProjectClassMember> projectClassMembers = projectClassInfo.get(className);
		if(projectClassMembers == null) {
			throw new GdxRuntimeException("There is no class with name '" + className + "' in given Tiled project file.");
		}

		for(ProjectClassMember projectClassMember : projectClassMembers) {
			String propName = projectClassMember.name();
			XmlReader.Element classProp = classElement == null ? null : getPropertyByName(classElement, propName);
			switch(projectClassMember.type()) {
				case "object": {
					String value = classProp == null ? projectClassMember.defaultValue().asString() : getPropertyValue(classProp);
					loadObjectProperty(classProperties, propName, value);
					break;
				}
				case "class": {
					// A 'class' property is a property which is itself a set of properties
					MapProperties nestedClassProperties = new MapProperties();
					String nestedClassName = projectClassMember.propertyType();
					nestedClassProperties.put("type", nestedClassName);
					// the actual properties of a 'class' property are stored as a new properties tag
					classProperties.put(propName, nestedClassProperties);
					if(classProp == null) {
						// no class values overridden -> use default class values
						loadJsonClassProperties(nestedClassName, nestedClassProperties, projectClassMember.defaultValue());
					} else {
						loadClassProperties(nestedClassName, nestedClassProperties, classProp);
					}
					break;
				}
				default: {
					String value = classProp == null ? projectClassMember.defaultValue().asString() : getPropertyValue(classProp);
					loadBasicProperty(classProperties, propName, value, projectClassMember.type());
					break;
				}
			}
		}
	}

	private static String getPropertyValue(Element classProp) {
		return classProp.getAttribute("value", classProp.getText());
	}

	protected Element getPropertyByName(Element classElement, String propName) {
		// we use getChildrenByNameRecursively here because in case of nested classes,
		// we get an element with a root property (=class) and inside additional property tags for the real
		// class properties. If we just use getChildrenByName we don't get any children for a nested class.
		for(Element property : classElement.getChildrenByNameRecursively("property")) {
			if(propName.equals(property.getAttribute("name"))) {
				return property;
			}
		}
		return null;
	}

	static public int[] getTileIds(Element element, int width, int height) {
		Element data = element.getChildByName("data");
		String encoding = data.getAttribute("encoding", null);
		if(encoding == null) { // no 'encoding' attribute means that the encoding is XML
			throw new GdxRuntimeException("Unsupported encoding (XML) for TMX Layer Data");
		}
		int[] ids = new int[width * height];
		if(encoding.equals("csv")) {
			String[] array = data.getText().split(",");
			for(int i = 0; i < array.length; i++)
				ids[i] = (int) Long.parseLong(array[i].trim());
		} else if(encoding.equals("base64")) {
			readBase64(width, height, data, ids);
		} else {
			// any other value of 'encoding' is one we're not aware of, probably a feature of a future version of Tiled
			// or another editor
			throw new GdxRuntimeException("Unrecognised encoding (" + encoding + ") for TMX Layer Data");
		}
		return ids;
	}

	private static void readBase64(int width, int height, Element data, int[] ids) {
		try(InputStream is = getStream(data.getAttribute("compression", null), data.getText())) {
			byte[] temp = new byte[4];
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					int read = is.read(temp);
					while(read < temp.length) {
						int curr = is.read(temp, read, temp.length - read);
						if(curr == -1) break;
						read += curr;
					}
					if(read != temp.length)
						throw new GdxRuntimeException("Error Reading TMX Layer Data: Premature end of tile data");
					ids[y * width + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
							| unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
				}
			}
		} catch(IOException e) {
			throw new GdxRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
		}
	}

	private static InputStream getStream(String compression, String text) throws IOException {
		byte[] bytes = Base64Coder.decode(text);

		return switch(compression) {
			case null -> new ByteArrayInputStream(bytes);
			case "gzip" -> new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length));
			case "zlib" -> new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)));
			default ->
					throw new GdxRuntimeException("Unrecognised compression (" + compression + ") for TMX Layer Data");
		};
	}

	protected void loadTileSet(Element element) {
		int firstgid = element.getIntAttribute("firstgid", 1);
		String imageSource = "";
		int imageWidth = 0;
		int imageHeight = 0;
		String image = null;

		String source = element.getAttribute("source", null);
		if(source != null) {
			// TODO celestialgdx WHY???
			FileHandle tsx = getRelativeFileHandle(this.file, source);
			try {
				element = xml.parse(tsx);
				Element imageElement = element.getChildByName("image");
				if(imageElement != null) {
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = imageSource;
				}
			} catch(SerializationException e) {
				throw new GdxRuntimeException("Error parsing external tileset.");
			}
		} else {
			Element imageElement = element.getChildByName("image");
			if(imageElement != null) {
				imageSource = imageElement.getAttribute("source");
				imageWidth = imageElement.getIntAttribute("width", 0);
				imageHeight = imageElement.getIntAttribute("height", 0);
				image = imageSource;
			}
		}
		String name = element.get("name", null);
		int tilewidth = element.getIntAttribute("tilewidth", 0);
		int tileheight = element.getIntAttribute("tileheight", 0);
		int spacing = element.getIntAttribute("spacing", 0);
		int margin = element.getIntAttribute("margin", 0);

		Element offset = element.getChildByName("tileoffset");
		int offsetX;
		int offsetY;
		if(offset != null) {
			offsetX = offset.getIntAttribute("x", 0);
			offsetY = offset.getIntAttribute("y", 0);
		} else {
			offsetX = 0;
			offsetY = 0;
		}
		TiledMapTileSet tileSet = new TiledMapTileSet();

		// TileSet
		tileSet.setName(name);
		final MapProperties tileSetProperties = tileSet.getProperties();
		Element properties = element.getChildByName("properties");
		if(properties != null) {
			loadProperties(tileSetProperties, properties);
		}
		tileSetProperties.put("firstgid", firstgid);

		// Tiles
		Array<Element> tileElements = element.getChildrenByName("tile");

		String finalImageSource = imageSource;
		int finalImageWidth = imageWidth;
		int finalImageHeight = imageHeight;
		String finalImage = image;
		tilesetHandlers.add(supplier -> {
			addStaticTiles(supplier, tileSet, tileElements, firstgid, tilewidth, tileheight, spacing,
					margin, source, offsetX, offsetY, finalImageSource, finalImageWidth, finalImageHeight,
					finalImage);
		});

		Array<AnimatedTiledMapTile> animatedTiles = new Array<>();

		for(Element tileElement : tileElements) {
			int localtid = tileElement.getIntAttribute("id", 0);
			TiledMapTile tile = tileSet.getTile(firstgid + localtid);
			if(tile != null) {
				AnimatedTiledMapTile animatedTile = createAnimatedTile(tileSet, tile, tileElement, firstgid);
				if(animatedTile != null) {
					animatedTiles.add(animatedTile);
					tile = animatedTile;
				}
				addTileProperties(tile, tileElement);
				addTileObjectGroup(tile, tileElement);
			}
		}

		// replace original static tiles by animated tiles
		for(AnimatedTiledMapTile animatedTile : animatedTiles) {
			tileSet.putTile(animatedTile.getId(), animatedTile);
		}

		map.getTileSets().addTileSet(tileSet);
	}

	protected void addStaticTiles(Function<String, TextureRegion> supplier, TiledMapTileSet tileSet,
	                              Array<Element> tileElements, int firstgid, int tilewidth, int tileheight, int spacing, int margin,
	                              String source, int offsetX, int offsetY, String imageSource, int imageWidth, int imageHeight,
	                              String image) {

		MapProperties props = tileSet.getProperties();
		if(image != null) {
			// One image for the whole tileSet
			TextureRegion texture = supplier.apply(image);

			props.put("imagesource", imageSource);
			props.put("imagewidth", imageWidth);
			props.put("imageheight", imageHeight);
			props.put("tilewidth", tilewidth);
			props.put("tileheight", tileheight);
			props.put("margin", margin);
			props.put("spacing", spacing);

			int stopWidth = texture.getRegionWidth() - tilewidth;
			int stopHeight = texture.getRegionHeight() - tileheight;

			int id = firstgid;

			for(int y = margin; y <= stopHeight; y += tileheight + spacing) {
				for(int x = margin; x <= stopWidth; x += tilewidth + spacing) {
					TextureRegion tileRegion = new TextureRegion(texture, x, y, tilewidth, tileheight);
					int tileId = id++;
					addStaticTiledMapTile(tileSet, tileRegion, tileId, offsetX, offsetY);
				}
			}
		} else {
			// Every tile has its own image source
			for(Element tileElement : tileElements) {
				Element imageElement = tileElement.getChildByName("image");
				if(imageElement != null) {
					String element = imageElement.getAttribute("source");
					// TODO celestialgdx is this right?
					source = source == null ? element : source + "/" + element;
				} else {
					continue;
				}
				TextureRegion texture = supplier.apply(source);
				int tileId = firstgid + tileElement.getIntAttribute("id");
				addStaticTiledMapTile(tileSet, texture, tileId, offsetX, offsetY);
			}
		}
	}

	protected void addTileProperties(TiledMapTile tile, Element tileElement) {
		String terrain = tileElement.getAttribute("terrain", null);
		MapProperties tileProperties = tile.getProperties();
		if(terrain != null) {
			tileProperties.put("terrain", terrain);
		}
		String probability = tileElement.getAttribute("probability", null);
		if(probability != null) {
			tileProperties.put("probability", probability);
		}
		String type = tileElement.getAttribute("type", null);
		if(type != null) {
			tileProperties.put("type", type);
		}
		Element properties = tileElement.getChildByName("properties");
		if(properties != null) {
			loadProperties(tileProperties, properties);
		}

		// if there is a 'type' (=class) specified, then check if there are any other
		// class properties available and put their default values into the properties.
		loadMapPropertiesClassDefaults(type, tileProperties);
	}

	protected void addTileObjectGroup(TiledMapTile tile, Element tileElement) {
		Element objectgroupElement = tileElement.getChildByName("objectgroup");
		if(objectgroupElement != null) {
			for(Element objectElement : objectgroupElement.getChildrenByName("object")) {
				loadObject(map, tile, objectElement);
			}
		}
	}

	protected AnimatedTiledMapTile createAnimatedTile(TiledMapTileSet tileSet, TiledMapTile tile, Element tileElement,
	                                                  int firstgid) {
		Element animationElement = tileElement.getChildByName("animation");
		if(animationElement != null) {
			Array<StaticTiledMapTile> staticTiles = new Array<>();
			IntArray intervals = new IntArray();
			for(Element frameElement : animationElement.getChildrenByName("frame")) {
				staticTiles.add((StaticTiledMapTile) tileSet.getTile(firstgid + frameElement.getIntAttribute("tileid")));
				intervals.add(frameElement.getIntAttribute("duration"));
			}

			AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
			animatedTile.setId(tile.getId());
			return animatedTile;
		}
		return null;
	}

}