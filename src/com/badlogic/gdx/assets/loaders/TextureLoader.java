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

import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.assets.AssetManager;
import me.thosea.celestialgdx.graphics.Texture;
import me.thosea.celestialgdx.graphics.Texture.TextureFilter;
import me.thosea.celestialgdx.graphics.Texture.TextureWrap;
import me.thosea.celestialgdx.image.Pixmap;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link TextureParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify parameters as can be passed to the
 * various Texture constructors, e.g. filtering, whether to generate mipmaps and so on.
 * @author mzechner
 */
public class TextureLoader extends AssetLoader<Texture, TextureLoader.TextureParameter> {
	public TextureLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Texture load(String path, TextureParameter parameter, AssetLoadingContext<Texture> ctx) throws Exception {
		// TODO celestialgdx: in asset manager rework, allow same path with different types
		byte[] data = resolve(path).readBytes();
		Pixmap pixmap = ctx.awaitWork(() -> {
			ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
			buffer.put(data);
			buffer.flip();
			try {
				return Pixmap.load(buffer);
			} finally {
				MemoryUtil.memFree(buffer);
			}
		});
		return ctx.awaitMainThread(() -> {
			Texture texture = Texture.create2D();
			texture.upload(pixmap, parameter != null && parameter.compress);
			if(parameter != null) {
				texture.setMinificationFilter(parameter.minFilter);
				texture.setMinificationFilter(parameter.magFilter);
				texture.setHorizontalWrap(parameter.wrapU);
				texture.setVerticalWrap(parameter.wrapV);
			} else {
				texture.setMinificationFilter(TextureFilter.NEAREST);
				texture.setMagnificationFilter(TextureFilter.NEAREST);
				texture.setWrap(TextureWrap.CLAMP_TO_EDGE);
			}
			pixmap.dispose();
			return texture;
		});
	}

	static public class TextureParameter extends AssetLoaderParameters<Texture> {
		/* If true, the texture will be compressed on the GPU */
		public boolean compress = false;
		public TextureFilter minFilter = TextureFilter.NEAREST;
		public TextureFilter magFilter = TextureFilter.NEAREST;
		public TextureWrap wrapU = TextureWrap.CLAMP_TO_EDGE;
		public TextureWrap wrapV = TextureWrap.CLAMP_TO_EDGE;
	}
}