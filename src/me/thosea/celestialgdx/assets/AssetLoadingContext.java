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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import me.thosea.celestialgdx.files.FileHandle;
import me.thosea.celestialgdx.log.GdxLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Responsible for loading an asset through an {@link AssetLoader} based on an {@link AssetDescriptor}.
 *
 */
public class AssetLoadingContext<T> {
	public final AssetManager manager;
	public final AssetDescriptor<T> desc;
	public final AssetLoader<T, ?> loader;

	private final CompletableFuture<T> future;
	//	private final long startTime;

	private final List<String> dependencies = new ArrayList<>(); // synchronized
	private final AtomicInteger refCount = new AtomicInteger(1);

	private final List<String> createdDependencies = Collections.synchronizedList(new ArrayList<>());

	private volatile boolean active = true;

	public AssetLoadingContext(AssetManager manager, AssetDescriptor<T> desc, AssetLoader<T, ?> loader) {
		this.manager = manager;
		this.desc = desc;
		this.loader = loader;
		this.future = new CompletableFuture<>();

		// TODO celestialgdx: do we keep this?
		//		this.startTime = manager.log.getLevel() == Logger.DEBUG ? TimeUtils.nanoTime() : 0;
	}

	void schedule() {
		Thread.startVirtualThread(this::load);
	}

	private void load() {
		try {
			requireActive();
			T result = loader.load(desc.fileName, cast(desc.params), this);
			requireActive();
			complete(result);
		} catch(TaskNotActiveException e) {
			future.completeExceptionally(e);
		} catch(Exception e) {
			manager.logger.error("Error loading " + desc.fileName, e);
			future.completeExceptionally(e);

			if(manager.listener != null)
				manager.listener.error(desc, e);
		} finally {
			active = false;
			manager.tasks.remove(desc.fileName, this);
		}
	}

	private void complete(T result) {
		List<String> dependencies;
		synchronized(this.dependencies) {
			if(this.dependencies.isEmpty()) {
				dependencies = Collections.emptyList();
			} else {
				// clone to avoid post modifications
				dependencies = List.of(this.dependencies.toArray(new String[0]));
			}
		}

		requireActive();

		var asset = new AssetManager.Asset(desc.fileName, desc.type, result, refCount);
		manager.assets.put(desc.fileName, asset);
		manager.assetDependencies.put(desc.fileName, dependencies);

		active = false;

		future.complete(result);

		if(desc.params != null && desc.params.loadedCallback != null)
			desc.params.loadedCallback.finishedLoading(manager, desc.fileName, desc.type);
	}

	public <D> D dependOn(AssetDescriptor<D> desc) {
		requireActive();
		manager.load(desc);
		synchronized(dependencies) {
			if(!dependencies.contains(desc.fileName)) {
				dependencies.add(desc.fileName);
			}
		}
		D result = manager.finishLoadingAsset(desc);
		requireActive();
		return result;
	}

	public <D> D dependOn(String path, Class<D> type) {
		return dependOn(new AssetDescriptor<>(path, type));
	}

	public <D> D dependOn(FileHandle file, Class<D> type) {
		return dependOn(new AssetDescriptor<>(file, type));
	}

	public <D, P> D dependOn(String path, Class<D> type, AssetLoaderParameters<P> parameter) {
		return dependOn(cast(new AssetDescriptor<>(path, type, (AssetLoaderParameters<D>) parameter)));
	}

	public <D> void createDependency(String path, Class<D> type, D object) {
		requireActive();

		if(manager.assets.containsKey(path)) {
			throw new IllegalStateException("Tried to replace asset " + path);
		}

		// it's us..we're the dependency
		var asset = new AssetManager.Asset(path, type, object, new AtomicInteger(1));
		manager.assets.put(path, asset);
		manager.assetDependencies.put(path, List.of(desc.fileName));

		createdDependencies.add(path);
	}

	public <T> T awaitMainThread(Supplier<T> supplier) {
		requireActive();
		CompletableFuture<T> future = new CompletableFuture<>();
		Gdx.app.postRunnable(() -> {
			try {
				future.complete(supplier.get());
			} catch(Exception e) {
				manager.logger.error("Error performing sync task for " + desc.fileName, e);
				future.completeExceptionally(e);
			}
		});
		T result = future.join();
		requireActive();
		return result;
	}

	// TODO celestialgdx use this more
	// or remove it if it doesn't actually improve performance
	public <T> T awaitWork(Callable<T> supplier) {
		requireActive();
		try {
			return manager.workExecutor.submit(supplier).get();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error performing work task", e);
		} finally {
			requireActive();
		}
	}

	public void awaitWork(Runnable work) {
		requireActive();
		try {
			manager.workExecutor.submit(work).get();
		} catch(InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error performing work task", e);
		} finally {
			requireActive();
		}
	}

	private static <T> T cast(Object obj) {
		return (T) obj;
	}

	// package-private to avoid accidental calls
	T awaitResult() {
		if(Gdx.app.isGameThread()) {
			// since many tasks require awaitMainThread,
			// we must poll events in between
			while(true) {
				try {
					return future.get(10L, TimeUnit.MILLISECONDS);
				} catch(InterruptedException | ExecutionException e) {
					throw new GdxRuntimeException(e);
				} catch(TimeoutException e) {
					Gdx.app.pollRunnables();
					// and then try again
				}
			}
		}

		try {
			return future.get();
		} catch(InterruptedException | ExecutionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public void cancel() {
		if(!active) {
			manager.unload(desc.fileName);
			return;
		}
		this.active = false;
		createdDependencies.forEach(manager::unload);
	}

	public GdxLogger logger() {
		return manager.getLogger();
	}

	void incrementRefCount() {
		if(!active) {
			manager.load(desc);
			return;
		}
		refCount.incrementAndGet();
	}

	private void requireActive() {
		if(!active) throw new TaskNotActiveException();
	}

	public static class TaskNotActiveException extends RuntimeException {
		public TaskNotActiveException() {
			super("This task either finished or was cancelled");
		}
	}
}