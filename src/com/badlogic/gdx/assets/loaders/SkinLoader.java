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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.files.FileHandle;

/**
 * {@link AssetLoader} for {@link Skin} instances. All {@link Texture} and {@link BitmapFont} instances will be loaded as
 * dependencies. Passing a {@link SkinParameter} allows the exact name of the texture associated with the skin to be specified.
 * Otherwise the skin texture is looked up just as with a call to {@link Skin#Skin(FileHandle)}. A
 * {@link SkinParameter} also allows named resources to be set that will be added to the skin before loading the json file,
 * meaning that they can be referenced from inside the json file itself. This is useful for dynamic resources such as a BitmapFont
 * generated through FreeTypeFontGenerator.
 * @author Nathan Sweet
 */
public class SkinLoader extends AssetLoader<Skin, SkinLoader.SkinParameter> {
	public SkinLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Skin load(String path, SkinParameter parameter, AssetLoadingContext<Skin> ctx) throws Exception {
		FileHandle file = resolve(path);
		String textureAtlasPath = file.pathWithoutExtension() + ".atlas";

		ObjectMap<String, Object> resources = null;
		if(parameter != null) {
			if(parameter.textureAtlasPath != null) {
				textureAtlasPath = parameter.textureAtlasPath;
			}
			if(parameter.resources != null) {
				resources = parameter.resources;
			}
		}

		TextureAtlas atlas = ctx.dependOn(textureAtlasPath, TextureAtlas.class);
		Skin skin = newSkin(atlas);
		if(resources != null) {
			for(Entry<String, Object> entry : resources.entries()) {
				skin.add(entry.key, entry.value);
			}
		}
		skin.load(file);
		return skin;
	}

	/**
	 * Override to allow subclasses of Skin to be loaded or the skin instance to be configured.
	 * @param atlas The TextureAtlas that the skin will use.
	 * @return A new Skin (or subclass of Skin) instance based on the provided TextureAtlas.
	 */
	protected Skin newSkin(TextureAtlas atlas) {
		return new Skin(atlas);
	}

	static public class SkinParameter extends AssetLoaderParameters<Skin> {
		public final String textureAtlasPath;
		public final ObjectMap<String, Object> resources;

		public SkinParameter() {
			this(null, null);
		}

		public SkinParameter(ObjectMap<String, Object> resources) {
			this(null, resources);
		}

		public SkinParameter(String textureAtlasPath) {
			this(textureAtlasPath, null);
		}

		public SkinParameter(String textureAtlasPath, ObjectMap<String, Object> resources) {
			this.textureAtlasPath = textureAtlasPath;
			this.resources = resources;
		}
	}
}