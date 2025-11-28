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

package me.thosea.celestialgdx.maps.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.thosea.celestialgdx.maps.MapProperties;
import org.jetbrains.annotations.Nullable;

/**
 * @brief Represents a changing {@link TiledMapTile}.
 */
public record AnimatedMapTile(
		int id,
		boolean isBlended,
		@Nullable MapProperties properties,
		int animationLength, AnimationFrame[] frames
) implements TiledMapTile {
	public record AnimationFrame(int duration, TextureRegion texture) {}

	public AnimatedMapTile(TiledMapTile base, MapProperties prop, AnimationFrame[] frames) {
		this(
				base.id(), base.isBlended(),
				prop,
				animationLength(frames), frames
		);
	}

	private static int animationLength(AnimationFrame[] frames) {
		int result = 0;
		for(AnimationFrame frame : frames) result += frame.duration;
		return result;
	}

	@Override
	public boolean isStaticTexture() {
		return false;
	}

	@Override
	public TextureRegion texture(long timeMs) {
		int time = (int) (timeMs % animationLength);
		for(AnimationFrame frame : frames) {
			if(time <= frame.duration) return frame.texture;
			time -= animationLength;
		}
		throw new IllegalStateException("Animation time went outside frame length? This shouldn't happen");
	}
}