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

package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.files.WriteableFileHandle;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;

/** This class can save and load a {@link ParticleEffect}. It should be added as {@link AsynchronousAssetLoader} to the
 * {@link AssetManager} so it will be able to load the effects. It's important to note that the two classes
 * {@link ParticleEffectLoadParameter} and {@link ParticleEffectSaveParameter} should be passed in whenever possible, because when
 * present the batches settings will be loaded automatically. When the load and save parameters are absent, once the effect will
 * be created, one will have to set the required batches manually otherwise the {@link ParticleController} instances contained
 * inside the effect will not be able to render themselves.
 * @author inferno */
public class ParticleEffectLoader
		extends AssetLoader<ParticleEffect, ParticleEffectLoader.ParticleEffectLoadParameter> {
	public ParticleEffectLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	/** Saves the effect to the given file contained in the passed in parameter. */
	public void save (ParticleEffect effect, ParticleEffectSaveParameter parameter) throws IOException {
		ResourceData<ParticleEffect> data = new ResourceData<>(effect);

		// effect assets
		effect.save(parameter.manager, data);

		// Batches configurations
		if (parameter.batches != null) {
			for (ParticleBatch<?> batch : parameter.batches) {
				boolean save = false;
				for (ParticleController controller : effect.getControllers()) {
					if (controller.renderer.isCompatible(batch)) {
						save = true;
						break;
					}
				}

				if (save) batch.save(parameter.manager, data);
			}
		}

		// save
		Json json = new Json(parameter.jsonOutputType);
		if (parameter.prettyPrint) {
			String prettyJson = json.prettyPrint(data);
			parameter.file.writeString(prettyJson, false);
		} else {
			json.toJson(data, parameter.file);
		}
	}

	@Override
	public ParticleEffect load (String path, ParticleEffectLoadParameter parameter, AssetLoadingContext<ParticleEffect> ctx) throws Exception {
		FileHandle file = resolve(path);
		Json json = new Json();

		ResourceData<ParticleEffect> data = json.fromJson(ResourceData.class, file);

		for (AssetData<?> assetData : data.getAssets()) {
			// If the asset doesn't exist try to load it from loading effect directory
			if (!resolve(assetData.filename).exists()) {
				assetData.filename = file.parent().child(assetData.filename).path();
			}

			if (assetData.type == ParticleEffect.class) {
				ctx.dependOn(assetData.filename, assetData.type, parameter);
			} else {
				ctx.dependOn(assetData.filename, assetData.type);
			}
		}

		data.resource.load(ctx.manager, data);
		if (parameter != null) {
			if (parameter.batches != null) {
				for (ParticleBatch<?> batch : parameter.batches) {
					batch.load(ctx.manager, data);
				}
			}
			data.resource.setBatch(parameter.batches);
		}
		return data.resource;
	}

	public static class ParticleEffectLoadParameter extends AssetLoaderParameters<ParticleEffect> {
		final Array<ParticleBatch<?>> batches;

		public ParticleEffectLoadParameter (Array<ParticleBatch<?>> batches) {
			this.batches = batches;
		}
	}

	public static class ParticleEffectSaveParameter extends AssetLoaderParameters<ParticleEffect> {
		/** Optional parameters, but should be present to correctly load the settings */
		final Array<ParticleBatch<?>> batches;

		/** Required parameters */
		final WriteableFileHandle file;
		final AssetManager manager;
		final JsonWriter.OutputType jsonOutputType;
		final boolean prettyPrint;

		public ParticleEffectSaveParameter (WriteableFileHandle file, AssetManager manager, Array<ParticleBatch<?>> batches) {
			this(file, manager, batches, JsonWriter.OutputType.minimal, false);
		}

		public ParticleEffectSaveParameter (WriteableFileHandle file, AssetManager manager, Array<ParticleBatch<?>> batches,
			JsonWriter.OutputType jsonOutputType, boolean prettyPrint) {
			this.batches = batches;
			this.file = file;
			this.manager = manager;
			this.jsonOutputType = jsonOutputType;
			this.prettyPrint = prettyPrint;
		}
	}

}
