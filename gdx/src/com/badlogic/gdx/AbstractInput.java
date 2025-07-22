
package com.badlogic.gdx;

public abstract class AbstractInput implements Input {
	protected final boolean[] pressedKeys;
	protected final boolean[] justPressedKeys;
	protected boolean keyJustPressed;

	public AbstractInput () {
		pressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
		justPressedKeys = new boolean[Keys.MAX_KEYCODE + 1];
	}

	@Override
	public boolean isKeyPressed (int key) {
		if (key < 0 || key > Keys.MAX_KEYCODE) {
			return false;
		}
		return pressedKeys[key];
	}

	@Override
	public boolean isKeyJustPressed (int key) {
		if (key < 0 || key > Keys.MAX_KEYCODE) {
			return false;
		}
		return justPressedKeys[key];
	}
}
