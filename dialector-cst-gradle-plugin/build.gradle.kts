plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.maven.publish)
    alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
    implementation(project(":dialector-cst"))
    implementation(project(":dialector-cst-cli"))
    compileOnly(kotlin("gradle-plugin"))

    testImplementation(libs.kotlin.test)
    testImplementation(gradleTestKit())
    testImplementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    website = "https://dialector.dev"
    vcsUrl = "https://github.com/ty1824/dialector"
    plugins {
        create("dialectorCst") {
            id = "dev.dialector.cst"
            implementationClass = "dev.dialector.cst.gradle.DialectorCstPlugin"
            displayName = "Dialector CST Gradle Plugin"
            description = "Compiles Dialector .cst schema files into high-performance Flat CST Kotlin source files."
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
        description.set("Gradle plugin for compiling Dialector .cst schema files into Flat CST Kotlin code.")
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
