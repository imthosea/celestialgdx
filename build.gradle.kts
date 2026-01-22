plugins {
	`java-library`
	`maven-publish`
	id("buildSrc-gdx-setup")
}

repositories {
	mavenCentral()
}

dependencies {
	api(libs.jetbrains.annotations)
	api(libs.joml)

	api(platform(libs.lwjgl))
	api("org.lwjgl:lwjgl")
	api("org.lwjgl:lwjgl-freetype")
	api("org.lwjgl:lwjgl-egl")
	api("org.lwjgl:lwjgl-glfw")
	api("org.lwjgl:lwjgl-opengl")
	api("org.lwjgl:lwjgl-stb")
	for(platform in listOf(
		"natives-linux", "natives-linux-arm64", "natives-linux-arm32",
		"natives-macos", "natives-macos-arm64",
		"natives-windows", "natives-windows-arm64", "natives-windows-x86"
	)) {
		api("org.lwjgl:lwjgl") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-freetype") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-glfw") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-opengl") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-stb") { artifact { classifier = platform } }
	}
}