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

package com.badlogic.gdx.assets;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.ThreadUtils;

import java.lang.StringBuilder;

/**
 * Loads and stores assets like textures, bitmapfonts, tile maps, sounds, music and so on.
 *
 * @author mzechner
 */
public class AssetManager implements Disposable {
	class Asset {
		final String fileName;
		final Class<?> type;
		Object object;
		int refCount = 1;

		Asset (String fileName, Class<?> type) {
			this.fileName = fileName;
			this.type = type;
		}
	}

	private final ObjectMap<String, Asset> assets = new ObjectMap<>();
	private final ObjectMap<String, Array<String>> assetDependencies = new ObjectMap<>();
	private final ObjectSet<String> injected = new ObjectSet<>();

	private final ObjectMap<Class<?>, AssetLoader<?, ?>> loaders = new ObjectMap<>();
	private final Array<AssetDescriptor<?>> loadQueue = new Array<>();

	private final AsyncExecutor executor = new AsyncExecutor(1, "AssetManager");
	private final Array<AssetLoadingTask> tasks = new Array<>();

	private AssetErrorListener listener;
	private int loaded;
	private int toLoad;
	private int peakTasks;

	final FileHandleResolver resolver;

	public final Logger log = new Logger("AssetManager", Application.LOG_NONE);

	/** Creates a new AssetManager with all default loaders. */
	public AssetManager () {
		this(new InternalFileHandleResolver());
	}

	/** Creates a new AssetManager with all default loaders. */
	public AssetManager (FileHandleResolver resolver) {
		this(resolver, true);
	}

	/** Creates a new AssetManager with optionally all default loaders. If you don't add the default loaders then you do have to
	 * manually add the loaders you need, including any loaders they might depend on.
	 * @param defaultLoaders whether to add the default loaders */
	public AssetManager (FileHandleResolver resolver, boolean defaultLoaders) {
		this.resolver = resolver;
		if (defaultLoaders) {
			setLoader(BitmapFont.class, new BitmapFontLoader(resolver));
			setLoader(Pixmap.class, new PixmapLoader(resolver));
			setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
			setLoader(Texture.class, new TextureLoader(resolver));
			setLoader(Skin.class, new SkinLoader(resolver));
			setLoader(ParticleEffect.class, new ParticleEffectLoader(resolver));
			setLoader(com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.class,
					new com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader(resolver));
			setLoader(PolygonRegion.class, new PolygonRegionLoader(resolver));
			setLoader(I18NBundle.class, new I18NBundleLoader(resolver));
			// TODO celestialgdx - what do i do with these??
			//			setLoader(Model.class, ".g3dj", new G3dModelLoader(new JsonReader(), resolver));
//			setLoader(Model.class, ".g3db", new G3dModelLoader(new UBJsonReader(), resolver));
//			setLoader(Model.class, ".obj", new ObjLoader(resolver));
			setLoader(ShaderProgram.class, new ShaderProgramLoader(resolver));
			setLoader(Cubemap.class, new CubemapLoader(resolver));
		}
	}

	/** Returns the {@link FileHandleResolver} for which this AssetManager was loaded with.
	 * @return the file handle resolver which this AssetManager uses */
	public FileHandleResolver getFileHandleResolver () {
		return resolver;
	}

	/** @param fileName the asset file name
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded */
	public synchronized <T> T get (String fileName) {
		return get(fileName, true);
	}

	/** @param fileName the asset file name
	 * @param type the asset type
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded */
	public synchronized <T> T get (String fileName, Class<T> type) {
		return get(fileName, type, true);
	}

	/** @param fileName the asset file name
	 * @param required true to throw GdxRuntimeException if the asset is not loaded, else null is returned
	 * @return the asset or null if it is not loaded and required is false */
	public synchronized @Null <T> T get (String fileName, boolean required) {
		Asset asset = assets.get(fileName);
		if (asset != null) {
			return (T) asset.object;
		}
		if (required) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return null;
	}

	/** @param fileName the asset file name
	 * @param type the asset type
	 * @param required true to throw GdxRuntimeException if the asset is not loaded, else null is returned
	 * @return the asset or null if it is not loaded and required is false */
	public synchronized @Null <T> T get (String fileName, Class<T> type, boolean required) {
		Asset asset = assets.get(fileName);
		if (asset != null && asset.type == type) {
			return (T) asset.object;
		}
		if (required) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return null;
	}

	/** @param assetDescriptor the asset descriptor
	 * @return the asset
	 * @throws GdxRuntimeException if the asset is not loaded */
	public synchronized <T> T get (AssetDescriptor<T> assetDescriptor) {
		return get(assetDescriptor.fileName, assetDescriptor.type, true);
	}

	/** @param type the asset type
	 * @return all the assets matching the specified type */
	public synchronized <T> Array<T> getAll (Class<T> type, Array<T> out) {
		assets.values().forEach(asset -> {
			if (asset.type == type) out.add((T) asset.object);
		});
		return out;
	}

	/**
	 * Returns true if an asset with the specified name is loading, queued to be loaded, or has been loaded.
	 */
	public synchronized boolean contains (String fileName) {
		if (tasks.size > 0 && tasks.first().assetDesc.fileName.equals(fileName)) return true;

		for (int i = 0; i < loadQueue.size; i++)
			if (loadQueue.get(i).fileName.equals(fileName)) return true;

		return isLoaded(fileName);
	}

	/**
	 * Returns true if an asset with the specified name and type is loading, queued to be loaded, or has been loaded.
	 */
	public synchronized boolean contains (String fileName, Class<?> type) {
		for (AssetDescriptor<?> desc : loadQueue) {
			if (desc.type == type && desc.fileName.equals(fileName)) return true;
		}

		return isLoaded(fileName, type);
	}

	/**
	 * Removes the asset and all its dependencies, if they are not used by other assets.
	 *
	 * @param fileName the file name
	 */
	public synchronized void unload (String fileName) {
		// check if it's currently processed (and the first element in the stack, thus not a dependency) and cancel if necessary
		if (tasks.size > 0) {
			AssetLoadingTask currentTask = tasks.first();
			if (currentTask.assetDesc.fileName.equals(fileName)) {
				log.info("Unload (from tasks): " + fileName);
				currentTask.cancel = true;
				currentTask.unload();
				return;
			}
		}

		Asset asset = assets.get(fileName);
		if (asset == null) throw new GdxRuntimeException("Asset not loaded: " + fileName);

		// check if it's in the queue
		int foundIndex = -1;
		for (int i = 0; i < loadQueue.size; i++) {
			if (loadQueue.get(i).fileName.equals(fileName)) {
				foundIndex = i;
				break;
			}
		}
		if (foundIndex != -1) {
			toLoad--;
			AssetDescriptor<?> desc = loadQueue.removeIndex(foundIndex);
			log.info("Unload (from queue): " + fileName);

			// if the queued asset was already loaded, let the callback know it is available.
			if (desc.params != null && desc.params.loadedCallback != null)
				desc.params.loadedCallback.finishedLoading(this, desc.fileName, desc.type);
			return;
		}

		// if it is reference counted, decrement ref count and check if we can really get rid of it.
		asset.refCount--;
		if (asset.refCount <= 0) {
			log.info("Unload (dispose): " + fileName);

			// if it is disposable dispose it
			if (asset.object instanceof Disposable disposable) disposable.dispose();

			// remove the asset from the manager.
			assets.remove(fileName);
		} else {
			log.info("Unload (decrement): " + fileName);
		}

		// remove any dependencies (or just decrement their ref count).
		Array<String> dependencies = assetDependencies.get(fileName);
		if (dependencies != null) {
			for (String dependency : dependencies)
				if (isLoaded(dependency)) unload(dependency);
		}
		// remove dependencies if ref count < 0
		if (asset.refCount <= 0) assetDependencies.remove(fileName);
	}

	/** @param object the asset
	 * @return whether the asset is contained in this manager */
	public synchronized <T> boolean containsAsset (T object) {
		for (Asset asset : assets.values())
			if (asset.object == object) return true;
		return false;
	}

	/**
	 * @param object the asset
	 * @return the filename of the asset or null
	 */
	public synchronized <T> String getAssetFileName (T object) {
		for (Asset asset : assets.values())
			if (asset.object == object) return asset.fileName;
		return null;
	}

	/**
	 * @param assetDesc the AssetDescriptor of the asset
	 * @return whether the asset is loaded
	 */
	public synchronized boolean isLoaded (AssetDescriptor<?> assetDesc) {
		return isLoaded(assetDesc.fileName);
	}

	/**
	 * @param fileName the file name of the asset
	 * @return whether the asset is loaded
	 */
	public synchronized boolean isLoaded (String fileName) {
		if (fileName == null) return false;
		return assets.containsKey(fileName);
	}

	/**
	 * @param fileName the file name of the asset
	 * @return whether the asset is loaded
	 */
	public synchronized boolean isLoaded (String fileName, Class<?> type) {
		Asset asset = assets.get(fileName);
		return asset != null && asset.type == type;
	}

	/**
	 * Returns the default loader for the given type.
	 *
	 * @param type The type of the loader to get
	 * @return The loader capable of loading the type, or null if none exists
	 */
	public <T> AssetLoader<T, ?> getLoader (final Class<T> type) {
		return (AssetLoader<T, ?>) loaders.get(type);
	}

	/**
	 * Adds the given asset to the loading queue of the AssetManager.
	 *
	 * @param fileName the file name (interpretation depends on {@link AssetLoader})
	 * @param type     the type of the asset.
	 */
	public synchronized <T> void load (String fileName, Class<T> type) {
		load(fileName, type, null);
	}

	/**
	 * Adds the given asset to the loading queue of the AssetManager.
	 *
	 * @param fileName  the file name (interpretation depends on {@link AssetLoader})
	 * @param type      the type of the asset.
	 * @param parameter parameters for the AssetLoader.
	 */
	public synchronized <T> void load (String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
		AssetLoader<T, ?> loader = getLoader(type);
		if (loader == null) throw new GdxRuntimeException("No loader for type: " + type.getSimpleName());

		// reset stats
		if (loadQueue.size == 0) {
			loaded = 0;
			toLoad = 0;
			peakTasks = 0;
		}

		// check if an asset with the same name but a different type has already been added.

		// check preload queue
		for (int i = 0; i < loadQueue.size; i++) {
			AssetDescriptor<?> desc = loadQueue.get(i);
			if (desc.fileName.equals(fileName) && desc.type != type) throw new GdxRuntimeException(
					"Asset with name '" + fileName + "' already in preload queue, but has different type (expected: "
							+ type.getSimpleName() + ", found: " + desc.type.getSimpleName() + ")");
		}

		// check task list
		for (int i = 0; i < tasks.size; i++) {
			AssetDescriptor<?> desc = tasks.get(i).assetDesc;
			if (desc.fileName.equals(fileName) && desc.type != type) throw new GdxRuntimeException(
					"Asset with name '" + fileName + "' already in task list, but has different type (expected: "
							+ type.getSimpleName() + ", found: " + desc.type.getSimpleName() + ")");
		}

		toLoad++;
		var assetDesc = new AssetDescriptor<>(fileName, type, parameter);
		loadQueue.add(assetDesc);
		log.debug("Queued: " + assetDesc);
	}

	/** Adds the given asset to the loading queue of the AssetManager.
	 * @param desc the {@link AssetDescriptor} */
	public synchronized void load (AssetDescriptor desc) {
		load(desc.fileName, desc.type, desc.params);
	}

	/** Updates the AssetManager for a single task. Returns if the current task is still being processed or there are no tasks,
	 * otherwise it finishes the current task and starts the next task.
	 * @return true if all loading is finished. */
	public synchronized boolean update () {
		try {
			if (tasks.size == 0) {
				// loop until we have a new task ready to be processed
				while (loadQueue.size != 0 && tasks.size == 0)
					nextTask();
				// have we not found a task? We are done!
				if (tasks.size == 0) return true;
			}
			return updateTask() && loadQueue.size == 0 && tasks.size == 0;
		} catch (Throwable t) {
			handleTaskError(t);
			return loadQueue.size == 0;
		}
	}

	/**
	 * Updates the AssetManager continuously for the specified number of milliseconds, yielding the CPU to the loading thread
	 * between updates. This may block for less time if all loading tasks are complete. This may block for more time if the portion
	 * of a single task that happens in the GL thread takes a long time. On GWT, updates for a single task instead (see
	 * {@link #update()}).
	 *
	 * @return true if all loading is finished.
	 */
	public boolean update (int millis) {
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) return update();
		long endTime = TimeUtils.millis() + millis;
		while (true) {
			boolean done = update();
			if (done || TimeUtils.millis() > endTime) return done;
			ThreadUtils.yield();
		}
	}

	/** Returns true when all assets are loaded. Can be called from any thread but note {@link #update()} or related methods must
	 * be called to process tasks. */
	public synchronized boolean isFinished () {
		return loadQueue.size == 0 && tasks.size == 0;
	}

	/** Blocks until all assets are loaded. */
	public void finishLoading () {
		log.debug("Waiting for loading to complete...");
		while (!update())
			ThreadUtils.yield();
		log.debug("Loading complete.");
	}

	/** Blocks until the specified asset is loaded.
	 * @param assetDesc the AssetDescriptor of the asset */
	public <T> T finishLoadingAsset (AssetDescriptor assetDesc) {
		return finishLoadingAsset(assetDesc.fileName);
	}

	/** Blocks until the specified asset is loaded.
	 * @param fileName the file name (interpretation depends on {@link AssetLoader}) */
	public <T> T finishLoadingAsset (String fileName) {
		log.debug("Waiting for asset to be loaded: " + fileName);
		Asset asset = assets.get(fileName);
		while (true) {
			synchronized (this) {
				if (asset != null && asset.object != null) {
					log.debug("Asset loaded: " + fileName);
					return (T) asset.object;
				}
				update();
			}
			Thread.yield();
		}
	}

	synchronized void injectDependencies (String parentAssetFilename, Array<AssetDescriptor> dependendAssetDescs) {
		ObjectSet<String> injected = this.injected;
		for (AssetDescriptor<?> desc : dependendAssetDescs) {
			if (injected.contains(desc.fileName)) continue; // Ignore subsequent dependencies if there are duplicates.
			injected.add(desc.fileName);
			injectDependency(parentAssetFilename, desc);
		}
		injected.clear(32);
	}

	private synchronized void injectDependency (String parentAssetFilename, AssetDescriptor dependendAssetDesc) {
		// add the asset as a dependency of the parent asset
		Array<String> dependencies = assetDependencies.get(parentAssetFilename);
		if (dependencies == null) {
			dependencies = new Array();
			assetDependencies.put(parentAssetFilename, dependencies);
		}
		dependencies.add(dependendAssetDesc.fileName);

		// if the asset is already loaded, increase its reference count.
		Asset asset = assets.get(dependendAssetDesc.fileName);
		if (asset != null) {
			log.debug("Dependency already loaded: " + dependendAssetDesc);
			asset.refCount++;
			incrementRefCountedDependencies(dependendAssetDesc.fileName);
		} else {
			// else add a new task for the asset.
			log.info("Loading dependency: " + dependendAssetDesc);
			addTask(dependendAssetDesc);
		}
	}

	/** Removes a task from the loadQueue and adds it to the task stack. If the asset is already loaded (which can happen if it was
	 * a dependency of a previously loaded asset) its reference count will be increased. */
	private void nextTask () {
		AssetDescriptor assetDesc = loadQueue.removeIndex(0);

		// if the asset not meant to be reloaded and is already loaded, increase its reference count
		Asset asset = assets.get(assetDesc.fileName);
		if (asset != null) {
			asset.refCount++;
			incrementRefCountedDependencies(assetDesc.fileName);
			if (assetDesc.params != null && assetDesc.params.loadedCallback != null)
				assetDesc.params.loadedCallback.finishedLoading(this, assetDesc.fileName, assetDesc.type);
			loaded++;
		} else {
			// else add a new task for the asset.
			log.info("Loading: " + assetDesc);
			addTask(assetDesc);
		}
	}

	/** Adds a {@link AssetLoadingTask} to the task stack for the given asset. */
	private void addTask (AssetDescriptor assetDesc) {
		AssetLoader loader = getLoader(assetDesc.type);
		if (loader == null) throw new GdxRuntimeException("No loader for type: " + assetDesc.type.getSimpleName());
		tasks.add(new AssetLoadingTask(this, assetDesc, loader, executor));
		peakTasks++;
	}

	/** Adds an asset to this AssetManager */
	public <T> void addAsset (final String fileName, Class<T> type, T object) {
		// add the asset to the filename lookup
		Asset asset = new Asset(fileName, type);
		asset.object = object;
		assets.put(fileName, asset);
	}

	/**
	 * Updates the current task on the top of the task stack.
	 *
	 * @return true if the asset is loaded or the task was cancelled.
	 */
	private boolean updateTask () {
		AssetLoadingTask task = tasks.peek();

		boolean complete = true;
		try {
			complete = task.cancel || task.update();
		} catch (RuntimeException ex) {
			task.cancel = true;
			taskFailed(task.assetDesc, ex);
		}

		// if the task has been cancelled or has finished loading
		if (complete) {
			// increase the number of loaded assets and pop the task from the stack
			if (tasks.size == 1) {
				loaded++;
				peakTasks = 0;
			}
			tasks.pop();

			if (task.cancel) return true;

			addAsset(task.assetDesc.fileName, task.assetDesc.type, task.asset);

			// otherwise, if a listener was found in the parameter invoke it
			if (task.assetDesc.params != null && task.assetDesc.params.loadedCallback != null)
				task.assetDesc.params.loadedCallback.finishedLoading(this, task.assetDesc.fileName, task.assetDesc.type);

			long endTime = TimeUtils.nanoTime();
			log.debug("Loaded: " + (endTime - task.startTime) / 1000000f + "ms " + task.assetDesc);

			return true;
		}
		return false;
	}

	/** Called when a task throws an exception during loading. The default implementation rethrows the exception. A subclass may
	 * supress the default implementation when loading assets where loading failure is recoverable. */
	protected void taskFailed (AssetDescriptor assetDesc, RuntimeException ex) {
		throw ex;
	}

	private void incrementRefCountedDependencies (String parent) {
		Array<String> dependencies = assetDependencies.get(parent);
		if (dependencies == null) return;

		for (String dependency : dependencies) {
			Asset asset = assets.get(dependency);
			asset.refCount++;
			incrementRefCountedDependencies(dependency);
		}
	}

	/** Handles a runtime/loading error in {@link #update()} by optionally invoking the {@link AssetErrorListener}.
	 * @param t */
	private void handleTaskError (Throwable t) {
		log.error("Error loading asset.", t);

		if (tasks.isEmpty()) throw new GdxRuntimeException(t);

		// pop the faulty task from the stack
		AssetLoadingTask task = tasks.pop();
		AssetDescriptor assetDesc = task.assetDesc;

		// remove all dependencies
		if (task.dependenciesLoaded && task.dependencies != null) {
			for (AssetDescriptor desc : task.dependencies)
				unload(desc.fileName);
		}

		// clear the rest of the stack
		tasks.clear();

		// inform the listener that something bad happened
		if (listener != null)
			listener.error(assetDesc, t);
		else
			throw new GdxRuntimeException(t);
	}

	/** Sets a new {@link AssetLoader} for the given type.
	 * @param type the type of the asset
	 * @param loader the loader */
	public synchronized <T, P extends AssetLoaderParameters<T>> void setLoader (Class<T> type, AssetLoader<T, P> loader) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		if (loader == null) throw new IllegalArgumentException("loader cannot be null.");
		log.debug("Loader set: " + type.getSimpleName() + " -> " + loader.getClass().getSimpleName());
		loaders.put(type, loader);
	}

	/** @return the number of loaded assets */
	public synchronized int getLoadedAssets () {
		return assets.size;
	}

	/** @return the number of currently queued assets */
	public synchronized int getQueuedAssets () {
		return loadQueue.size + tasks.size;
	}

	/** @return the progress in percent of completion. */
	public synchronized float getProgress () {
		if (toLoad == 0) return 1;
		float fractionalLoaded = loaded;
		if (peakTasks > 0) {
			fractionalLoaded += ((peakTasks - tasks.size) / (float) peakTasks);
		}
		return Math.min(1, fractionalLoaded / toLoad);
	}

	/**
	 * Sets an {@link AssetErrorListener} to be invoked in case loading an asset failed.
	 *
	 * @param listener the listener or null
	 */
	public synchronized void setErrorListener (AssetErrorListener listener) {
		this.listener = listener;
	}

	/**
	 * Disposes all assets in the manager and stops all asynchronous loading.
	 */
	@Override
	public void dispose () {
		log.debug("Disposing.");
		clear();
		executor.dispose();
	}

	/**
	 * Clears and disposes all assets and the preloading queue.
	 */
	public void clear () {
		synchronized (this) {
			loadQueue.clear();
		}

		// Lock is temporarily released to yield to blocked executor threads
		// A pending async task can cause a deadlock if we do not release

		finishLoading();

		synchronized (this) {
			ObjectIntMap<String> dependencyCount = new ObjectIntMap<>();
			while (assets.size > 0) {
				// for each asset, figure out how often it was referenced
				dependencyCount.clear(51);
				for (String asset : assets.keys()) {
					Array<String> dependencies = assetDependencies.get(asset);
					if (dependencies == null) continue;
					for (String dependency : dependencies)
						dependencyCount.getAndIncrement(dependency, 0, 1);
				}

				// only dispose of assets that are root assets (not referenced)
				for (String asset : assets.keys())
					if (dependencyCount.get(asset, 0) == 0) unload(asset);
			}

			this.assets.clear(51);
			this.assetDependencies.clear(51);
			this.loaded = 0;
			this.toLoad = 0;
			this.peakTasks = 0;
			this.loadQueue.clear();
			this.tasks.clear();
		}
	}

	/**
	 * @return the {@link Logger} used by the {@link AssetManager}
	 */
	public Logger getLogger () {
		return log;
	}

	/** Returns the reference count of an asset.
	 * @param fileName */
	public synchronized int getReferenceCount (String fileName) {
		Asset asset = assets.get(fileName);
		if (asset == null) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		return asset.refCount;
	}

	/** Sets the reference count of an asset.
	 * @param fileName */
	public synchronized void setReferenceCount (String fileName, int refCount) {
		Asset asset = assets.get(fileName);
		if (asset == null) throw new GdxRuntimeException("Asset not loaded: " + fileName);
		asset.refCount = refCount;
	}

	/** @return a string containing ref count and dependency information for all assets. */
	public synchronized String getDiagnostics () {
		StringBuilder buffer = new StringBuilder(256);
		for (Entry<String, Asset> entry : assets.entries()) {
			if (!buffer.isEmpty()) buffer.append('\n');
			buffer.append(entry.key);
			buffer.append(", ");
			buffer.append(entry.value.object);
			buffer.append(", refs: ");
			buffer.append(entry.value.refCount);

			Array<String> dependencies = assetDependencies.get(entry.key);
			if (dependencies != null) {
				buffer.append(", deps: [");
				for (String dep : dependencies) {
					buffer.append(dep);
					buffer.append(',');
				}
				buffer.append(']');
			}
		}
		return buffer.toString();
	}

	/** @return the file names of all loaded assets. */
	public synchronized Array<String> getAssetNames () {
		return assets.keys().toArray();
	}

	/** @return the dependencies of an asset or null if the asset has no dependencies. */
	public synchronized Array<String> getDependencies (String fileName) {
		return assetDependencies.get(fileName);
	}

	/**
	 * @return the type of a loaded asset.
	 */
	public synchronized Class<?> getAssetType (String fileName) {
		Asset asset = assets.get(fileName);
		return asset != null ? asset.type : null;
	}
}
