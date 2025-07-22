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

package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.util.EnumMap;
import java.util.Map;

public class Lwjgl3Cursor implements Cursor {
	private static final Array<Lwjgl3Cursor> cursors = new Array<>();
	private static final Map<SystemCursor, Long> systemCursors = new EnumMap<>(SystemCursor.class);

	private static int inputModeBeforeNoneCursor = -1;

	final Lwjgl3Window window;
	private Pixmap pixmapCopy;
	private final GLFWImage glfwImage;
	public final long glfwCursor;

	Lwjgl3Cursor (Lwjgl3Window window, Pixmap pixmap, int xHotspot, int yHotspot) {
		this.window = window;
		if (pixmap.getFormat() != Format.RGBA8888) {
			throw new GdxRuntimeException("Cursor image pixmap is not in RGBA8888 format.");
		}

		if ((pixmap.getWidth() & (pixmap.getWidth() - 1)) != 0) {
			throw new GdxRuntimeException(
				"Cursor image pixmap width of " + pixmap.getWidth() + " is not a power-of-two greater than zero.");
		}

		if ((pixmap.getHeight() & (pixmap.getHeight() - 1)) != 0) {
			throw new GdxRuntimeException(
				"Cursor image pixmap height of " + pixmap.getHeight() + " is not a power-of-two greater than zero.");
		}

		if (xHotspot < 0 || xHotspot >= pixmap.getWidth()) {
			throw new GdxRuntimeException(
				"xHotspot coordinate of " + xHotspot + " is not within image width bounds: [0, " + pixmap.getWidth() + ").");
		}

		if (yHotspot < 0 || yHotspot >= pixmap.getHeight()) {
			throw new GdxRuntimeException(
				"yHotspot coordinate of " + yHotspot + " is not within image height bounds: [0, " + pixmap.getHeight() + ").");
		}

		this.pixmapCopy = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		this.pixmapCopy.setBlending(Blending.None);
		this.pixmapCopy.drawPixmap(pixmap, 0, 0);

		glfwImage = GLFWImage.malloc();
		glfwImage.width(pixmapCopy.getWidth());
		glfwImage.height(pixmapCopy.getHeight());
		glfwImage.pixels(pixmapCopy.getPixels());
		glfwCursor = GLFW.glfwCreateCursor(glfwImage, xHotspot, yHotspot);
		cursors.add(this);
	}

	@Override
	public void dispose () {
		if (pixmapCopy == null) {
			throw new GdxRuntimeException("Cursor already disposed");
		}
		cursors.removeValue(this, true);
		pixmapCopy.dispose();
		pixmapCopy = null;
		glfwImage.free();
		GLFW.glfwDestroyCursor(glfwCursor);
	}

	static void dispose (Lwjgl3Window window) {
		for (int i = cursors.size - 1; i >= 0; i--) {
			Lwjgl3Cursor cursor = cursors.get(i);
			if (cursor.window.equals(window)) {
				cursors.removeIndex(i).dispose();
			}
		}
	}

	static void disposeSystemCursors () {
		for (long systemCursor : systemCursors.values()) {
			GLFW.glfwDestroyCursor(systemCursor);
		}
		systemCursors.clear();
	}

	static void setSystemCursor (long windowHandle, SystemCursor systemCursor) {
		if (systemCursor == SystemCursor.None) {
			if (inputModeBeforeNoneCursor == -1) inputModeBeforeNoneCursor = GLFW.glfwGetInputMode(windowHandle, GLFW.GLFW_CURSOR);
			GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
			return;
		} else if (inputModeBeforeNoneCursor != -1) {
			GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, inputModeBeforeNoneCursor);
			inputModeBeforeNoneCursor = -1;
		}
		Long glfwCursor = systemCursors.get(systemCursor);
		if (glfwCursor == null) {
			int shape = switch (systemCursor) {
				case Arrow -> GLFW.GLFW_ARROW_CURSOR;
				case Ibeam -> GLFW.GLFW_IBEAM_CURSOR;
				case Crosshair -> GLFW.GLFW_CROSSHAIR_CURSOR;
				case Hand -> GLFW.GLFW_HAND_CURSOR;
				case HorizontalResize -> GLFW.GLFW_HRESIZE_CURSOR;
				case VerticalResize -> GLFW.GLFW_VRESIZE_CURSOR;
				case NWSEResize -> GLFW.GLFW_RESIZE_NWSE_CURSOR;
				case NESWResize -> GLFW.GLFW_RESIZE_NESW_CURSOR;
				case AllResize -> GLFW.GLFW_RESIZE_ALL_CURSOR;
				case NotAllowed -> GLFW.GLFW_NOT_ALLOWED_CURSOR;
				default -> throw new GdxRuntimeException("Unknown system cursor " + systemCursor);
			};
			glfwCursor = GLFW.glfwCreateStandardCursor(shape);
			systemCursors.put(systemCursor, glfwCursor);
		}
		GLFW.glfwSetCursor(windowHandle, glfwCursor);
	}
}
