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

import com.badlogic.gdx.assets.loaders.TextureLoader;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.image.Texture;
import me.thosea.celestialgdx.image.TextureRegion;

@FunctionalInterface
public interface ImageResolver {
	/**
	 * @return the Texture for the given image name or null.
	 */
	TextureRegion getImage(AssetLoadingContext<?> ctx, String name);

	// this is moved here so i don't have to use it in omocha - thosea

	ImageResolver BY_RELATIVE_FILE = (ctx, name) -> {
		TextureLoader.TextureParameter textParam;
		if(ctx.desc.params instanceof TsxTilesetLoader.Parameters param) {
			textParam = new TextureLoader.TextureParameter();
			textParam.minFilter = param.textureMinFilter;
			textParam.magFilter = param.textureMagFilter;
		} else {
			textParam = null;
		}

		String path = relativePath(ctx.desc.fileName, name);
		return new TextureRegion(ctx.dependOn(path, Texture.class, textParam));
	};

	private static String relativePath(String base, String path) {
		int slashIndex = base.lastIndexOf('/');
		if(slashIndex == -1) {
			return path;
		} else {
			return base.substring(0, slashIndex) + "/" + path;
		}
	}
}