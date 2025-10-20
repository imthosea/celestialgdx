package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.ApplicationListener;

/**
 * CelestialGDX - called when initialing lwjgl3application.
 * construct your application and initialize your constants with the parameters
 */
@FunctionalInterface
public interface ApplicationCreator {
	ApplicationListener create(Lwjgl3Application app, Lwjgl3Window window);
}