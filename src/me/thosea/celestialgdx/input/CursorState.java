package me.thosea.celestialgdx.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * GLFW Cursor states
 */
public enum CursorState {
	/**
	 * The cursor is visible and behaves normally
	 */
	NORMAL(GLFW_CURSOR_NORMAL),
	/**
	 * The cursor is invisible over the window but can still leave the window area
	 */
	HIDDEN(GLFW_CURSOR_HIDDEN),
	/**
	 * The cursor is hidden and grabbed
	 */
	DISABLED(GLFW_CURSOR_DISABLED),
	/**
	 * The cursor is visible and confined to the window's area
	 */
	CAPTURED(GLFW_CURSOR_CAPTURED);

	public final int glfwState;

	CursorState(int glfwState) {
		this.glfwState = glfwState;
	}
}