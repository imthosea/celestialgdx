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

package me.thosea.celestialgdx.maps.loader;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlElement;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.graphics.Texture;
import me.thosea.celestialgdx.image.TextureRegion;
import me.thosea.celestialgdx.maps.MapProperties;
import me.thosea.celestialgdx.maps.TiledMap;
import me.thosea.celestialgdx.maps.TiledProject;
import me.thosea.celestialgdx.maps.Tileset;
import me.thosea.celestialgdx.maps.layers.GroupLayer;
import me.thosea.celestialgdx.maps.layers.ImageLayer;
import me.thosea.celestialgdx.maps.layers.MapLayer;
import me.thosea.celestialgdx.maps.layers.ObjectLayer;
import me.thosea.celestialgdx.maps.layers.TileLayer;
import me.thosea.celestialgdx.maps.layers.TileLayer.Cell;
import me.thosea.celestialgdx.maps.loader.TmxMapLoader.TmxLoadContext.TilesetEntry;
import me.thosea.celestialgdx.maps.objects.MapObject;
import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class TmxMapLoader extends AssetLoader<TiledMap, TmxMapLoader.Parameters> {
	public TmxMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public static class Parameters extends AssetLoaderParameters<TiledMap> {
		/**
		 * The tiled project. Required to resolve custom class types
		 */
		public TiledProject project;

		/**
		 * The default looks for the tileset in the same folder
		 */
		public BiFunction<String, AssetLoadingContext<?>, Tileset> tilesetResolver = (source, ctx) -> {
			String path = ctx.desc.fileName;
			int index = path.lastIndexOf('/');
			String parent = index != -1
					? path.substring(0, index) + "/"
					: path;
			return ctx.dependOn(parent + source, Tileset.class);
		};

		/**
		 * Defaults to looking in the same folder
		 */
		public BiFunction<String, AssetLoadingContext<?>, TextureRegion> imageResolver = (source, ctx) -> {
			String path = ctx.desc.fileName;
			int index = path.lastIndexOf('/');
			String parent = index != -1
					? path.substring(0, index) + "/"
					: path;
			return new TextureRegion(ctx.dependOn(parent + source, Texture.class));
		};

		/**
		 * Whether to convert the objects' pixel position and size to the equivalent in tile space.
		 */
		public boolean convertObjectToTileSpace = true;
		/**
		 * Whether to flip all Y coordinates so that Y positive is up. All libGDX renderers require flipped Y coordinates, and thus
		 * flipY set to true. This parameter is included for non-rendering related purposes of TMX files, or custom renderers.
		 */
		public boolean flipY = true;
	}

	private static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	private static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	private static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
	private static final int FLAG_ROTATED_HEXAGONAL_120 = 0x10000000;
	private static final int MASK_CLEAR = 0xE0000000;

	static final class TmxLoadContext {
		record TilesetEntry(int firstgid, Tileset tileset) {}

		final XmlElement root;
		final List<TilesetEntry> tilesets;
		final Parameters parameter;
		final TiledProject project;

		// this is somewhat of a hack - some layers need non-tile dependencies we can't
		// easily probe before loading (i.e. "image" in imagelayer)
		final List<Consumer<AssetLoadingContext<?>>> deferredTasks = new ArrayList<>();

		float objectScaleX;
		float objectScaleY;

		int tileWidth;
		int tileHeight;
		int widthInPixels;
		int heightInPixels;

		TiledMap map;

		TmxLoadContext(XmlElement root, List<TilesetEntry> tilesets, Parameters parameter) {
			this.root = root;
			this.tilesets = tilesets;
			this.parameter = parameter;
			this.project = parameter.project;
		}

		public TiledMapTile tile(int id) {
			id &= ~MASK_CLEAR;
			for(TilesetEntry entry : tilesets) {
				if(entry.firstgid <= id) {
					return entry.tileset.getTile(id - entry.firstgid);
				}
			}
			return null;
		}
	}

	private record WorkResult(TiledMap map, List<Consumer<AssetLoadingContext<?>>> deferredTasks) {}

	@Override
	public TiledMap load(String path, TmxMapLoader.Parameters parameter, AssetLoadingContext<TiledMap> ctx) throws Exception {
		XmlElement root;
		try(InputStream stream = resolve(path).read()) {
			root = XmlElement.parse(stream);
		}
		return load(root, parameter, ctx);
	}

	private TiledMap load(
			XmlElement root,
			Parameters parameter,
			AssetLoadingContext<?> ctx
	) {
		List<TilesetEntry> tilesets = new ArrayList<>(3);

		for(XmlElement entry : root.getChildrenByName("tileset")) {
			String source = entry.expectAttribute("source");
			int firstgid = entry.getIntAttribute("firstgid");

			Tileset tileset = parameter.tilesetResolver.apply(source, ctx);
			tilesets.add(new TmxLoadContext.TilesetEntry(firstgid, tileset));
		}

		WorkResult result = ctx.awaitWork(() -> {
			return loadMap(new TmxLoadContext(root, tilesets, parameter));
		});
		result.deferredTasks.forEach(task -> task.accept(ctx));
		return result.map;
	}

	private TmxMapLoader.WorkResult loadMap(TmxMapLoader.TmxLoadContext ctx) {
		XmlElement root = ctx.root;
		MapProperties prop = new MapProperties();

		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		int hexSideLength = root.getIntAttribute("hexsidelength", 0);
		String staggerAxis = root.getAttribute("staggeraxis", null);
		String staggerIndex = root.getAttribute("staggerindex", null);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

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

		ctx.tileWidth = tileWidth;
		ctx.tileHeight = tileHeight;
		ctx.widthInPixels = mapWidth * tileWidth;
		ctx.heightInPixels = mapHeight * tileHeight;

		boolean convert = ctx.parameter.convertObjectToTileSpace;
		ctx.objectScaleX = !convert ? 1.0f : 1.0f / ctx.tileWidth;
		ctx.objectScaleY = !convert ? 1.0f : 1.0f / ctx.tileHeight;

		if(mapOrientation != null) {
			prop.put("orientation", mapOrientation);
			if("staggered".equals(mapOrientation)) {
				if(mapHeight > 1) {
					ctx.widthInPixels += tileWidth / 2;
					ctx.heightInPixels = ctx.heightInPixels / 2 + tileHeight / 2;
				}
			}
		}

		TiledLoaderUtils.loadPropertiesFor(prop, root, ctx.project);

		List<MapLayer> layers = new ArrayList<>();
		TiledMap map = new TiledMap(prop, layers, mapWidth, mapHeight, tileWidth, tileHeight);
		ctx.map = map;

		for(XmlElement element : root.getChildren()) {
			MapLayer layer = loadLayer(ctx, null, element);
			if(layer != null) {
				layers.add(layer);
			}
		}

		return new WorkResult(map, ctx.deferredTasks);
	}

	private MapLayer loadLayer(TmxMapLoader.TmxLoadContext ctx, MapLayer parent, XmlElement element) {
		String name = element.getName();
		return switch(name) {
			case "group" -> loadGroupLayer(element, parent, ctx);
			case "layer" -> loadTileLayer(element, parent, ctx);
			case "objectgroup" -> loadObjectGroup(ctx, parent, element);
			case "imagelayer" -> loadImageLayer(element, parent, ctx);
			default -> null;
		};
	}

	private MapLayer loadGroupLayer(XmlElement xml, MapLayer parent, TmxMapLoader.TmxLoadContext ctx) {
		GroupLayer layer = new GroupLayer(parent, ctx.map);
		loadLayerProperties(layer, xml, ctx);

		for(XmlElement child : xml.getChildren()) {
			MapLayer childLayer = loadLayer(ctx, layer, child);
			if(childLayer == null) {
				throw new GdxRuntimeException("Unknown layer " + child.getName());
			} else {
				layer.getLayers().add(childLayer);
			}
		}

		return layer;
	}

	private TileLayer loadTileLayer(XmlElement xml, MapLayer parent, TmxMapLoader.TmxLoadContext ctx) {
		int width = xml.getIntAttribute("width", 0);
		int height = xml.getIntAttribute("height", 0);

		TileLayer layer = new TileLayer(
				parent, ctx.map,
				width, height, ctx.tileWidth, ctx.tileHeight
		);
		loadLayerProperties(layer, xml, ctx);

		int[] ids = readTileIds(xml, width, height);
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int id = ids[y * width + x];
				boolean flipHorizontally = (id & FLAG_FLIP_HORIZONTALLY) != 0;
				boolean flipVertically = (id & FLAG_FLIP_VERTICALLY) != 0;
				boolean flipDiagonally = (id & FLAG_FLIP_DIAGONALLY) != 0;
				boolean rotatedhex120 = (id & FLAG_ROTATED_HEXAGONAL_120) != 0;
				byte flags = Cell.rotation(flipHorizontally, flipVertically, flipDiagonally, rotatedhex120);

				TiledMapTile tile = ctx.tile(id);
				if(tile != null) {
					Cell cell = new Cell(tile, flags);
					layer.setCell(x, ctx.parameter.flipY ? height - 1 - y : y, cell);
				}
			}
		}

		return layer;
	}

	private static int[] readTileIds(XmlElement xml, int width, int height) {
		XmlElement data = xml.expectChildByName("data");
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

	private static void readBase64(int width, int height, XmlElement data, int[] ids) {
		try(InputStream stream = getStream(data.getAttribute("compression", null), data.getText())) {
			byte[] buf = new byte[4];
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					int result = stream.readNBytes(buf, 0, 4);
					if(result != 4) {
						throw new GdxRuntimeException("Premature end of tile data");
					}
					ids[y * width + x]
							= unsignedByteToInt(buf[0])
							| unsignedByteToInt(buf[1]) << 8
							| unsignedByteToInt(buf[2]) << 16
							| unsignedByteToInt(buf[3]) << 24;
				}
			}
		} catch(IOException e) {
			throw new GdxRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
		}
	}

	private static int unsignedByteToInt(byte b) {
		return b & 0xFF;
	}

	private ObjectLayer loadObjectGroup(
			TmxMapLoader.TmxLoadContext ctx, MapLayer parent,
			XmlElement xml
	) {
		ObjectLayer layer = new ObjectLayer(parent, ctx.map);
		loadLayerProperties(layer, xml, ctx);

		Function<Integer, TiledMapTile> tileSupplier = ctx::tile;
		List<MapObject> objects = layer.getObjects();
		for(XmlElement objectElement : xml.getChildrenByName("object")) {
			MapObject object = TiledObjectLoader.read(
					objectElement,
					ctx.heightInPixels,
					ctx.parameter.flipY,
					ctx.objectScaleX, ctx.objectScaleY,
					tileSupplier
			);
			objects.add(object);
		}

		return layer;
	}

	private MapLayer loadImageLayer(XmlElement xml, MapLayer parent, TmxLoadContext ctx) {
		ImageLayer layer = new ImageLayer(
				parent, ctx.map,
				xml.getIntAttribute("repeatx", 0) == 1,
				xml.getIntAttribute("repeaty", 0) == 1
		);

		XmlElement image = xml.getChildByName("image");
		if(image != null) {
			String source = image.expectAttribute("source");
			ctx.deferredTasks.add(assetCtx -> {
				layer.setTexture(ctx.parameter.imageResolver.apply(source, assetCtx));
			});
		}

		loadLayerProperties(layer, xml, ctx);
		return layer;
	}

	private void loadLayerProperties(MapLayer layer, XmlElement xml, TmxMapLoader.TmxLoadContext ctx) {
		layer.setName(xml.expectAttribute("name"));
		layer.setVisible(xml.getIntAttribute("visible", 1) == 1);
		layer.setOpacity(xml.getFloatAttribute("opacity", 1.0f));
		layer.setOffsetX(xml.getFloatAttribute("offsetx", 0));
		layer.setOffsetY(xml.getFloatAttribute("offsety", 0));

		layer.setParallaxX(xml.getFloatAttribute("parallaxx", 1f));
		layer.setParallaxY(xml.getFloatAttribute("parallaxy", 1f));

		// set layer tint color after converting from #AARRGGBB to #RRGGBBAA
		String tintColor = xml.getAttribute("tintcolor", "#ffffffff");
		layer.setTint(Color.valueOf(TiledLoaderUtils.tiledToGdxColor(tintColor)));

		TiledLoaderUtils.loadPropertiesFor(layer.getProperties(), xml, ctx.project);
	}

	private static InputStream getStream(String compression, String text) throws IOException {
		byte[] bytes = Base64.getDecoder().decode(text.getBytes(StandardCharsets.UTF_8));

		return switch(compression) {
			case null -> new ByteArrayInputStream(bytes);
			case "gzip" -> new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length));
			case "zlib" -> new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)));
			default ->
					throw new GdxRuntimeException("Unrecognised compression (" + compression + ") for TMX Layer Data");
		};
	}
}