package com.badlogic.gdx.maps.tiled.loader;

import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Null;

import java.util.function.Function;

public class MapLoader<P extends BaseTiledMapLoadHandler.Parameters> extends AssetLoader<TiledMap, P> {
	private final LoadHandlerSupplier<P> handlerSupplier;

	public MapLoader (FileHandleResolver resolver, LoadHandlerSupplier<P> handlerSupplier) {
		super(resolver);
		this.handlerSupplier = handlerSupplier;
	}

	@Override
	public TiledMap load (String path, P parameter, AssetLoadingContext<TiledMap> ctx) throws Exception {
		FileHandle file = resolve(path);
		char[] data = file.readString().toCharArray();
		char[] projectFileData;
		if (parameter == null || parameter.projectFilePath == null) {
			projectFileData = null;
		} else {
			projectFileData = resolve(parameter.projectFilePath).readString().toCharArray();
		}

		var handler = ctx.awaitWork(() -> {
			return handlerSupplier.parse(file, data,  projectFileData, parameter);
		});
		handler.loadTilesets(ctx);
		return handler.result();
	}

	@FunctionalInterface
	public interface LoadHandlerSupplier<P extends BaseTiledMapLoadHandler.Parameters> {
		BaseTiledMapLoadHandler<P> parse (FileHandle file, char[] fileData,
										  @Null char[] projectFileData,
										  P parameter);
	}

	// celestialgdx - this is a hack and should be removed
	@FunctionalInterface
	public interface TilesetPhaseHandler {
		void handle(Function<String, TextureRegion> imageSupplier);
	}
}
