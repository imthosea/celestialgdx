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
import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * {@link AssetLoader} for {@link Pixmap} instances. The Pixmap is loaded asynchronously.
 */
public class PixmapLoader extends AssetLoader<Pixmap, PixmapLoader.PixmapParameter> {
	public PixmapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Pixmap load(String path, PixmapParameter parameter, AssetLoadingContext<Pixmap> ctx) throws Exception {
		/*
		 * TODO celestialgdx:
		 * refactor FileHandle to allow directly reading to off-heap buffer to skip copy
		 */
		byte[] data = resolve(path).readBytes();
		return ctx.awaitWork(() -> {
			ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
			buffer.put(data);
			buffer.flip();
			try {
				if(parameter != null && parameter.forcedFormat != null) {
					return Pixmap.load(buffer, parameter.forcedFormat);
				} else {
					return Pixmap.load(buffer);
				}
			} finally {
				MemoryUtil.memFree(buffer);
			}
		});
	}

	public static class PixmapParameter extends AssetLoaderParameters<Pixmap> {
		public PixelFormat forcedFormat;
	}
}