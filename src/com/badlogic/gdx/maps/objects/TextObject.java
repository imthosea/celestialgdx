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

package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

/** @brief Represents a text map object */
// https://doc.mapeditor.org/en/stable/reference/tmx-map-format/#tmx-object
public class TextObject extends MapObject {
	public String text = "";
	public String fontFamily = "sans-serif";
	public int pixelSize = 16;
	public boolean wrap = false;
	public Color color;
	public boolean bold = false;
	public boolean italic = false;
	public boolean underline = false;
	public boolean strikeout = false;
	public boolean kerning = true;

	public enum TextHAlign {
		LEFT,
		CENTER,
		RIGHT,
		JUSTIFY;

		public static TextHAlign of(String name) {
			return switch(name) {
				case "left" -> LEFT;
				case "center" -> CENTER;
				case "right" -> RIGHT;
				case "justify" -> JUSTIFY;
				default -> throw new IllegalStateException("Unexpected value: " + name);
			};
		}
	}

	public TextHAlign horizontalAlign = TextHAlign.LEFT;

	public enum TextVAlign {
		TOP,
		CENTER,
		BOTTOM;

		public static TextVAlign of(String name) {
			return switch(name) {
				case "top" -> TOP;
				case "center" -> CENTER;
				case "bottom" -> BOTTOM;
				default -> throw new IllegalStateException("Unexpected value: " + name);
			};
		}
	}

	public TextVAlign verticalAlign = TextVAlign.TOP;

	private final Rectangle rectangle;

	/**
	 * Creates a TextObject, represents a text map object
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param width width of the object bounds
	 * @param height height of the object bounds
	 * @param text a String representing the text for display
	 */
	public TextObject(
			ObjectProfile profile,
			float x, float y, float width, float height
	) {
		super(profile);
		this.rectangle = new Rectangle(x, y, width, height);
	}

	/** @return rectangle representing object bounds */
	public Rectangle getRectangle() {
		return rectangle;
	}

	/** @return object's X coordinate */
	public float getX() {
		return rectangle.getX();
	}

	/** @return object's Y coordinate */
	public float getY() {
		return rectangle.getY();
	}

	/** @return object's bounds height */
	public float getWidth() {
		return rectangle.getWidth();
	}

	/** @return object's bounds height */
	public float getHeight() {
		return rectangle.getHeight();
	}
}