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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.Disposable;

import java.util.Iterator;

public abstract class ModelLoader<P extends ModelLoader.ModelParameters> extends AssetLoader<Model, P> {
	public ModelLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	/** Directly load the raw model data on the calling thread. */
	public abstract ModelData loadModelData(final FileHandle fileHandle, P parameters);

	/** Directly load the raw model data on the calling thread. */
	public ModelData loadModelData(final FileHandle fileHandle) {
		return loadModelData(fileHandle, null);
	}

	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, TextureProvider textureProvider, P parameters) {
		final ModelData data = loadModelData(fileHandle, parameters);
		return data == null ? null : new Model(data, textureProvider);
	}

	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, P parameters) {
		return loadModel(fileHandle, new TextureProvider.FileTextureProvider(), parameters);
	}

	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle, TextureProvider textureProvider) {
		return loadModel(fileHandle, textureProvider, null);
	}

	/** Directly load the model on the calling thread. The model with not be managed by an {@link AssetManager}. */
	public Model loadModel(final FileHandle fileHandle) {
		return loadModel(fileHandle, new TextureProvider.FileTextureProvider(), null);
	}

	@Override
	public Model load(String path, P parameter, AssetLoadingContext<Model> ctx) throws Exception {
		ModelData data = loadModelData(resolve(path), parameter);
		if(data == null) {
			// TODO celestialgdx - is this intentional?
			// the original implmenetation returns null if data is null, which seems quite weird
			throw new IllegalStateException("Load error");
		}

		TextureLoader.TextureParameter textureParameter;
		if(parameter == null) {
			textureParameter = new ModelParameters().textureParameter;
		} else {
			textureParameter = parameter.textureParameter;
		}

		for(final ModelMaterial modelMaterial : data.materials) {
			if(modelMaterial.textures == null) continue;

			for(ModelTexture modelTexture : modelMaterial.textures) {
				ctx.dependOn(modelTexture.fileName, Texture.class, textureParameter);
			}
		}

		Model model = ctx.awaitMainThread(() -> {
			return new Model(data, new TextureProvider.AssetTextureProvider(ctx.manager));
		});
		// need to remove the textures from the managed disposables, or else ref counting
		// doesn't work!
		Iterator<Disposable> disposables = model.getManagedDisposables().iterator();
		while(disposables.hasNext()) {
			Disposable disposable = disposables.next();
			if(disposable instanceof Texture) {
				disposables.remove();
			}
		}
		return model;
	}

	static public class ModelParameters extends AssetLoaderParameters<Model> {
		public TextureLoader.TextureParameter textureParameter;

		public ModelParameters() {
			textureParameter = new TextureLoader.TextureParameter();
			textureParameter.minFilter = textureParameter.magFilter = Texture.TextureFilter.Linear;
			textureParameter.wrapU = textureParameter.wrapV = Texture.TextureWrap.Repeat;
		}
	}
}