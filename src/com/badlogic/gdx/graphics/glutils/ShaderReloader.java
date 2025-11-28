package com.badlogic.gdx.graphics.glutils;

import me.thosea.celestialgdx.core.CelestialGdx;
import me.thosea.celestialgdx.log.GdxLogger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

/**
 * CelestialGDX -
 * Debug tool that watches the filesystem for changes to shader files
 * and automatically recompiles the shader when a change is detected.
 * Create with {@link #create(CelestialGdx)}, add entries with {@link #listen(String, Shader, Path, Path)}
 * then call {@link Thread#start()} to start and {@link #interrupt()} to stop.
 * Listeners cannot be added or removed after the thread is started.
 * @author thosea
 */
public final class ShaderReloader extends Thread {
	private final CelestialGdx gdx;
	private final GdxLogger logger;

	private final WatchService service;

	private final Map<String, Map<String, WatchEntry>> folders = new HashMap<>();

	private ShaderReloader(CelestialGdx gdx) {
		this.gdx = gdx;
		this.logger = gdx.createLogger("ShaderReloader");
		try {
			this.service = FileSystems.getDefault().newWatchService();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		this.setDaemon(true);
	}

	/**
	 * Registers a new shader that will be watched for changes
	 * @param name the name to be used in logs
	 * @param shader the shader to be reloaded when a change is detected
	 * @param vertexFile the path to the vertex file which will be watched
	 * @param fragmentFile the path to the fragment file which will be watched
	 */
	public void listen(String name, Shader shader, Path vertexFile, Path fragmentFile) {
		if(this.getState() != State.NEW) {
			throw new IllegalStateException("Cannot add listeners after the thread has started");
		}

		logger.info("Added listener for shader " + name);

		vertexFile = vertexFile.toAbsolutePath();
		fragmentFile = fragmentFile.toAbsolutePath();

		WatchEntry entry = new WatchEntry(name, shader, vertexFile, fragmentFile);
		try {
			entry.lastVertexContent = Files.readString(vertexFile);
			entry.lastFragmentContent = Files.readString(fragmentFile);
		} catch(IOException e) {
			throw new RuntimeException("Failed to read files", e);
		}

		try {
			addFile(vertexFile, entry);
			addFile(fragmentFile, entry);
		} catch(IOException e) {
			throw new RuntimeException("Failed to register file listeners", e);
		}
	}

	private void addFile(Path path, WatchEntry entry) throws IOException {
		Path parent = path.getParent();
		String parentPath = parent.toAbsolutePath().toString();

		Map<String, WatchEntry> folderMap = this.folders.get(parentPath);
		if(folderMap == null) {
			parent.register(service, StandardWatchEventKinds.ENTRY_MODIFY);
			folderMap = new HashMap<>();
			this.folders.put(parentPath, folderMap);
		}

		folderMap.put(parent.relativize(path).toString(), entry);
	}

	@Override
	public void start() {
		super.start();
		logger.info("Listener started!");
	}

	@Override
	public void run() {
		while(true) {
			try {
				poll();
			} catch(InterruptedException e) {
				logger.info("Interrupted, exiting");
				break;
			} catch(Exception e) {
				logger.error("Exception caught", e);
			}
		}

		try {
			service.close();
			logger.info("WatchService closed");
		} catch(IOException e) {
			logger.error("Failed to close WatchService", e);
		}
	}

	private void poll() throws IOException, InterruptedException {
		WatchKey key = this.service.take();

		Path folder = ((Path) key.watchable()).toAbsolutePath();
		Map<String, WatchEntry> map = this.folders.get(folder.toString());

		for(WatchEvent<?> event : key.pollEvents()) {
			WatchEvent.Kind<?> kind = event.kind();
			if(kind == StandardWatchEventKinds.OVERFLOW) {
				for(WatchEntry entry : map.values())
					recheckEntry(entry);
			} else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
				Path name = (Path) event.context();
				WatchEntry entry = map.get(name.toString());
				if(entry == null) continue;

				Path path = folder.resolve(name).toAbsolutePath();

				if(entry.vertexFile.equals(path)) {
					String data = Files.readString(path);
					if(entry.lastVertexContent.equals(data)) continue;
					recompile(entry, data, entry.lastFragmentContent);
				} else if(entry.fragmentFile.equals(path)) {
					String data = Files.readString(path);
					if(entry.lastFragmentContent.equals(data)) continue;
					recompile(entry, entry.lastVertexContent, data);
				}
			}
		}

		if(!key.reset()) {
			throw new InterruptedException();
		}
	}

	private void recheckEntry(WatchEntry entry) throws IOException {
		String vertex = Files.readString(entry.vertexFile);
		String fragment = Files.readString(entry.fragmentFile);
		if(vertex.equals(entry.lastVertexContent) && fragment.equals(entry.lastFragmentContent)) {
			return;
		}
		recompile(entry, vertex, fragment);
	}

	private void recompile(WatchEntry entry, String vertex, String fragment) {
		entry.lastVertexContent = vertex;
		entry.lastFragmentContent = fragment;
		gdx.postRunnable(() -> {
			try {
				entry.shader.compile(vertex, fragment);
				logger.info("Shader " + entry.name + " recompiled");
			} catch(IllegalStateException e) {
				logger.error("Error recompiling shader " + entry.name + "\n" + e.getMessage());
			}
		});
	}

	private static final class WatchEntry {
		final String name;
		final Shader shader;
		final Path vertexFile;
		final Path fragmentFile;
		String lastVertexContent;
		String lastFragmentContent;

		WatchEntry(String name, Shader shader, Path vertexFile, Path fragmentFile) {
			this.name = name;
			this.shader = shader;
			this.vertexFile = vertexFile;
			this.fragmentFile = fragmentFile;
		}
	}

	public static ShaderReloader create(CelestialGdx gdx) {
		return new ShaderReloader(gdx);
	}
}