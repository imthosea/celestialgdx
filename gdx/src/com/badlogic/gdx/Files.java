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

package com.badlogic.gdx;

import com.badlogic.gdx.files.FileHandle;

/**
 * Provides standard access to the filesystem, classpath, Android app storage (internal and external), and Android assets
 * directory.
 * @author mzechner
 * @author Nathan Sweet
 */
public interface Files {
	public FileHandle classpath(String path);

	public FileHandle internal(String path);

	public FileHandle external(String path);

	public FileHandle absolute(String path);

	public FileHandle local(String path);

	/**
	 * Returns the external storage path directory. This is the app external storage on Android and the home directory of the
	 * current user on the desktop.
	 */
	public String getExternalStoragePath();

	/** Returns true if the external storage is ready for file IO. */
	public boolean isExternalStorageAvailable();

	/**
	 * Returns the local storage path directory. This is the private files directory on Android and the directory of the jar on
	 * the desktop.
	 */
	public String getLocalStoragePath();

	/** Returns true if the local storage is ready for file IO. */
	public boolean isLocalStorageAvailable();
}