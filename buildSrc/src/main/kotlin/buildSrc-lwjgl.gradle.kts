val lwjglVersion = rootProject
	.versionCatalogs
	.named("libs")
	.findVersion("lwjgl")
	.orElseThrow()

val name = System.getProperty("os.name")
val arch = System.getProperty("os.arch")
val lwjglNatives = when {
	arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
		if(arrayOf("arm", "aarch64").any { arch.startsWith(it) })
			"natives-linux${if(arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
		else if(arch.startsWith("ppc"))
			"natives-linux-ppc64le"
		else if(arch.startsWith("riscv"))
			"natives-linux-riscv64"
		else
			"natives-linux"

	arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
		"natives-macos${if(arch.startsWith("aarch64")) "-arm64" else ""}"

	arrayOf("Windows").any { name.startsWith(it) } ->
		if(arch.contains("64"))
			"natives-windows${if(arch.startsWith("aarch64")) "-arm64" else ""}"
		else
			"natives-windows-x86"

	else ->
		throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
}

repositories {
	mavenCentral()
}

val api by configurations

dependencies {
	api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

	api("org.lwjgl:lwjgl")
	api("org.lwjgl:lwjgl-assimp")
	api("org.lwjgl:lwjgl-egl")
	api("org.lwjgl:lwjgl-glfw")
	api("org.lwjgl:lwjgl-jemalloc")
	api("org.lwjgl:lwjgl-opengles")
	api("org.lwjgl:lwjgl-stb")
	api("org.lwjgl:lwjgl") { artifact { classifier = lwjglNatives } }
	api("org.lwjgl:lwjgl-assimp") { artifact { classifier = lwjglNatives } }
//	api("org.lwjgl:lwjgl-freetype") { artifact { classifier = lwjglNatives } }
	api("org.lwjgl:lwjgl-glfw") { artifact { classifier = lwjglNatives } }
	api("org.lwjgl:lwjgl-jemalloc") { artifact { classifier = lwjglNatives } }
	api("org.lwjgl:lwjgl-opengles") { artifact { classifier = lwjglNatives } }
	api("org.lwjgl:lwjgl-stb") { artifact { classifier = lwjglNatives } }
}