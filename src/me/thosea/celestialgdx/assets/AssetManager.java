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
import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.assets.loaders.ShaderLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.glutils.Shader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import me.thosea.celestialgdx.core.CelestialGdx;
import me.thosea.celestialgdx.image.Pixmap;
import me.thosea.celestialgdx.image.Texture;
import me.thosea.celestialgdx.log.GdxLogger;
import me.thosea.celestialgdx.maps.TiledMap;
import me.thosea.celestialgdx.maps.TiledProject;
import me.thosea.celestialgdx.maps.Tileset;
import me.thosea.celestialgdx.maps.loader.TiledProjectLoader;
import me.thosea.celestialgdx.maps.loader.TmxMapLoader;
import me.thosea.celestialgdx.maps.loader.TsxTilesetLoader;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Loads and stores assets like textures, bitmapfonts, tile maps, sounds, music and so on.
 * @author mzechner, made parallel by Thosea
 */
public class AssetManager implements Disposable {
	static final class Asset {
		final String fileName;
		final Class<?> type;
		final Object object;

		final AtomicInteger refCount;

		Asset(String fileName, Class<?> type, Object object, AtomicInteger refCount) {
			this.fileName = fileName;
			this.type = type;
			this.object = object;
			this.refCount = refCount;
		}
	}

	// TODO celestialgdx - is using package-private variables the neatest way for this?

	final Map<String, Asset> assets = new ConcurrentHashMap<>();
	final Map<String, List<String>> assetDependencies = new ConcurrentHashMap<>();

	private final Map<Class<?>, AssetLoader<?, ?>> loaders = new ConcurrentHashMap<>();
	final Map<String, AssetLoadingContext<?>> tasks = new ConcurrentHashMap<>();

	final ExecutorService workExecutor = Executors.newFixedThreadPool(2, runnable -> {
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setDaemon(true);
		return thread;
	});
	volatile AssetErrorListener listener;

	private final ReadWriteLock countLock = new ReentrantReadWriteLock();
	private int loaded;
	private int toLoad;

	private final FileHandleResolver resolver;

	public final CelestialGdx gdx;
	public final GdxLogger logger;

	/** Creates a new AssetManager with all default loaders. */
	public AssetManager(CelestialGdx gdx) {
		this(gdx, new InternalFileHandleResolver());
	}

	/** Creates a new AssetManager with all default loaders. */
	public AssetManager(CelestialGdx gdx, FileHandleResolver resolver) {
		this(gdx, resolver, true);
	}

	/**
	 * Creates a new AssetManager with optionally all default loaders. If you don't add the default loaders then you do have to
	 * manually add the loaders you need, including any loaders they might depend on.
	 * @param defaultLoaders whether to add the default loaders
	 */
	public AssetManager(CelestialGdx gdx, FileHandleResolver resolver, boolean defaultLoaders) {
		this.gdx = gdx;
		this.resolver = resolver;
		this.logger = gdx.createLogger("AssetManager");
		if(defaultLoaders) {
			setLoader(Pixmap.class, new PixmapLoader(resolver));
			setLoader(Texture.class, new TextureLoader(resolver));
			setLoader(Shader.class, new ShaderLoader(resolver));

			setLoader(TiledProject.class, new TiledProjectLoader(resolver));
			setLoader(Tileset.class, new TsxTilesetLoader(resolver));
			setLoader(TiledMap.class, new TmxMapLoader(resolver));
		}
	}

	/**
	 * Returns the {@link FileHandleResolver} for which this AssetManager was loaded with.
	 * @return the file handle resolver which this AssetManager uses
	 */
	public FileHandleResolver getFileHandleResolver() {
		return resolver;
	}

	/**
	 * @param fileName the asset file name
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded
	 */
	public <T> T get(String fileName) {
		return get(fileName, true);
	}

	/**
	 * @param fileName the asset file name
	 * @param type the asset type
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded
	 */
	public <T> T get(String fileName, Class<T> type) {
		return get(fileName, type, true);
	}

	/**
	 * @param fileName the asset file name
	 * @param required true to throw GdxRuntimeException if the asset is not loaded, else null is returned
	 * @return the asset or null if it is not loaded and required is false
	 */
	public @Nullable <T> T get(String fileName, boolean required) {
		Asset asset = assets.get(fileName);
		if(asset != null) {
			return (T) asset.object;
		}
		if(required) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return null;
	}

	/**
	 * @param fileName the asset file name
	 * @param type the asset type
	 * @param required true to throw GdxRuntimeException if the asset is not loaded, else null is returned
	 * @return the asset or null if it is not loaded and required is false
	 */
	public @Nullable <T> T get(String fileName, Class<T> type, boolean required) {
		Asset asset = assets.get(fileName);
		if(asset != null && asset.type == type) {
			return (T) asset.object;
		}
		if(required) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return null;
	}

	/**
	 * @param assetDescriptor the asset descriptor
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded
	 */
	public <T> T get(AssetDescriptor<T> assetDescriptor) {
		return get(assetDescriptor.fileName, assetDescriptor.type, true);
	}

	/**
	 * @param type the asset type
	 * @return all the assets matching the specified type
	 */
	public <T> Array<T> getAll(Class<T> type, Array<T> out) {
		assets.values().forEach(asset -> {
			if(asset.type == type) out.add((T) asset.object);
		});
		return out;
	}

	/**
	 * Returns true if an asset with the specified name is loading, queued to be loaded, or has been loaded.
	 */
	public boolean contains(String fileName) {
		if(isLoaded(fileName)) return true;
		return tasks.containsKey(fileName);
	}

	/**
	 * Returns true if an asset with the specified name and type is loading, queued to be loaded, or has been loaded.
	 */
	public boolean contains(String fileName, Class<?> type) {
		if(isLoaded(fileName, type)) return true;

		for(AssetLoadingContext<?> ctx : tasks.values()) {
			if(ctx.desc.type == type && ctx.desc.fileName.equals(fileName)) return true;
		}
		return false;
	}

	/**
	 * Removes the asset and all its dependencies, if they are not used by other assets.
	 * @param fileName the file name
	 */
	public synchronized void unload(String fileName) {
		AssetLoadingContext<?> ctx = tasks.remove(fileName);
		if(ctx != null) {
			logger.info("Unload (from tasks): " + fileName);
			ctx.cancel();
			writeCount(() -> toLoad--);
			return;
		}

		Asset asset = assets.get(fileName);
		if(asset == null) throw new GdxRuntimeException("Asset not loaded: " + fileName);

		// remove any dependencies (or just decrement their ref count).
		List<String> dependencies = assetDependencies.get(fileName);
		if(dependencies != null) {
			for(String dependency : dependencies) {
				if(isLoaded(dependency)) unload(dependency);
			}
		}

		// if it is reference counted, decrement ref count and check if we can really get rid of it.
		if(asset.refCount.decrementAndGet() <= 0) {
			logger.info("Unload (dispose): " + fileName);

			// if it is disposable dispose it
			if(asset.object instanceof Disposable disposable) disposable.dispose();

			// remove the asset from the manager.
			assets.remove(fileName);
			assetDependencies.remove(fileName);
		} else {
			logger.info("Unload (decrement): " + fileName);
		}
	}

	/**
	 * @param object the asset
	 * @return whether the asset is contained in this manager
	 */
	public <T> boolean containsAsset(T object) {
		for(Asset asset : assets.values())
			if(asset.object == object) return true;
		return false;
	}

	/**
	 * @param object the asset
	 * @return the filename of the asset or null
	 */
	public <T> String getAssetFileName(T object) {
		for(Asset asset : assets.values())
			if(asset.object == object) return asset.fileName;
		return null;
	}

	/**
	 * @param assetDesc the AssetDescriptor of the asset
	 * @return whether the asset is loaded
	 */
	public boolean isLoaded(AssetDescriptor<?> assetDesc) {
		return isLoaded(assetDesc.fileName);
	}

	/**
	 * @param fileName the file name of the asset
	 * @return whether the asset is loaded
	 */
	public boolean isLoaded(String fileName) {
		if(fileName == null) return false;
		return assets.containsKey(fileName);
	}

	/**
	 * @param fileName the file name of the asset
	 * @return whether the asset is loaded
	 */
	public boolean isLoaded(String fileName, Class<?> type) {
		Asset asset = assets.get(fileName);
		return asset != null && asset.type == type;
	}

	/**
	 * Returns the default loader for the given type.
	 * @param type The type of the loader to get
	 * @return The loader capable of loading the type, or null if none exists
	 */
	public <T> AssetLoader<T, ?> getLoader(final Class<T> type) {
		return (AssetLoader<T, ?>) loaders.get(type);
	}

	/**
	 * Adds the given asset to the loading queue of the AssetManager.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 * @param type the type of the asset.
	 */
	public <T> void load(String fileName, Class<T> type) {
		load(fileName, type, null);
	}

	/**
	 * Adds the given asset to the loading queue of the AssetManager.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 * @param type the type of the asset.
	 * @param parameter parameters for the AssetLoader.
	 */
	public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		AssetLoader<T, ?> loader = getLoader(type);
		if(loader == null) throw new GdxRuntimeException("No loader for type: " + type.getSimpleName());

		AssetLoadingContext<?> existing = tasks.get(fileName);
		if(existing != null) {
			if(existing.desc.type != type) {
				throw new GdxRuntimeException(
						"Asset with name '" + fileName + "' already in task list, but has different type (expected: "
								+ type.getSimpleName() + ", found: " + existing.desc.type.getSimpleName() + ")");
			} else {
				existing.incrementRefCount();
				return;
			}
		}

		// reset stats
		writeCount(() -> {
			if(tasks.isEmpty()) {
				loaded = 0;
				toLoad = 1;
			} else {
				toLoad++;
			}
		});

		var desc = new AssetDescriptor<>(fileName, type, parameter);

		AssetLoadingContext<T> ctx = new AssetLoadingContext<>(this, desc, loader);
		tasks.put(fileName, ctx);

		ctx.schedule();
		logger.debug("Queued: " + desc);
	}

	/**
	 * Adds the given asset to the loading queue of the AssetManager.
	 * @param desc the {@link AssetDescriptor}
	 */
	public <T> void load(AssetDescriptor<T> desc) {
		load(desc.fileName, desc.type, desc.params);
	}

	public boolean isFinished() {
		return tasks.isEmpty();
	}

	/** Blocks until all assets are loaded. */
	public void finishLoading() {
		logger.debug("Waiting for loading to complete...");
		AssetLoadingContext<?> ctx = null;
		while(true) {
			if(!tasks.isEmpty()) {
				ctx = tasks.values().iterator().next();
			}
			if(ctx == null) {
				logger.debug("Loading complete.");
				return;
			}
			try {
				ctx.awaitResult();
			} catch(AssetLoadingContext.TaskNotActiveException ignored) {
			}
		}
	}

	/**
	 * Blocks until the specified asset is loaded.
	 * @param assetDesc the AssetDescriptor of the asset
	 */
	public <T> T finishLoadingAsset(AssetDescriptor<T> assetDesc) {
		return finishLoadingAsset(assetDesc.fileName);
	}

	/**
	 * Blocks until the specified asset is loaded.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 */
	public <T> T finishLoadingAsset(String fileName) {
		logger.debug("Waiting for asset to be loaded: " + fileName);
		AssetLoadingContext<?> ctx = tasks.get(fileName);
		if(ctx != null) {
			return (T) ctx.awaitResult();
		} else {
			return get(fileName);
		}
	}

	public <T> void addAsset(final String fileName, Class<T> type, T object) {
		assets.put(fileName, new Asset(fileName, type, object, new AtomicInteger(1)));
	}

	/**
	 * Sets a new {@link AssetLoader} for the given type.
	 * @param type the type of the asset
	 * @param loader the loader
	 */
	public <T, P extends AssetLoaderParameters<T>> void setLoader(Class<T> type, AssetLoader<T, P> loader) {
		if(type == null) throw new IllegalArgumentException("type cannot be null.");
		if(loader == null) throw new IllegalArgumentException("loader cannot be null.");
		logger.debug("Loader set: " + type.getSimpleName() + " -> " + loader.getClass().getSimpleName());
		loaders.put(type, loader);
	}

	/** @return the number of loaded assets */
	public int getLoadedAssets() {
		return assets.size();
	}

	/** @return the number of currently queued assets */
	public int getQueuedAssets() {
		return tasks.size();
	}

	/** @return the progress in percent of completion. */
	public float getProgress() {
		return readCount(() -> {
			if(toLoad == 0) return 1;
			return Math.min(1, loaded / (float) toLoad);
		}).floatValue();
	}

	/**
	 * Disposes all assets in the manager and stops all asynchronous loading.
	 */
	@Override
	public void dispose() {
		logger.debug("Disposing.");
		clear();
	}

	/**
	 * Clears and disposes all assets and the preloading queue.
	 */
	public void clear() {
		tasks.values().forEach(AssetLoadingContext::cancel);
		tasks.clear();
		assets.clear();
		assetDependencies.clear();

		writeCount(() -> {
			this.loaded = 0;
			this.toLoad = 0;
		});
	}

	/**
	 * @return the {@link GdxLogger} used by the {@link AssetManager}
	 */
	public GdxLogger getLogger() {
		return logger;
	}

	/**
	 * Returns the reference count of an asset.
	 * @param fileName
	 */
	public int getReferenceCount(String fileName) {
		Asset asset = assets.get(fileName);
		if(asset == null) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return asset.refCount.get();
	}

	/** @return a string containing ref count and dependency information for all assets. */
	public synchronized String getDiagnostics() {
		StringBuilder buffer = new StringBuilder(256);
		for(Map.Entry<String, Asset> entry : assets.entrySet()) {
			if(!buffer.isEmpty()) buffer.append('\n');
			buffer.append(entry.getKey());
			buffer.append(", ");
			buffer.append(entry.getValue().object);
			buffer.append(", refs: ");
			buffer.append(entry.getValue().refCount);

			List<String> dependencies = assetDependencies.get(entry.getKey());
			if(dependencies != null) {
				buffer.append(", deps: [");
				for(String dep : dependencies) {
					buffer.append(dep);
					buffer.append(',');
				}
				buffer.append(']');
			}
		}
		return buffer.toString();
	}

	/** @return the file names of all loaded assets. */
	public String[] getAssetNames() {
		synchronized(assets) {
			return assets.keySet().toArray(new String[0]);
		}
	}

	/** @return the dependencies of an asset or an empty list if the asset has no dependencies. */
	public List<String> getDependencies(String fileName) {
		return assetDependencies.get(fileName);
	}

	/**
	 * @return the type of a loaded asset.
	 */
	public Class<?> getAssetType(String fileName) {
		Asset asset = assets.get(fileName);
		return asset != null ? asset.type : null;
	}

	private <T> T readCount(Supplier<T> supplier) {
		countLock.readLock().lock();
		try {
			return supplier.get();
		} finally {
			countLock.readLock().unlock();
		}
	}

	private void writeCount(Runnable action) {
		countLock.writeLock().lock();
		try {
			action.run();
		} finally {
			countLock.writeLock().unlock();
		}
	}
}