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

package com.badlogic.gdx.backends.lwjgl3.angle;

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.CRC32;

public class ANGLELoader {
	public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
	public static final boolean isLinux = System.getProperty("os.name").contains("Linux")
			|| System.getProperty("os.name").contains("FreeBSD");
	public static final boolean isMac = System.getProperty("os.name").contains("Mac");
	public static final boolean isARM = System.getProperty("os.arch").startsWith("arm")
			|| System.getProperty("os.arch").startsWith("aarch64");
	public static final boolean is64Bit = System.getProperty("os.arch").contains("64")
			|| System.getProperty("os.arch").startsWith("armv8");

	private static File egl;
	private static File gles;
	private static File lastWorkingDir;

	public static String crc (InputStream input) {
		if (input == null) throw new IllegalArgumentException("input cannot be null.");
		CRC32 crc = new CRC32();
		byte[] buffer = new byte[4096];
		try {
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				crc.update(buffer, 0, length);
			}
		} catch (Exception ignored) {
		}
		return Long.toString(crc.getValue(), 16);
	}

	private static File extractFile (String sourcePath, File outFile) {
		try {
			if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) throw new GdxRuntimeException(
					"Couldn't create ANGLE native library output directory " + outFile.getParentFile().getAbsolutePath());
			if (outFile.exists()) {
				return outFile;
			}

			try (
					var out = new FileOutputStream(outFile);
					var in = ANGLELoader.class.getResourceAsStream("/" + sourcePath)
			) {
				byte[] buffer = new byte[4096];
				while (true) {
					int length = in.read(buffer);
					if (length == -1) return outFile;
					out.write(buffer, 0, length);
				}
			}
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load ANGLE shared library " + sourcePath, t);
		}
	}

	/**
	 * Returns a path to a file that can be written. Tries multiple locations and verifies writing succeeds.
	 * @return null if a writable path could not be found.
	 */
	private static File getExtractedFile (String dirName, String fileName) {
		// Temp directory with username in path.
		File idealFile = new File(
				System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/" + dirName, fileName);
		if (canWrite(idealFile)) return idealFile;

		// System provided temp directory.
		try {
			File file = File.createTempFile(dirName, null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (canWrite(file)) return file;
			}
		} catch (IOException ignored) {
		}

		// User home.
		File file = new File(System.getProperty("user.home") + "/.libgdx/" + dirName, fileName);
		if (canWrite(file)) return file;

		// Relative directory.
		file = new File(".temp/" + dirName, fileName);
		if (canWrite(file)) return file;

		// We are running in the OS X sandbox.
		if (System.getenv("APP_SANDBOX_CONTAINER_ID") != null) return idealFile;

		return null;
	}

	/**
	 * Returns true if the parent directories of the file can be created and the file can be written.
	 */
	private static boolean canWrite (File file) {
		File parent = file.getParentFile();
		File testFile;
		if (file.exists()) {
			if (!file.canWrite() || !canExecute(file)) return false;
			// Don't overwrite existing file just to check if we can write to directory.
			testFile = new File(parent, UUID.randomUUID().toString());
		} else {
			parent.mkdirs();
			if (!parent.isDirectory()) return false;
			testFile = file;
		}
		try {
			new FileOutputStream(testFile).close();
			if (!canExecute(testFile)) return false;
			return true;
		} catch (Throwable ex) {
			return false;
		} finally {
			testFile.delete();
		}
	}

	private static boolean canExecute (File file) {
		if (file.canExecute()) return true;
		return file.setExecutable(true, false);
	}

	public static void load() {
		String osDir = null;
		String ext = null;
		if (isWindows) {
			osDir = is64Bit ? "windows64" : "windows32";
			ext = ".dll";
		}
		if (isLinux) {
			osDir = "linux64";
			ext = ".so";
		}
		if(isMac) {
			osDir = isARM ? "macosxarm64" : "macosx64";
			ext = ".dylib";
		}

		String eglSource = osDir + "/libEGL" + ext;
		String glesSource = osDir + "/libGLESv2" + ext;
		String crc = "";
		try (
				var egl = ANGLELoader.class.getResourceAsStream("/" + eglSource);
				var gles = ANGLELoader.class.getResourceAsStream("/" + glesSource)
		) {
			crc = crc(egl) + crc(gles);
		} catch (Exception ignored) {}
		egl = getExtractedFile(crc, new File(eglSource).getName());
		gles = getExtractedFile(crc, new File(glesSource).getName());

		if (!isMac) {
			extractFile(eglSource, egl);
			System.load(egl.getAbsolutePath());
			extractFile(glesSource, gles);
			System.load(gles.getAbsolutePath());
		} else {
			// On macOS, we can't preload the shared libraries. calling dlopen("path1/lib.dylib")
			// then calling dlopen("lib.dylib") will not return the dylib loaded in the first dlopen()
			// call, but instead perform the dlopen library search algorithm anew. Since the dylibs
			// we extract are not in any paths dlopen knows about, GLFW fails to load them.
			// Instead, we need to copy the shared libraries to the current working directory (which
			// we can't temporarily change in pure Java either...). The dylibs will get deleted
			// in postGlfwInit() once the first window has been created, and GLFW has loaded the dylibs.
			lastWorkingDir = new File(".");
			extractFile(eglSource, new File(lastWorkingDir, egl.getName()));
			extractFile(glesSource, new File(lastWorkingDir, gles.getName()));
		}
	}

	public static void postGlfwInit() {
		new File(lastWorkingDir, egl.getName()).delete();
		new File(lastWorkingDir, gles.getName()).delete();
	}
}
