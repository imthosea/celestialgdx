package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.assets.AssetManager;

/**
 * Creates {@link BitmapFont} instances from FreeType font files. Requires a {@link FreeTypeFontLoaderParameter} to be passed to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} which specifies the name of the TTF file as well the parameters
 * used to generate the BitmapFont (size, characters, etc.)
 */
public class FreetypeFontLoader extends AssetLoader<BitmapFont, FreetypeFontLoader.FreeTypeFontLoaderParameter> {
	public FreetypeFontLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public static class FreeTypeFontLoaderParameter extends AssetLoaderParameters<BitmapFont> {
		/** the name of the TTF file to be used to load the font **/
		public String fontFileName;
		/** the parameters used to generate the font, e.g. size, characters, etc. **/
		public final FreeTypeFontParameter fontParameters = new FreeTypeFontParameter();
	}

	@Override
	public BitmapFont load(String path, FreeTypeFontLoaderParameter parameter, AssetLoadingContext<BitmapFont> ctx) throws Exception {
		if(parameter == null)
			throw new RuntimeException("FreetypeFontParameter must be set in AssetManager#load to point at a TTF file!");
		FreeTypeFontGenerator generator = ctx.dependOn(parameter.fontFileName + ".gen", FreeTypeFontGenerator.class);
		return ctx.awaitWork(() -> generator.generateFont(parameter.fontParameters));
	}
}