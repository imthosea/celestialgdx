import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
	`java-library`
	id("com.vanniktech.maven.publish")
}

version = rootProject.version

repositories {
	mavenCentral()
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(prop("java_version"))
}

tasks.withType<Javadoc> {
	options {
		this as CoreJavadocOptions
		addStringOption("Xdoclint:none,-missing", "-quiet")
	}
}

sourceSets.main {
	java.setSrcDirs(listOf("src"))
	resources.setSrcDirs(listOf("resources"))
}

configure<MavenPublishBaseExtension> {
	publishToMavenCentral()
	signAllPublications()
	coordinates("io.github.imthosea", prop("pom_artifact"))

	pom {
		name = prop("pom_name")
		description = prop("pom_description")
		url = "https://github.com/imthosea/celestialgdx"
		licenses {
			license {
				name = "BSD 2-clause"
				url = "https://github.com/imthosea/celestialgdx/blob/master/LICENSE"
			}
		}
		developers {
			developer {
				id = "thosea"
				name = "Thosea"
				url.set("https://github.com/imthosea/")
			}
		}
		scm {
			url = "https://github.com/imthosea/celestialgdx"
			connection = "scm:git:git://https://github.com/imthosea/celestialgdx.git"
			developerConnection = "scm:git:ssh://github.com:imthosea/celestialgdx.git"
		}
	}
}

configure<SigningExtension> {
	useGpgCmd()
}

private fun prop(key: String) = project.property(key)!!.toString()