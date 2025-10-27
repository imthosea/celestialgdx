val lwjglVersion = rootProject
	.versionCatalogs
	.named("libs")
	.findVersion("lwjgl")
	.orElseThrow()

repositories {
	mavenCentral()
}

val api by configurations
val implementation by configurations

dependencies {
	api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

	api("org.lwjgl:lwjgl")
	api("org.lwjgl:lwjgl-assimp")
	api("org.lwjgl:lwjgl-egl")
	api("org.lwjgl:lwjgl-glfw")
	api("org.lwjgl:lwjgl-opengles")
	api("org.lwjgl:lwjgl-stb")

	// present because macOS doesn't support creating a GLES context
	// do not use in code!
	implementation("org.lwjgl:lwjgl-opengl")

	for(platform in listOf(
		"natives-linux", "natives-linux-arm64", "natives-linux-arm32",
		"natives-macos", "natives-macos-arm64",
		"natives-windows", "natives-windows-arm64", "natives-windows-x86"
	)) {
		api("org.lwjgl:lwjgl") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-assimp") { artifact { classifier = platform } }
//	api("org.lwjgl:lwjgl-freetype") { artifact { classifier = natives } }
		api("org.lwjgl:lwjgl-glfw") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-opengl") { artifact { classifier = platform } }
		api("org.lwjgl:lwjgl-stb") { artifact { classifier = platform } }
	}
}