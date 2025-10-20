/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.maps.layers;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.TiledMap;

public final class ImageLayer extends MapLayer {
	private TextureRegion texture;
	private boolean repeatX;
	private boolean repeatY;
	private boolean supportsTransparency;

	public ImageLayer(
			MapLayer parent, TiledMap map,
			boolean repeatX, boolean repeatY
	) {
		super(parent, map);
		this.repeatX = repeatX;
		this.repeatY = repeatY;
	}

	/**
	 * TiledMap ImageLayers can support transparency through tint color if the image provided supports the proper pixel format.
	 * Here we check to see if the file supports transparency by checking the format of the TextureData.
	 * @param region TextureRegion of the ImageLayer
	 * @return boolean
	 */
	private boolean hasTransparencySupport(TextureRegion region) {
		Pixmap.Format format = region.getTexture().getTextureData().getFormat();
		return switch(format) {
			case Alpha:
			case LuminanceAlpha:
			case RGBA4444:
			case RGBA8888:
				yield true;
			case null:
			default:
				yield false;
		};
	}


	public TextureRegion getTexture() {
		return texture;
	}

	public void setTexture(TextureRegion texture) {
		this.texture = texture;
		this.supportsTransparency = hasTransparencySupport(this.texture);
	}

	public boolean supportsTransparency() {
		return supportsTransparency;
	}

	public boolean isRepeatX() {
		return repeatX;
	}

	public boolean isRepeatY() {
		return repeatY;
	}

	public void setRepeatX(boolean repeatX) {
		this.repeatX = repeatX;
	}

	public void setRepeatY(boolean repeatY) {
		this.repeatY = repeatY;
	}
}