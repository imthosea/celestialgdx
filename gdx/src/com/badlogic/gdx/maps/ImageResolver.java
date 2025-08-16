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

package com.badlogic.gdx.maps;

import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.loader.BaseTiledMapLoadHandler;

import java.util.StringTokenizer;

@FunctionalInterface
public interface ImageResolver {
	/**
	 * @return the Texture for the given image name or null.
	 */
	TextureRegion getImage (AssetLoadingContext<?> ctx, FileHandle projectFile, String name);

	// this is moved here so i don't have to use it in omocha - thosea

	ImageResolver BY_RELATIVE_FILE = (ctx, projectFile, name) -> {
		FileHandle file = getRelativeFileHandle(projectFile, name);
		TextureLoader.TextureParameter textParam;
		if(ctx.desc.params instanceof BaseTiledMapLoadHandler.Parameters param) {
			textParam = new TextureLoader.TextureParameter();
			textParam.genMipMaps = param.generateMipMaps;
			textParam.minFilter = param.textureMinFilter;
			textParam.magFilter = param.textureMagFilter;
		} else {
			textParam = null;
		}
		return new TextureRegion(ctx.dependOn(file.path(), Texture.class, textParam));
	};

	static FileHandle getRelativeFileHandle (FileHandle file, String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
		FileHandle result = file.parent();
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals(".."))
				result = result.parent();
			else {
				result = result.child(token);
			}
		}
		return result;
	}
}
