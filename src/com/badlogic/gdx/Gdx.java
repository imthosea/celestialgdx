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

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL30;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.input.InputController;

/**
 * Environment class holding references to the {@link Application}, {@link Graphics}, {@link Audio}, {@link Files} and
 * {@link Input} instances. The references are held in public static fields which allows static access to all sub systems. Do not
 * use Graphics in a thread that is not the rendering thread.
 * <p>
 * This is normally a design faux pas but in this case is better than the alternatives.
 * @author mzechner
 * @deprecated celestialgdx - mutable static constants aren't good for maintainability, kept for compat, will be removed
 */
@Deprecated(forRemoval = true)
public class Gdx {
	public static Application app;
	public static Graphics graphics;
	public static InputController input;
	public static final Files files = new Lwjgl3Files();

	public static final GL20 gl;
	public static final GL20 gl20;
	public static final GL30 gl30;

	static {
		gl = gl20 = gl30 = new Lwjgl3GL30();
	}
}