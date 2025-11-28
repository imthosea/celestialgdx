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

package me.thosea.celestialgdx.assets;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import me.thosea.celestialgdx.files.FileHandle;

import java.util.function.Supplier;

/**
 * Abstract base class for asset loaders.
 * @param <T> the class of the asset the loader supports
 * @param <P> the class of the loading parameters the loader supports.
 * @author mzechner
 */
public abstract class AssetLoader<T, P extends AssetLoaderParameters<T>> {
	/** {@link FileHandleResolver} used to map from plain asset names to {@link FileHandle} instances **/
	protected final FileHandleResolver resolver;

	/**
	 * Constructor, sets the {@link FileHandleResolver} to use to resolve the file associated with the asset name.
	 * @param resolver
	 */
	public AssetLoader(FileHandleResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Called on a virtual thread to load the asset
	 * @param path the file path, may or may not point to a real file. use {@link #resolve}
	 * @param parameter asset parameter
	 * @see AssetLoadingContext#dependOn(AssetDescriptor)
	 * @see AssetLoadingContext#awaitMainThread(Supplier)
	 *
	 */
	public abstract T load(String path, P parameter, AssetLoadingContext<T> ctx) throws Exception;

	/**
	 * @param fileName file name to resolve
	 * @return handle to the file, as resolved by the {@link FileHandleResolver} set on the loader
	 */
	protected FileHandle resolve(String fileName) {
		return resolver.resolve(fileName);
	}
}