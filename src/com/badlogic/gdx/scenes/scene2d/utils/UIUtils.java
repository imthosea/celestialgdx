package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import org.lwjgl.system.Platform;

import static org.lwjgl.glfw.GLFW.*;

// TODO celestialgdx wtf is this
public final class UIUtils {
	private UIUtils() {
	}

	static public boolean isAndroid = false; // TODO remove
	static public boolean isMac = Platform.get() == Platform.MACOSX;
	static public boolean isWindows = Platform.get() == Platform.WINDOWS;
	static public boolean isLinux = Platform.get() == Platform.LINUX;
	static public boolean isIos = false; // TODO remove

	static public boolean left() {
		return Gdx.input.isButtonPressed(GLFW_MOUSE_BUTTON_1);
	}

	static public boolean right() {
		return Gdx.input.isButtonPressed(GLFW_MOUSE_BUTTON_2);
	}

	static public boolean shift() {
		return Gdx.input.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || Gdx.input.isKeyPressed(GLFW_KEY_RIGHT_SHIFT);
	}

	static public boolean ctrl() {
		if(isMac)
			return Gdx.input.isKeyPressed(GLFW_KEY_LEFT_SUPER) || Gdx.input.isKeyPressed(GLFW_KEY_RIGHT_SUPER);
		else
			return Gdx.input.isKeyPressed(GLFW_KEY_LEFT_CONTROL) || Gdx.input.isKeyPressed(GLFW_KEY_RIGHT);
	}
}