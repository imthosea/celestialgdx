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

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.function.Function;

public class TideMapLoader extends AssetLoader<TiledMap, TideMapLoader.Parameters> {

	public static class Parameters extends AssetLoaderParameters<TiledMap> {
		public ImageResolver resolver = ImageResolver.BY_RELATIVE_FILE;
	}

	public TideMapLoader() {
		super(new InternalFileHandleResolver());
	}

	public TideMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	// celestialgdx HACK ALERT BECAUSE IM GOING CRAZY
	private record MapParseResult(Element root, TiledMap map) {}

	@Override
	public TiledMap load(String path, Parameters parameter, AssetLoadingContext<TiledMap> ctx) throws Exception {
		FileHandle tideFile = resolve(path);
		ImageResolver resolver = parameter != null
				? parameter.resolver
				: ImageResolver.BY_RELATIVE_FILE;

		char[] data = tideFile.readString("UTF-8").toCharArray();

		MapParseResult result = ctx.awaitWork(() -> {
			Element root = new XmlReader().parse(data, 0, data.length);
			TiledMap map = parseMap(root);
			return new MapParseResult(root, map);
		});

		Function<String, TextureRegion> supplier = name -> resolver.getImage(ctx, tideFile, name);
		loadTileSheets(result.map, result.root, supplier);
		return result.map;
	}

	private TiledMap parseMap(Element root) {
		TiledMap map = new TiledMap();
		Element properties = root.getChildByName("Properties");
		if(properties != null) {
			loadProperties(map.getProperties(), properties);
		}
		Element layers = root.getChildByName("Layers");
		for(Element layer : layers.getChildrenByName("Layer")) {
			loadLayer(map, layer);
		}
		return map;
	}

	private void loadTileSheets(TiledMap map, Element root, Function<String, TextureRegion> supplier) {
		Element tilesheets = root.getChildByName("TileSheets");
		for(Element tilesheet : tilesheets.getChildrenByName("TileSheet")) {
			loadTileSheet(map, tilesheet, supplier);
		}
	}

	private void loadTileSheet(TiledMap map, Element element, Function<String, TextureRegion> supplier) {
		String id = element.getAttribute("Id");
		String description = element.getChildByName("Description").getText();
		String imageSource = element.getChildByName("ImageSource").getText();

		Element alignment = element.getChildByName("Alignment");
		String sheetSize = alignment.getAttribute("SheetSize");
		String tileSize = alignment.getAttribute("TileSize");
		String margin = alignment.getAttribute("Margin");
		String spacing = alignment.getAttribute("Spacing");

		String[] sheetSizeParts = sheetSize.split(" x ");
		int sheetSizeX = Integer.parseInt(sheetSizeParts[0]);
		int sheetSizeY = Integer.parseInt(sheetSizeParts[1]);

		String[] tileSizeParts = tileSize.split(" x ");
		int tileSizeX = Integer.parseInt(tileSizeParts[0]);
		int tileSizeY = Integer.parseInt(tileSizeParts[1]);

		String[] marginParts = margin.split(" x ");
		int marginX = Integer.parseInt(marginParts[0]);
		int marginY = Integer.parseInt(marginParts[1]);

		String[] spacingParts = margin.split(" x ");
		int spacingX = Integer.parseInt(spacingParts[0]);
		int spacingY = Integer.parseInt(spacingParts[1]);

		TextureRegion texture = supplier.apply(imageSource);

		TiledMapTileSets tilesets = map.getTileSets();
		int firstgid = 1;
		for(TiledMapTileSet tileset : tilesets) {
			firstgid += tileset.size();
		}

		TiledMapTileSet tileset = new TiledMapTileSet();
		tileset.setName(id);
		tileset.getProperties().put("firstgid", firstgid);
		int gid = firstgid;

		int stopWidth = texture.getRegionWidth() - tileSizeX;
		int stopHeight = texture.getRegionHeight() - tileSizeY;

		for(int y = marginY; y <= stopHeight; y += tileSizeY + spacingY) {
			for(int x = marginX; x <= stopWidth; x += tileSizeX + spacingX) {
				TiledMapTile tile = new StaticTiledMapTile(new TextureRegion(texture, x, y, tileSizeX, tileSizeY));
				tile.setId(gid);
				tileset.putTile(gid++, tile);
			}
		}

		Element properties = element.getChildByName("Properties");
		if(properties != null) {
			loadProperties(tileset.getProperties(), properties);
		}
		tilesets.addTileSet(tileset);
	}

	// celestialgdx - WHAT IS GOING ON??
	private void loadLayer(TiledMap map, Element element) {
		String id = element.getAttribute("Id");
		String visible = element.getAttribute("Visible");

		Element dimensions = element.getChildByName("Dimensions");
		String layerSize = dimensions.getAttribute("LayerSize");
		String tileSize = dimensions.getAttribute("TileSize");

		String[] layerSizeParts = layerSize.split(" x ");
		int layerSizeX = Integer.parseInt(layerSizeParts[0]);
		int layerSizeY = Integer.parseInt(layerSizeParts[1]);

		String[] tileSizeParts = tileSize.split(" x ");
		int tileSizeX = Integer.parseInt(tileSizeParts[0]);
		int tileSizeY = Integer.parseInt(tileSizeParts[1]);

		TiledMapTileLayer layer = new TiledMapTileLayer(layerSizeX, layerSizeY, tileSizeX, tileSizeY);
		layer.setName(id);
		layer.setVisible(visible.equalsIgnoreCase("True"));
		Element tileArray = element.getChildByName("TileArray");
		Array<Element> rows = tileArray.getChildrenByName("Row");
		TiledMapTileSets tilesets = map.getTileSets();
		TiledMapTileSet currentTileSet = null;
		int firstgid = 0;
		int x, y;
		for(int row = 0, rowCount = rows.size; row < rowCount; row++) {
			Element currentRow = rows.get(row);
			y = rowCount - 1 - row;
			x = 0;
			for(int child = 0, childCount = currentRow.getChildCount(); child < childCount; child++) {
				Element currentChild = currentRow.getChild(child);
				String name = currentChild.getName();
				if(name.equals("TileSheet")) {
					currentTileSet = tilesets.getTileSet(currentChild.getAttribute("Ref"));
					firstgid = currentTileSet.getProperties().get("firstgid", Integer.class);
				} else if(name.equals("Null")) {
					x += currentChild.getIntAttribute("Count");
				} else if(name.equals("Static")) {
					Cell cell = new Cell();
					cell.setTile(currentTileSet.getTile(firstgid + currentChild.getIntAttribute("Index")));
					layer.setCell(x++, y, cell);
				} else if(name.equals("Animated")) {
					// Create an AnimatedTile
					int interval = currentChild.getInt("Interval");
					Element frames = currentChild.getChildByName("Frames");
					Array<StaticTiledMapTile> frameTiles = new Array<>();
					for(int frameChild = 0, frameChildCount = frames.getChildCount(); frameChild < frameChildCount; frameChild++) {
						Element frame = frames.getChild(frameChild);
						String frameName = frame.getName();
						if(frameName.equals("TileSheet")) {
							currentTileSet = tilesets.getTileSet(frame.getAttribute("Ref"));
							firstgid = currentTileSet.getProperties().get("firstgid", Integer.class);
						} else if(frameName.equals("Static")) {
							frameTiles.add((StaticTiledMapTile) currentTileSet.getTile(firstgid + frame.getIntAttribute("Index")));
						}
					}
					Cell cell = new Cell();
					cell.setTile(new AnimatedTiledMapTile(interval / 1000f, frameTiles));
					layer.setCell(x++, y, cell); // TODO: Reuse existing animated tiles
				}
			}
		}

		Element properties = element.getChildByName("Properties");
		if(properties != null) {
			loadProperties(layer.getProperties(), properties);
		}

		map.getLayers().add(layer);
	}

	private void loadProperties(MapProperties properties, Element element) {
		for(Element property : element.getChildrenByName("Property")) {
			String key = property.getAttribute("Key", null);
			String type = property.getAttribute("Type", null);
			String value = property.getText();

			if(type.equals("Int32")) {
				properties.put(key, Integer.parseInt(value));
			} else if(type.equals("String")) {
				properties.put(key, value);
			} else if(type.equals("Boolean")) {
				properties.put(key, value.equalsIgnoreCase("true"));
			} else {
				properties.put(key, value);
			}
		}
	}
}