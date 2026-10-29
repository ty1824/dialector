plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.maven.publish)
    alias(libs.plugins.gradle.plugin.publish)
}

val cliTestClasspath by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(project(":dialector-cst-cli"))

    cliTestClasspath(project(":dialector-cst-cli"))

    testImplementation(project(":dialector-cst-cli"))
    testImplementation(libs.kotlin.test)
    testImplementation(gradleTestKit())
    testImplementation(kotlin("gradle-plugin"))
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(cliTestClasspath)
}

gradlePlugin {
    website = "https://dialector.dev"
    vcsUrl = "https://github.com/ty1824/dialector"
    plugins {
        create("dialectorCst") {
            id = "dev.dialector.cst"
            implementationClass = "dev.dialector.cst.gradle.DialectorCstPlugin"
            displayName = "Dialector CST Gradle Plugin"
            description = "Compiles Dialector .cst schema files into Kotlin source files."
            tags = setOf("dialector", "syntax", "generator", "kotlin")
        }
    }
}

val javaLanguageVersion: String by project
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (!System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey").isNullOrBlank()) {
        signAllPublications()
    }
    pom {
        name.set("dialector-cst-gradle-plugin")
        description.set("Gradle plugin for compiling Dialector .cst schema files into CST Kotlin code.")
        url.set("http://dialector.dev")
        licenses {
            license {
                name.set("GPL-3.0")
                url.set("https://opensource.org/licenses/GPL-3.0")
            }
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/ty1824/dialector/issues")
        }
        scm {
            connection.set("https://github.com/ty1824/dialector.git")
            url.set("https://github.com/ty1824/dialector")
        }
        developers {
            developer {
                name.set("Tyler Hodgkins")
                email.set("ty1824@gmail.com")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/ty1824/dialector")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
