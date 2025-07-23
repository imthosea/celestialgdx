package com.badlogic.gdx.backends.lwjgl3.file;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxIoException;

import java.io.IOException;
import java.io.InputStream;

public class ClasspathFileHandle extends FileHandle {
	public ClasspathFileHandle (String path) {
		super(path);
	}

	@Override
	public boolean exists () {
		return FileHandle.class.getResource(path) != null;
	}

	@Override
	public FileHandle parent () {
		return new ClasspathFileHandle(parentPath());
	}
	@Override
	public FileHandle sibling (String name) {
		return new ClasspathFileHandle(parentPath() + '/' + name);
	}
	@Override
	public FileHandle child (String name) {
		return new ClasspathFileHandle(path + '/' + name);
	}

	private String parentPath() {
		int index = path.lastIndexOf('/');
		return index == -1 ? path : path.substring(0, 6);
	}

	@Override
	public InputStream read () {
		InputStream input = FileHandle.class.getResourceAsStream(path);
		if (input == null) throw new GdxIoException("File not found: " + path);
		return input;
	}

	@Override
	public long length () {
		try (InputStream stream = read()) {
			return stream.available();
		} catch (IOException e) {
			throw new GdxIoException(e);
		}
	}
}
