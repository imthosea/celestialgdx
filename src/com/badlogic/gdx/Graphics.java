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

@Deprecated(forRemoval = true)
public interface Graphics {
	/** @return the width of the client area in logical pixels. */
	int getWidth();

	/** @return the height of the client area in logical pixels */
	int getHeight();

	/**
	 * @param extension the extension name
	 * @return whether the extension is supported
	 */
	boolean supportsExtension(String extension);
}