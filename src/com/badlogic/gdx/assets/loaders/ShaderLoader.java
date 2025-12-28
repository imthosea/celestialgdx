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

import com.badlogic.gdx.assets.loaders.ShaderLoader.ShaderParameter;
import com.badlogic.gdx.assets.loaders.ShaderLoader.ShaderParameter.ShaderCreator;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.graphics.Shader;

import java.util.Objects;

/**
 * {@link AssetLoader} for {@link Shader}s.
 * <p>
 * The path passed will be appended with {@link #vertexSuffix} (default: .vert)
 * to find the vertex shader and appended with {@link #fragSuffix} (default: .frag)
 * to find the fragment shader. If these files aren't found, loading with fail.
 * Override the path with {@link ShaderParameter#vertexFile} & {@link ShaderParameter#fragmentFile}
 * </p>
 *
 * <p>
 * <b>The {@link ShaderCreator} parameter is required.</b>
 * After loading the vertex and fragment files into strings, the creator will be invoked
 * on the main thread to make the result object. An exception will be thrown if it returns null.
 * </p>
 */
public class ShaderLoader extends AssetLoader<Shader, ShaderParameter> {
	private final String vertexSuffix;
	private final String fragSuffix;

	public ShaderLoader(FileHandleResolver resolver) {
		this(resolver, ".vert", ".frag");
	}

	public ShaderLoader(FileHandleResolver resolver, String vertexFileSuffix, String fragmentFileSuffix) {
		super(resolver);
		this.vertexSuffix = vertexFileSuffix;
		this.fragSuffix = fragmentFileSuffix;
	}

	@Override
	public Shader load(String path, ShaderParameter parameter, AssetLoadingContext<Shader> ctx) throws Exception {
		if(parameter == null || parameter.creator == null) {
			throw new IllegalStateException("shader creator parameter is required");
		}

		String vertPath;
		String fragPath;
		if(parameter.vertexFile != null) {
			vertPath = parameter.vertexFile;
		} else {
			vertPath = path + vertexSuffix;
		}
		if(parameter.fragmentFile != null) {
			fragPath = parameter.fragmentFile;
		} else {
			fragPath = path + fragSuffix;
		}

		String vertex = resolve(vertPath).readString();
		String frag = resolve(fragPath).readString();
		return ctx.awaitMainThread(() -> {
			Shader shader = parameter.creator.create(vertex, frag);
			Objects.requireNonNull(shader, "ShaderCreator returned null");
			return shader;
		});
	}

	public static class ShaderParameter extends AssetLoaderParameters<Shader> {
		@FunctionalInterface
		public interface ShaderCreator {
			Shader create(String vert, String frag);
		}

		public ShaderParameter(ShaderCreator creator) {
			this.creator = creator;
		}

		/**
		 * Creator to use once the vertex and fragment files have been loaded in memory.
		 * This will always be called on the main thread.
		 * This is a required parameter!
		 */
		public ShaderCreator creator;

		/**
		 * Path to the vertex shader instead of the default of appending the suffix (default: .vert) to the path
		 */
		public String vertexFile;
		/**
		 * Path to the fragment shader instead of the default of appending the suffix (default: .frag) to the path
		 */
		public String fragmentFile;
	}
}