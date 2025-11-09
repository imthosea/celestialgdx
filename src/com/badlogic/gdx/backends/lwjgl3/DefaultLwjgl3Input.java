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

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.InputProcessor;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public class DefaultLwjgl3Input extends AbstractInput implements Lwjgl3Input {
	private final Window window;
	private InputProcessor processor;

	private int mouseX, mouseY;
	private boolean justTouched;

	private final boolean[] justPressedButtons = new boolean[5];

	private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if(action == GLFW_PRESS) {
				key = getGdxKeyCode(key);
				keyJustPressed = true;
				pressedKeys[key] = true;
				justPressedKeys[key] = true;
				processor.keyDown(key);
				char character = characterForKeyCode(key);
				if(character != 0) charCallback.invoke(window, character);
			} else if(action == GLFW_RELEASE) {
				key = getGdxKeyCode(key);
				pressedKeys[key] = false;
				processor.keyUp(key);
			} else if(action == GLFW_REPEAT) {
				processor.keyTyped((char) scancode);
			}
		}
	};

	private final GLFWCharCallback charCallback = new GLFWCharCallback() {
		@Override
		public void invoke(long window, int codepoint) {
			processor.keyTyped((char) codepoint);
		}
	};

	private final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
		@Override
		public void invoke(long window, double scrollX, double scrollY) {
			processor.scrolled(scrollX, scrollY);
		}
	};

	private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
		@Override
		public void invoke(long windowHandle, double x, double y) {
			mouseX = (int) x;
			mouseY = (int) y;

			processor.mouseMoved(mouseX, mouseY);
		}
	};

	private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			int gdxButton = toGdxButton(button);
			if(button != -1 && gdxButton == -1) return;

			if(action == GLFW_PRESS) {
				justTouched = true;
				justPressedButtons[gdxButton] = true;
				processor.touchDown(mouseX, mouseY, 0, gdxButton);
			} else {
				processor.touchUp(mouseX, mouseY, 0, gdxButton);
			}
		}

		private int toGdxButton(int button) {
			if(button == 0) return Buttons.LEFT;
			if(button == 1) return Buttons.RIGHT;
			if(button == 2) return Buttons.MIDDLE;
			if(button == 3) return Buttons.BACK;
			if(button == 4) return Buttons.FORWARD;
			return -1;
		}
	};

	public DefaultLwjgl3Input(Window window) {
		this.window = window;
		long handle = window.handle;
		glfwSetKeyCallback(handle, keyCallback);
		glfwSetCharCallback(handle, charCallback);
		glfwSetScrollCallback(handle, scrollCallback);
		glfwSetCursorPosCallback(handle, cursorPosCallback);
		glfwSetMouseButtonCallback(handle, mouseButtonCallback);
	}

	@Override
	public void prepareNext() {
		if(justTouched) {
			justTouched = false;
			Arrays.fill(justPressedButtons, false);
		}

		if(keyJustPressed) {
			keyJustPressed = false;
			Arrays.fill(justPressedKeys, false);
		}
	}

	@Override
	public int getMaxPointers() {
		return 1;
	}

	@Override
	public int getX() {
		return mouseX;
	}

	@Override
	public int getX(int pointer) {
		return pointer == 0 ? mouseX : 0;
	}

	@Override
	public int getY() {
		return mouseY;
	}

	@Override
	public boolean isButtonPressed(int button) {
		return glfwGetMouseButton(window.handle, button) == GLFW_PRESS;
	}

	@Override
	public boolean isButtonJustPressed(int button) {
		if(button < 0 || button >= justPressedButtons.length) {
			return false;
		}
		return justPressedButtons[button];
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
		this.processor = processor;
	}

	@Override
	public InputProcessor getInputProcessor() {
		return processor;
	}

	@Override
	public void setCursorCatched(boolean catched) {
		glfwSetInputMode(window.handle, GLFW_CURSOR,
				catched ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
	}

	@Override
	public boolean isCursorCatched() {
		return glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
	}

	@Override
	public void setCursorPosition(int x, int y) {
		glfwSetCursorPos(window.handle, x, y);
		cursorPosCallback.invoke(window.handle, x, y);
	}

	protected char characterForKeyCode(int key) {
		// Map certain key codes to character codes.
		return switch(key) {
			case Keys.BACKSPACE -> 8;
			case Keys.TAB -> '\t';
			case Keys.FORWARD_DEL -> 127;
			case Keys.ENTER, Keys.NUMPAD_ENTER -> '\n';
			default -> 0;
		};
	}

	public int getGdxKeyCode(int lwjglKeyCode) {
		return switch(lwjglKeyCode) {
			case GLFW_KEY_SPACE -> Keys.SPACE;
			case GLFW_KEY_APOSTROPHE -> Keys.APOSTROPHE;
			case GLFW_KEY_COMMA -> Keys.COMMA;
			case GLFW_KEY_MINUS -> Keys.MINUS;
			case GLFW_KEY_PERIOD -> Keys.PERIOD;
			case GLFW_KEY_SLASH -> Keys.SLASH;
			case GLFW_KEY_0 -> Keys.NUM_0;
			case GLFW_KEY_1 -> Keys.NUM_1;
			case GLFW_KEY_2 -> Keys.NUM_2;
			case GLFW_KEY_3 -> Keys.NUM_3;
			case GLFW_KEY_4 -> Keys.NUM_4;
			case GLFW_KEY_5 -> Keys.NUM_5;
			case GLFW_KEY_6 -> Keys.NUM_6;
			case GLFW_KEY_7 -> Keys.NUM_7;
			case GLFW_KEY_8 -> Keys.NUM_8;
			case GLFW_KEY_9 -> Keys.NUM_9;
			case GLFW_KEY_SEMICOLON -> Keys.SEMICOLON;
			case GLFW_KEY_EQUAL -> Keys.EQUALS;
			case GLFW_KEY_A -> Keys.A;
			case GLFW_KEY_B -> Keys.B;
			case GLFW_KEY_C -> Keys.C;
			case GLFW_KEY_D -> Keys.D;
			case GLFW_KEY_E -> Keys.E;
			case GLFW_KEY_F -> Keys.F;
			case GLFW_KEY_G -> Keys.G;
			case GLFW_KEY_H -> Keys.H;
			case GLFW_KEY_I -> Keys.I;
			case GLFW_KEY_J -> Keys.J;
			case GLFW_KEY_K -> Keys.K;
			case GLFW_KEY_L -> Keys.L;
			case GLFW_KEY_M -> Keys.M;
			case GLFW_KEY_N -> Keys.N;
			case GLFW_KEY_O -> Keys.O;
			case GLFW_KEY_P -> Keys.P;
			case GLFW_KEY_Q -> Keys.Q;
			case GLFW_KEY_R -> Keys.R;
			case GLFW_KEY_S -> Keys.S;
			case GLFW_KEY_T -> Keys.T;
			case GLFW_KEY_U -> Keys.U;
			case GLFW_KEY_V -> Keys.V;
			case GLFW_KEY_W -> Keys.W;
			case GLFW_KEY_X -> Keys.X;
			case GLFW_KEY_Y -> Keys.Y;
			case GLFW_KEY_Z -> Keys.Z;
			case GLFW_KEY_LEFT_BRACKET -> Keys.LEFT_BRACKET;
			case GLFW_KEY_BACKSLASH -> Keys.BACKSLASH;
			case GLFW_KEY_RIGHT_BRACKET -> Keys.RIGHT_BRACKET;
			case GLFW_KEY_GRAVE_ACCENT -> Keys.GRAVE;
			case GLFW_KEY_WORLD_1 -> Keys.WORLD_1;
			case GLFW_KEY_WORLD_2 -> Keys.WORLD_2;
			case GLFW_KEY_ESCAPE -> Keys.ESCAPE;
			case GLFW_KEY_ENTER -> Keys.ENTER;
			case GLFW_KEY_TAB -> Keys.TAB;
			case GLFW_KEY_BACKSPACE -> Keys.BACKSPACE;
			case GLFW_KEY_INSERT -> Keys.INSERT;
			case GLFW_KEY_DELETE -> Keys.FORWARD_DEL;
			case GLFW_KEY_RIGHT -> Keys.RIGHT;
			case GLFW_KEY_LEFT -> Keys.LEFT;
			case GLFW_KEY_DOWN -> Keys.DOWN;
			case GLFW_KEY_UP -> Keys.UP;
			case GLFW_KEY_PAGE_UP -> Keys.PAGE_UP;
			case GLFW_KEY_PAGE_DOWN -> Keys.PAGE_DOWN;
			case GLFW_KEY_HOME -> Keys.HOME;
			case GLFW_KEY_END -> Keys.END;
			case GLFW_KEY_CAPS_LOCK -> Keys.CAPS_LOCK;
			case GLFW_KEY_SCROLL_LOCK -> Keys.SCROLL_LOCK;
			case GLFW_KEY_PRINT_SCREEN -> Keys.PRINT_SCREEN;
			case GLFW_KEY_PAUSE -> Keys.PAUSE;
			case GLFW_KEY_F1 -> Keys.F1;
			case GLFW_KEY_F2 -> Keys.F2;
			case GLFW_KEY_F3 -> Keys.F3;
			case GLFW_KEY_F4 -> Keys.F4;
			case GLFW_KEY_F5 -> Keys.F5;
			case GLFW_KEY_F6 -> Keys.F6;
			case GLFW_KEY_F7 -> Keys.F7;
			case GLFW_KEY_F8 -> Keys.F8;
			case GLFW_KEY_F9 -> Keys.F9;
			case GLFW_KEY_F10 -> Keys.F10;
			case GLFW_KEY_F11 -> Keys.F11;
			case GLFW_KEY_F12 -> Keys.F12;
			case GLFW_KEY_F13 -> Keys.F13;
			case GLFW_KEY_F14 -> Keys.F14;
			case GLFW_KEY_F15 -> Keys.F15;
			case GLFW_KEY_F16 -> Keys.F16;
			case GLFW_KEY_F17 -> Keys.F17;
			case GLFW_KEY_F18 -> Keys.F18;
			case GLFW_KEY_F19 -> Keys.F19;
			case GLFW_KEY_F20 -> Keys.F20;
			case GLFW_KEY_F21 -> Keys.F21;
			case GLFW_KEY_F22 -> Keys.F22;
			case GLFW_KEY_F23 -> Keys.F23;
			case GLFW_KEY_F24 -> Keys.F24;
			case GLFW_KEY_NUM_LOCK -> Keys.NUM_LOCK;
			case GLFW_KEY_KP_0 -> Keys.NUMPAD_0;
			case GLFW_KEY_KP_1 -> Keys.NUMPAD_1;
			case GLFW_KEY_KP_2 -> Keys.NUMPAD_2;
			case GLFW_KEY_KP_3 -> Keys.NUMPAD_3;
			case GLFW_KEY_KP_4 -> Keys.NUMPAD_4;
			case GLFW_KEY_KP_5 -> Keys.NUMPAD_5;
			case GLFW_KEY_KP_6 -> Keys.NUMPAD_6;
			case GLFW_KEY_KP_7 -> Keys.NUMPAD_7;
			case GLFW_KEY_KP_8 -> Keys.NUMPAD_8;
			case GLFW_KEY_KP_9 -> Keys.NUMPAD_9;
			case GLFW_KEY_KP_DECIMAL -> Keys.NUMPAD_DOT;
			case GLFW_KEY_KP_DIVIDE -> Keys.NUMPAD_DIVIDE;
			case GLFW_KEY_KP_MULTIPLY -> Keys.NUMPAD_MULTIPLY;
			case GLFW_KEY_KP_SUBTRACT -> Keys.NUMPAD_SUBTRACT;
			case GLFW_KEY_KP_ADD -> Keys.NUMPAD_ADD;
			case GLFW_KEY_KP_ENTER -> Keys.NUMPAD_ENTER;
			case GLFW_KEY_KP_EQUAL -> Keys.NUMPAD_EQUALS;
			case GLFW_KEY_LEFT_SHIFT -> Keys.SHIFT_LEFT;
			case GLFW_KEY_LEFT_CONTROL -> Keys.CONTROL_LEFT;
			case GLFW_KEY_LEFT_ALT -> Keys.ALT_LEFT;
			case GLFW_KEY_LEFT_SUPER, GLFW_KEY_RIGHT_SUPER -> Keys.SYM;
			case GLFW_KEY_RIGHT_SHIFT -> Keys.SHIFT_RIGHT;
			case GLFW_KEY_RIGHT_CONTROL -> Keys.CONTROL_RIGHT;
			case GLFW_KEY_RIGHT_ALT -> Keys.ALT_RIGHT;
			case GLFW_KEY_MENU -> Keys.MENU;
			default -> Keys.UNKNOWN;
		};
	}

	@Override
	public void dispose() {
		keyCallback.free();
		charCallback.free();
		scrollCallback.free();
		cursorPosCallback.free();
		mouseButtonCallback.free();
	}

	// --------------------------------------------------------------------------
	// -------------------------- Nothing to see below this line except for stubs
	// --------------------------------------------------------------------------

	/*
	 * Kept for textratypist compat:
	 */

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {
	}
}