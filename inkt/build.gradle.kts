plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
    signing
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
}

kotlin {
    explicitApiWarning()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    filters {
        classes {
            excludes += listOf(
                "dev.dialector.inkt.example.*"
            )
        }
    }

    xmlReport {
        onCheck.set(true)
    }
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/ty1824/dialector")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    repositories.forEach { println((it as MavenArtifactRepository).url)}
    publications {
        register<MavenPublication>("default") {
            from(components["java"])
            pom {
                name.set("inkt")
                description.set("Incremental computation framework for Kotlin")
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
    }
}

signing {
    val gpgPrivateKey = System.getenv("GPG_SIGNING_KEY")
    if (!gpgPrivateKey.isNullOrBlank()) {
        useInMemoryPgpKeys(
            gpgPrivateKey,
            System.getenv("GPG_SIGNING_PASSPHRASE")
        )
        sign(publishing.publications)
    }
}
