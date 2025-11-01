plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.maven.publish)
}

dependencies {
    implementation(libs.kotlin.reflect)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

val javaLanguageVersion: String by project
kotlin {
    explicitApiWarning()
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
        name.set("dialector-kt")
        description.set("Dialector language workbench core library.")
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
