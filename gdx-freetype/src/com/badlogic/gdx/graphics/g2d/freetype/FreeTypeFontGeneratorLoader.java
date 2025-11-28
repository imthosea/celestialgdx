/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.assets.AssetManager;
import me.thosea.celestialgdx.files.FileHandle;

/**
 * Makes {@link FreeTypeFontGenerator} managable via {@link AssetManager}.
 * <p>
 * Do
 * {@code assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()))}
 * to register it.
 * </p>
 * @author Daniel Holderbaum
 */
public class FreeTypeFontGeneratorLoader
		extends AssetLoader<FreeTypeFontGenerator, FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters> {

	public FreeTypeFontGeneratorLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public FreeTypeFontGenerator load(String path, FreeTypeFontGeneratorParameters parameter, AssetLoadingContext<FreeTypeFontGenerator> ctx) throws Exception {
		FileHandle file = resolve(path);
		if(file.extension().equals("gen")) {
			return new FreeTypeFontGenerator(file.sibling(file.nameWithoutExtension()));
		} else {
			return new FreeTypeFontGenerator(file);
		}
	}

	static public class FreeTypeFontGeneratorParameters extends AssetLoaderParameters<FreeTypeFontGenerator> {
	}
}