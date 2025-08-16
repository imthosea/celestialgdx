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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetLoadingContext;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegionLoader.PolygonRegionParameters;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.GdxIoException;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;

/** loads {@link PolygonRegion PolygonRegions} using a {@link com.badlogic.gdx.graphics.g2d.PolygonRegionLoader}
 * @author dermetfan */
public class PolygonRegionLoader extends AssetLoader<PolygonRegion, PolygonRegionParameters> {

	public static class PolygonRegionParameters extends AssetLoaderParameters<PolygonRegion> {

		/** what the line starts with that contains the file name of the texture for this {@code PolygonRegion} */
		public String texturePrefix = "i ";

		/** what buffer size of the reader should be used to read the {@link #texturePrefix} line
		 * @see FileHandle#reader(int) */
		public int readerBuffer = 1024;

		/** the possible file name extensions of the texture file */
		public String[] textureExtensions = new String[]{"png", "PNG", "jpeg", "JPEG", "jpg", "JPG", "cim", "CIM", "etc1", "ETC1",
			"ktx", "KTX", "zktx", "ZKTX"};

	}

	private static final PolygonRegionParameters defaultParameters = new PolygonRegionParameters();

	public PolygonRegionLoader () {
		this(new InternalFileHandleResolver());
	}

	public PolygonRegionLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public PolygonRegion load (String path, PolygonRegionParameters params, AssetLoadingContext<PolygonRegion> ctx) throws Exception {
		if (params == null) params = defaultParameters;

		String image = null;
		FileHandle file = resolve(path);
		EarClippingTriangulator triangulator = new EarClippingTriangulator();

		try (BufferedReader reader = file.reader(params.readerBuffer)) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.startsWith(params.texturePrefix)) {
					image = line.substring(params.texturePrefix.length());
					break;
				}
			}
		}

		if (image == null && params.textureExtensions != null) {
			for (String extension : params.textureExtensions) {
				FileHandle sibling = file.sibling(file.nameWithoutExtension() + "." + extension);
				if (sibling.exists()) image = sibling.name();
			}
		}

		if (image == null) {
			// TODO celestialgdx - this what I'm supposed to do?
			throw new IllegalStateException("missing image");
		}

		Texture texture = ctx.dependOn(file.sibling(image), Texture.class);
		return load(new TextureRegion(texture), triangulator, file);
	}

	/** Loads a PolygonRegion from a PSH (Polygon SHape) file. The PSH file format defines the polygon vertices before
	 * triangulation:
	 * <p>
	 * s 200.0, 100.0, ...
	 * <p>
	 * Lines not prefixed with "s" are ignored. PSH files can be created with external tools, eg: <br>
	 * https://code.google.com/p/libgdx-polygoneditor/ <br>
	 * http://www.codeandweb.com/physicseditor/
	 * @param file file handle to the shape definition file */
	public PolygonRegion load (TextureRegion textureRegion, EarClippingTriangulator triangulator, FileHandle file) {
		try(BufferedReader reader = file.reader(256)) {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("s")) {
					// Read shape.
					String[] polygonStrings = line.substring(1).trim().split(",");
					float[] vertices = new float[polygonStrings.length];
					for (int i = 0, n = vertices.length; i < n; i++)
						vertices[i] = Float.parseFloat(polygonStrings[i]);
					// It would probably be better if PSH stored the vertices and triangles, then we don't have to triangulate here.
					return new PolygonRegion(textureRegion, vertices, triangulator.computeTriangles(vertices).toArray());
				}
			}
		} catch (IOException ex) {
			throw new GdxIoException("Error reading polygon shape file: " + file, ex);
		}
		throw new GdxRuntimeException("Polygon shape not found: " + file);
	}

}
