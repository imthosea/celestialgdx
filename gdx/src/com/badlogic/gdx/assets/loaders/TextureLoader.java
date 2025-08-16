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

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;

/** {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link TextureParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify parameters as can be passed to the
 * various Texture constructors, e.g. filtering, whether to generate mipmaps and so on.
 * @author mzechner */
public class TextureLoader extends AssetLoader<Texture, TextureLoader.TextureParameter> {
	public TextureLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Texture load (String path, TextureParameter parameter, AssetLoadingContext<Texture> ctx) throws Exception {
		Texture texture;
		TextureData data;
		if (parameter == null || parameter.textureData == null) {
			Format format;
			boolean genMipMaps;

			if (parameter != null) {
				format = parameter.format;
				genMipMaps = parameter.genMipMaps;
				texture = parameter.texture;
			} else {
				format = null;
				genMipMaps = false;
				texture = null;
			}

			data = ctx.awaitWork(() -> {
				return TextureData.Factory.loadFromFile(resolve(path), format, genMipMaps);
			});
		} else {
			texture = parameter.texture;
			data = parameter.textureData;
		}
		if (!data.isPrepared()) data.prepare();

		return ctx.awaitMainThread(() -> {
			Texture result;
			if (texture != null) {
				result = texture;
				texture.load(data);
			} else {
				result = new Texture(data);
			}
			if (parameter != null) {
				result.setFilter(parameter.minFilter, parameter.magFilter);
				result.setWrap(parameter.wrapU, parameter.wrapV);
			}
			return result;
		});
	}

	static public class TextureParameter extends AssetLoaderParameters<Texture> {
		/** the format of the final Texture. Uses the source images format if null **/
		public Format format = null;
		/** whether to generate mipmaps **/
		public boolean genMipMaps = false;
		/** The texture to put the {@link TextureData} in, optional. **/
		public Texture texture = null;
		/** TextureData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
		public TextureData textureData = null;
		public TextureFilter minFilter = TextureFilter.Nearest;
		public TextureFilter magFilter = TextureFilter.Nearest;
		public TextureWrap wrapU = TextureWrap.ClampToEdge;
		public TextureWrap wrapV = TextureWrap.ClampToEdge;
	}
}
