plugins {
	`java-library`
	`maven-publish`
}

version = rootProject.version

repositories {
	mavenCentral()
}

java {
	withSourcesJar()
	withJavadocJar()
	toolchain.languageVersion = JavaLanguageVersion.of(prop("java_version"))
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

publishing {
	repositories {
		maven {
			name = "teamCelestialPublic"
			url = uri("https://maven.teamcelestial.org/public")
			credentials(PasswordCredentials::class)
		}
	}

	publications.create<MavenPublication>("maven") {
		from(components["java"])
		pom {
			name = prop("pom_name")
			artifactId = prop("pom_artifact")
			url = prop("pom_url")
			licenses {
				license {
					name = prop("pom_license_name")
					distribution = "repo"
				}
			}
		}
	}
}

private fun prop(key: String) = project.property(key)!!.toString()