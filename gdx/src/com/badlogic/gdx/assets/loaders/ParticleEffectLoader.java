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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/** {@link AssetLoader} to load {@link ParticleEffect} instances. Passing a {@link ParticleEffectParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows to specify an atlas file or an image directory to be
 * used for the effect's images. Per default images are loaded from the directory in which the effect file is found. */
public class ParticleEffectLoader extends AssetLoader<ParticleEffect, ParticleEffectLoader.ParticleEffectParameter> {
	public ParticleEffectLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ParticleEffect load (String path, ParticleEffectParameter parameter, AssetLoadingContext<ParticleEffect> ctx) throws Exception {
		ParticleEffect effect = new ParticleEffect();
		FileHandle file = resolve(path);

		if (parameter != null && parameter.atlasFile != null)
			effect.load(file, ctx.dependOn(parameter.atlasFile, TextureAtlas.class), parameter.atlasPrefix);
		else if (parameter != null && parameter.imagesDir != null)
			effect.load(file, parameter.imagesDir);
		else
			effect.load(file, file.parent());
		return effect;
	}

	/** Parameter to be passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)} if additional configuration is
	 * necessary for the {@link ParticleEffect}. */
	public static class ParticleEffectParameter extends AssetLoaderParameters<ParticleEffect> {
		/** Atlas file name. */
		public String atlasFile;
		/** Optional prefix to image names **/
		public String atlasPrefix;
		/** Image directory. */
		public FileHandle imagesDir;
	}
}
