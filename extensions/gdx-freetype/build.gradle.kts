plugins {
	id("buildSrc-gdx-setup")
}

dependencies {
	val api by configurations
	api(rootProject)
}