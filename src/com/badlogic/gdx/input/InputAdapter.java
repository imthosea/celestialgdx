package com.badlogic.gdx.input;

public abstract class InputAdapter implements InputHandler {
	@Override public void onKeyEvent(int key, int scancode, int action, int mods) {}
	@Override public void onCharEntered(int codepoint) {}
	@Override public void onScroll(double xOffset, double yOffset) {}
	@Override public void onMouseMove(double xPos, double yPos) {}
	@Override public void onMouseClick(int button, int action, int mods) {}
}