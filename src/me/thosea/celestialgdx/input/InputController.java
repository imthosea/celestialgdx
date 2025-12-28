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

package me.thosea.celestialgdx.input;

import me.thosea.celestialgdx.cursor.CursorState;
import me.thosea.celestialgdx.utils.Disposable;
import me.thosea.celestialgdx.window.Window;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class InputController implements Disposable {
	private final Window window;

	private final GLFWKeyCallback keyCallback;
	private final GLFWCharCallback charCallback;
	private final GLFWScrollCallback scrollCallback;
	private final GLFWCursorPosCallback posCallback;
	private final GLFWMouseButtonCallback mouseButtonCallback;

	private InputHandler handler = new InputAdapter() {};

	private boolean disposed = false;

	public InputController(Window window) {
		this.window = window;

		long handle = window.getHandle();
		this.keyCallback = GLFWKeyCallback.create((i_, key, scancode, action, mods) -> {
			handler.onKeyEvent(key, scancode, action, mods);
		});
		this.charCallback = GLFWCharCallback.create((i_, codepoint) -> {
			handler.onCharEntered(codepoint);
		});
		this.scrollCallback = GLFWScrollCallback.create((i_, xOff, yOff) -> {
			handler.onScroll(xOff, yOff);
		});
		this.posCallback = GLFWCursorPosCallback.create((i_, xPos, yPos) -> {
			handler.onMouseMove(xPos, yPos);
		});
		this.mouseButtonCallback = GLFWMouseButtonCallback.create((i_, button, action, mods) -> {
			handler.onMouseClick(button, action, mods);
		});

		glfwSetKeyCallback(handle, keyCallback);
		glfwSetCharCallback(handle, charCallback);
		glfwSetScrollCallback(handle, scrollCallback);
		glfwSetCursorPosCallback(handle, posCallback);
		glfwSetMouseButtonCallback(handle, mouseButtonCallback);
	}

	public boolean isKeyPressed(int button) {
		return glfwGetKey(window.getHandle(), button) == GLFW_PRESS;
	}

	public boolean isButtonPressed(int button) {
		return glfwGetMouseButton(window.getHandle(), button) == GLFW_PRESS;
	}

	public void setCursorState(CursorState state) {
		glfwSetInputMode(window.getHandle(), GLFW_CURSOR, state.glfwState);
	}

	/**
	 * Sets the position, in screen coordinates, of the cursor relative to the upper-left corner of the content area of the specified window. The window must
	 * have input focus. If the window does not have input focus when this function is called, it fails silently.
	 */
	public void setMousePos(double x, double y) {
		glfwSetCursorPos(window.getHandle(), x, y);
	}

	public InputHandler getInputHandler() {
		this.requireNotDisposed();
		return this.handler;
	}
	public void setInputHandler(InputHandler handler) {
		this.requireNotDisposed();
		if(handler == null) {
			handler = new InputAdapter() {};
		}
		this.handler = handler;
	}

	// TODO celestialgdx these will be removed VERY SOON and are just here so scene2d works for now

	@Deprecated(forRemoval = true)
	public record MousePos(double x, double y) {}

	@Deprecated(forRemoval = true)
	public MousePos getMousePos() {
		this.requireNotDisposed();
		try(MemoryStack stack = MemoryStack.stackPush()) {
			DoubleBuffer x = stack.mallocDouble(1);
			DoubleBuffer y = stack.mallocDouble(1);
			glfwGetCursorPos(window.getHandle(), x, y);
			return new MousePos(x.get(), y.get());
		}
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		keyCallback.free();
		charCallback.free();
		scrollCallback.free();
		posCallback.free();
		mouseButtonCallback.free();
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}
}