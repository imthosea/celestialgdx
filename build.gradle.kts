plugins {
	`java-library`
	`maven-publish`
	id("buildSrc-lwjgl")
}

configure(listOf(
	rootProject,
	project(":extensions:gdx-freetype")
)) {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")

	group = "me.thosea.celestialgdx"
	version = rootProject.version

	repositories {
		google()
		mavenLocal()
		mavenCentral()
		gradlePluginPortal()
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
		maven(url = "https://oss.sonatype.org/content/repositories/releases/")
		maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
	}

	java {
		withSourcesJar()
		withJavadocJar()
		toolchain.languageVersion = JavaLanguageVersion.of(21)
	}

	tasks.withType<Javadoc> {
		options {
			this as CoreJavadocOptions
			addStringOption("Xdoclint:none,-missing", "-quiet")
		}
	}

	tasks.withType<JavaCompile> {
		options.isIncremental = true
	}

	sourceSets.main {
		java.setSrcDirs(listOf("src"))
		resources.setSrcDirs(listOf("resources"))
	}

	if(this != rootProject) {
		dependencies.api(rootProject)
	}

	publishing {
		repositories {
			maven {
				name = "teamCelestialPublic"
				url = uri("https://maven.teamcelestial.org/public")
				credentials(PasswordCredentials::class)
			}
		}
		createPublish(project, publications)
	}
}

private fun createPublish(project: Project, c: PublicationContainer) = c.create<MavenPublication>("maven") {
	from(components["java"])
	pom {
		fun prop(key: String) = project.property(key)!!.toString()
		name = prop("POM_NAME")
		artifactId = prop("POM_ARTIFACT")
		url = prop("POM_URL")
		licenses {
			license {
				name = prop("POM_LICENCE_NAME")
				url = prop("POM_LICENCE_URL")
				distribution = "repo"
			}
		}
	}
}

dependencies {
	// LWJGL handled by buildSrc plugin
	api(libs.jetbrains.annotations)

	// note that we don't bundle natives here, since libgdx's natives work fine with this
	// however, the setup is still cursed, so as a TODO i want to remove all native code from GDX
}