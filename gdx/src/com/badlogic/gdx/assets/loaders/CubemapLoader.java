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
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;

/** {@link AssetLoader} for {@link Cubemap} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link CubemapParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify parameters as can be passed to the
 * various Cubemap constructors, e.g. filtering and so on.
 * @author mzechner, Vincent Bousquet */
public class CubemapLoader extends AssetLoader<Cubemap, CubemapLoader.CubemapParameter> {
	public CubemapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Cubemap load (String path, CubemapParameter parameter, AssetLoadingContext<Cubemap> ctx) throws Exception {
		CubemapData data;
		final Cubemap cubemap;

		if (parameter == null || parameter.cubemapData == null) {
			data = new KTXTextureData(resolve(path), false);
			cubemap = null;
		} else {
			data = parameter.cubemapData;
			cubemap = parameter.cubemap;
		}

		if (!data.isPrepared()) data.prepare();

		return ctx.awaitMainThread(() -> {
			Cubemap map;
			if (cubemap != null) {
				map = cubemap;
				map.load(data);
			} else {
				map = new Cubemap(data);
			}
			if (parameter != null) {
				map.setFilter(parameter.minFilter, parameter.magFilter);
				map.setWrap(parameter.wrapU, parameter.wrapV);
			}
			return map;
		});
	}

	static public class CubemapParameter extends AssetLoaderParameters<Cubemap> {
		/** the format of the final Texture. Uses the source images format if null **/
		public Format format = null;
		/** The texture to put the {@link TextureData} in, optional. **/
		public Cubemap cubemap = null;
		/** CubemapData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
		public CubemapData cubemapData = null;
		public TextureFilter minFilter = TextureFilter.Nearest;
		public TextureFilter magFilter = TextureFilter.Nearest;
		public TextureWrap wrapU = TextureWrap.ClampToEdge;
		public TextureWrap wrapV = TextureWrap.ClampToEdge;
	}
}
