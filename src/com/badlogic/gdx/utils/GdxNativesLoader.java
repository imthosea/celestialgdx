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

package com.badlogic.gdx.utils;

import org.lwjgl.system.Platform;
import org.lwjgl.system.Platform.Architecture;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// TODO celestialgdx: remove all the natives
public final class GdxNativesLoader {
	private GdxNativesLoader() {}

	public static void load(String name) throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		String libName = mapLibraryName(name);
		File file = File.createTempFile("gdxnatives", null);
		file.deleteOnExit();

		try(InputStream data = loader.getResourceAsStream(libName)) {
			if(data == null) {
				throw new IllegalStateException("No natives named " + libName);
			}
			Files.copy(data, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		System.load(file.getAbsolutePath());
	}

	// GDX natives naming is quite inconsistent!
	private static String mapLibraryName(String name) {
		Platform platform = Platform.get();
		Architecture arch = Platform.getArchitecture();

		if(platform == Platform.WINDOWS) {
			boolean is64Bit = switch(arch) {
				case X64, ARM64 -> true;
				case X86, ARM32 -> false;
			};
			return name + (is64Bit ? "64.dll" : ".dll");
		} else if(platform == Platform.MACOSX) {
			String classifier = arch == Architecture.ARM64 ? "arm64" : "64";
			return "lib" + name + classifier + ".dylib";
		} else if(platform == Platform.LINUX) {
			// TODO RISCV
			String classifier = switch(arch) {
				case X64 -> "64";
				case ARM64 -> "arm64";
				case ARM32 -> "arm";
				default -> throw new IllegalStateException("32-bit Linux is not supported");
			};
			return "lib" + name + classifier + ".so";
		} else {
			throw new IncompatibleClassChangeError();
		}
	}
}