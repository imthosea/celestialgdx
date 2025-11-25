plugins {
	`java-library`
	`maven-publish`
	id("buildSrc-gdx-setup")
	id("buildSrc-lwjgl")
}

dependencies {
	api(libs.jetbrains.annotations)
	api(libs.joml)

	// libgdx natives work fine with this since the signatures didn't change
	// TODO remove all the natives
	implementation(libs.gdx.natives) {
		artifact { classifier = "natives-desktop" }
	}
}

// TODO: enable when this engine works
tasks.javadoc { enabled = false }