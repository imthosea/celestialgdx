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

package me.thosea.celestialgdx.files;

import com.badlogic.gdx.utils.GdxIoException;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public abstract class FileHandle {
	protected final String path;
	protected final String name;

	protected FileHandle(String path, String name) {
		this.path = path;
		this.name = name;
	}

	protected FileHandle(String path) {
		this.path = path;
		this.name = name(path);
	}

	private static String name(String path) {
		int slashIndex = path.lastIndexOf('/');
		if(slashIndex == -1 || slashIndex == path.length() - 1) return path;
		return path.substring(slashIndex);
	}

	public abstract boolean exists();
	public abstract FileHandle parent();
	public abstract FileHandle sibling(String name);
	public abstract FileHandle child(String name);

	public abstract InputStream read() throws GdxIoException;
	public abstract long length() throws GdxIoException;

	/**
	 * @return the path of the file as specified on construction, e.g. Gdx.files.internal("dir/file.png") -> dir/file.png.
	 * backward slashes will be replaced by forward slashes.
	 */
	public String path() {
		return path;
	}

	/** @return the name of the file, without any parent paths. */
	public String name() {
		return name;
	}

	/** Returns the file extension (without the dot) or an empty string if the file name doesn't contain a dot. */
	public String extension() {
		int dotIndex = name.lastIndexOf('.');
		if(dotIndex == -1) return "";
		return name.substring(dotIndex + 1);
	}

	/** @return the name of the file, without parent paths or the extension. */
	public String nameWithoutExtension() {
		int dotIndex = name.lastIndexOf('.');
		if(dotIndex == -1) return name;
		return name.substring(0, dotIndex);
	}

	/**
	 * @return the path and filename without the extension, e.g. dir/dir2/file.png -> dir/dir2/file. backward slashes will be
	 * returned as forward slashes.
	 */
	public String pathWithoutExtension() {
		int dotIndex = path.lastIndexOf('.');
		if(dotIndex == -1) return path;
		return path.substring(0, dotIndex);
	}

	public BufferedReader reader() throws IOException {
		return new BufferedReader(new InputStreamReader(read()));
	}

	public BufferedReader reader(int bufSize) throws IOException {
		return new BufferedReader(new InputStreamReader(read()), bufSize);
	}

	public BufferedReader reader(String charset) {
		try {
			return new BufferedReader(new InputStreamReader(read(), charset));
		} catch(UnsupportedEncodingException e) {
			throw new GdxIoException(e);
		}
	}

	public BufferedReader reader(String charset, int bufSize) throws IOException {
		return new BufferedReader(new InputStreamReader(read(), charset), bufSize);
	}

	/**
	 * Reads the entire file into a string using the platform's default charset.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
	 */
	public String readString() throws GdxIoException {
		return readString(null);
	}

	public String readString(String charset) throws GdxIoException {
		try(InputStream stream = read()) {
			StringBuilder output = new StringBuilder(stream.available());
			try(var reader = charset == null ? new InputStreamReader(read()) : new InputStreamReader(read(), charset)) {
				char[] buffer = new char[256];
				while(true) {
					int length = reader.read(buffer);
					if(length == -1) break;
					output.append(buffer, 0, length);
				}
			}
			return output.toString();
		} catch(IOException e) {
			throw new GdxIoException(e);
		}
	}

	/**
	 * Reads the entire file into a byte array.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
	 */
	public byte[] readBytes() throws IOException {
		try(InputStream stream = read()) {
			return stream.readAllBytes();
		}
	}
}