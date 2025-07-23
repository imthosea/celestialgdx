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

package com.badlogic.gdx.backends.lwjgl3.file;

import com.badlogic.gdx.files.MappableFile;
import com.badlogic.gdx.files.WriteableFileHandle;
import com.badlogic.gdx.utils.GdxIoException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public final class SystemFileHandle extends WriteableFileHandle implements MappableFile {
	public final Path path;

	public SystemFileHandle (Path path) {
		super(path.toAbsolutePath().toString().replace('\\', '/'), path.getFileName().toString());
		this.path = path;
	}

	public SystemFileHandle (String path) {
		this(Path.of(path));
	}

	@Override
	public boolean exists () {
		return Files.exists(path);
	}
	@Override
	public SystemFileHandle parent () {
		return new SystemFileHandle(path.getParent());
	}
	@Override
	public SystemFileHandle sibling (String name) {
		return new SystemFileHandle(path.resolveSibling(name));
	}
	@Override
	public SystemFileHandle child (String name) {
		return new SystemFileHandle(path.resolve(name));
	}

	@Override
	public InputStream read () throws GdxIoException {
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			throw new GdxIoException(e);
		}
	}

	@Override
	public long length () throws GdxIoException {
		try {
			return Files.size(path);
		} catch (IOException e) {
			throw new GdxIoException(e);
		}
	}

	@Override
	public String readString (String charset) {
		try {
			return Files.readString(path);
		} catch (IOException e) {
			throw new GdxIoException(e);
		}
	}

	@Override
	public byte[] readBytes () throws IOException {
		return Files.readAllBytes(path);
	}

	@Override
	public OutputStream write (boolean append) {
		try {
			Files.createDirectories(path.getParent());
			if (append) {
				return Files.newOutputStream(path, StandardOpenOption.APPEND);
			} else {
				return Files.newOutputStream(path);
			}
		} catch (IOException e) {
			throw new GdxIoException(e);
		}
	}

	@Override
	public ByteBuffer map () {
		try(var channel = (FileChannel) Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ))) {
			ByteBuffer map = channel.map(MapMode.READ_ONLY, 0, channel.size());
			map.order(ByteOrder.nativeOrder());
			return map;
		} catch (Exception ex) {
			throw new GdxIoException("Error memory mapping file: " + path.toAbsolutePath(), ex);
		}
	}
}
