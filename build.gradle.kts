plugins {
	`java-library`
	`maven-publish`
	id("buildSrc-gdx-setup")
	id("buildSrc-lwjgl")
}

dependencies {
	api(libs.jetbrains.annotations)
	api(libs.joml)
}