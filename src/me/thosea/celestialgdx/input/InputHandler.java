package me.thosea.celestialgdx.input;

import org.lwjgl.glfw.GLFW;

public interface InputHandler {
	/**
	 * Will be called when a key is pressed, repeated or released.
	 * @param key the keyboard key that was pressed or released
	 * @param scancode the platform-specific scancode of the key
	 * @param action the key action. One of:<br><table><tr><td>{@link GLFW#GLFW_PRESS PRESS}</td><td>{@link GLFW#GLFW_RELEASE RELEASE}</td><td>{@link GLFW#GLFW_REPEAT REPEAT}</td></tr></table>
	 * @param mods bitfield describing which modifiers keys were held down
	 */
	void onKeyEvent(int key, int scancode, int action, int mods);

	/**
	 * Will be called when a Unicode character is input.
	 * @param codepoint the Unicode code point of the character
	 */
	void onCharEntered(int codepoint);

	/**
	 * Will be called when a scrolling device is used, such as a mouse wheel or scrolling area of a touchpad.
	 * @param xOffset the scroll offset along the x-axis
	 * @param yOffset the scroll offset along the y-axis
	 */
	void onScroll(double xOffset, double yOffset);

	/**
	 * Will be called when the cursor is moved.
	 * <p>The callback function receives the cursor position, measured in screen coordinates but relative to the top-left corner of the window client area. On
	 * platforms that provide it, the full sub-pixel cursor position is passed on.</p>
	 * @param xPos the new cursor x-coordinate, relative to the left edge of the content area
	 * @param yPos the new cursor y-coordinate, relative to the top edge of the content area
	 */
	void onMouseMove(double xPos, double yPos);

	/**
	 * Will be called when a mouse button is pressed or released.
	 * @param button the mouse button that was pressed or released
	 * @param action the button action. One of:<br><table><tr><td>{@link GLFW#GLFW_PRESS PRESS}</td><td>{@link GLFW#GLFW_RELEASE RELEASE}</td></tr></table>
	 * @param mods bitfield describing which modifiers keys were held down
	 */
	void onMouseClick(int button, int action, int mods);
}