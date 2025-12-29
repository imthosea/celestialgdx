package me.thosea.celestialgdx.maps.loader;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.XmlElement;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.graphics.Texture.TextureFilter;
import me.thosea.celestialgdx.image.TextureRegion;
import me.thosea.celestialgdx.maps.MapProperties;
import me.thosea.celestialgdx.maps.TiledProject;
import me.thosea.celestialgdx.maps.Tileset;
import me.thosea.celestialgdx.maps.loader.element.MapElement;
import me.thosea.celestialgdx.maps.tiles.AnimatedMapTile;
import me.thosea.celestialgdx.maps.tiles.StaticMapTile;
import me.thosea.celestialgdx.maps.tiles.TiledMapTile;

import java.io.InputStream;

/*
 TODO celestialgdx this code is kinda unreadable but i really dont wanna fix it
 */
public final class TsxTilesetLoader extends AssetLoader<Tileset, TsxTilesetLoader.Parameters> {
	public TsxTilesetLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Tileset load(String path, Parameters param, AssetLoadingContext<Tileset> ctx) throws Exception {
		XmlElement xml;
		try(InputStream stream = resolve(path).read()) {
			xml = XmlElement.parse(stream);
		}
		if(param == null) param = new Parameters();
		return load(path, xml, param, ctx);
	}

	private Tileset load(
			String path,
			XmlElement root,
			Parameters param,
			AssetLoadingContext<Tileset> ctx
	) {
		int tileCount = root.getIntAttribute("tilecount");
		TiledMapTile[] tiles = new TiledMapTile[tileCount];

		XmlElement imageElement = root.getChildByName("image");
		if(imageElement != null) {
			readSingleImage(root, imageElement, tiles, param.imageResolver, ctx);
		} else {
			readMultiImage(root, tiles, param.imageResolver, ctx);
		}

		MapProperties setProp = new MapProperties();
		ctx.awaitWork(() -> {
			TiledLoaderUtils.loadPropertiesFor(setProp, root, param.project);
			for(XmlElement tileElement : root.getChildrenByName("tile")) {
				readTileProperties(tileElement, tiles, param.project);
			}
		});

		return createTileset(param.name, path, root, tiles, setProp);
	}

	private void readTileProperties(
			XmlElement element,
			TiledMapTile[] tiles,
			TiledProject project
	) {
		int id = element.getIntAttribute("id");
		checkId(id, tiles);

		AnimatedMapTile.AnimationFrame[] frames;
		MapProperties prop;

		XmlElement propElement = element.getChildByName("properties");
		if(propElement != null) {
			prop = new MapProperties();
			TiledLoaderUtils.loadProperties(
					prop,
					MapElement.xml(propElement),
					project
			);
		} else {
			prop = null;
		}

		XmlElement animElement = element.getChildByName("animation");
		if(animElement != null) {
			var frameElements = animElement.getChildren();
			frames = new AnimatedMapTile.AnimationFrame[frameElements.size()];

			for(int i = 0; i < frames.length; i++) {
				var frameElement = frameElements.get(i);
				int frameId = frameElement.getIntAttribute("tileid");
				checkId(frameId, tiles, "Animation frame tile ID is out of bounds");
				frames[i] = new AnimatedMapTile.AnimationFrame(
						frameElement.getIntAttribute("duration"),
						tiles[frameId].texture(0)
				);
			}
		} else {
			frames = null;
		}

		var original = tiles[id];
		if(frames != null) {
			tiles[id] = new AnimatedMapTile(original, prop, frames);
		} else if(prop != null) {
			tiles[id] = new StaticMapTile(original, prop);
		}
	}

	private Tileset createTileset(
			String paramName, String path,
			XmlElement root,
			TiledMapTile[] tiles, MapProperties properties
	) {
		String name = paramName;
		if(name == null) name = root.getAttribute("name", null);
		if(name == null) name = path;

		int offsetX = 0;
		int offsetY = 0;
		XmlElement offset = root.getChildByName("tileoffset");
		if(offset != null) {
			offsetX = offset.getIntAttribute("x", 0);
			offsetY = offset.getIntAttribute("y", 0);
		}

		return new Tileset(name, tiles, properties, offsetX, offsetY);
	}

	private void readSingleImage(
			XmlElement root,
			XmlElement imageElement,
			TiledMapTile[] tiles,
			ImageResolver imageResolver,
			AssetLoadingContext<Tileset> ctx
	) {
		// One image for the whole tileSet

		String source = imageElement.expectAttribute("source");
		TextureRegion texture = imageResolver.getImage(ctx, source);

		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		int spacing = root.getIntAttribute("spacing", 0);
		int margin = root.getIntAttribute("margin", 0);

		int stopWidth = texture.width - tileWidth;
		int stopHeight = texture.height - tileHeight;

		int id = 0;
		for(int y = margin + texture.getRegionY(); y <= stopHeight; y += tileHeight + spacing) {
			for(int x = margin + texture.getRegionX(); x <= stopWidth; x += tileWidth + spacing) {
				checkId(id, tiles);

				TextureRegion region = new TextureRegion(texture, x, y, tileWidth, tileHeight);
				tiles[id] = createStaticTile(id, region);
				id++;
			}
		}
	}

	private void readMultiImage(
			XmlElement root,
			TiledMapTile[] tiles,
			ImageResolver imageResolver,
			AssetLoadingContext<Tileset> ctx
	) {
		// Every tile has its own image source
		for(XmlElement tileElement : root.getChildrenByName("tile")) {
			XmlElement imageElement = tileElement.expectChildByName("image");
			String source = imageElement.expectAttribute("source");
			TextureRegion texture = imageResolver.getImage(ctx, source);

			int id = tileElement.getIntAttribute("id");
			checkId(id, tiles);
			tiles[id] = createStaticTile(id, texture);
		}
	}

	private StaticMapTile createStaticTile(int id, TextureRegion texture) {
		return new StaticMapTile(
				id,
				true, // isBlending - TODO allow changing this
				null, // properties
				texture
		);
	}

	private void checkId(int index, TiledMapTile[] tiles) {
		if(index < 0 || index >= tiles.length)
			throw new IllegalStateException("Tile ID is out of range (min: 0, max: " + tiles.length + ")");
	}

	private void checkId(int index, TiledMapTile[] tiles, String msg) {
		if(index < 0 || index >= tiles.length)
			throw new IllegalStateException(msg);
	}

	public static class Parameters extends AssetLoaderParameters<Tileset> {
		/**
		 * override tileset name. if null, defaults to
		 * 1. the name specified by the tileset, or 2. the file path
		 */
		public String name;
		/**
		 * owner project, used to get custom class types
		 */
		public TiledProject project;

		// TODO celestialgdx: why is this a thing? do i need it?
		public boolean flipY;

		/**
		 * override how images are resolved
		 */
		public ImageResolver imageResolver = ImageResolver.BY_RELATIVE_FILE;
		/**
		 * generate mipmaps?
		 **/
		public boolean generateMipMaps = false;
		/**
		 * The TextureFilter to use for minification
		 **/
		public TextureFilter textureMinFilter = TextureFilter.NEAREST;
		/**
		 * The TextureFilter to use for magnification
		 **/
		public TextureFilter textureMagFilter = TextureFilter.NEAREST;
	}
}