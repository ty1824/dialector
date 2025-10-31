plugins {
    kotlin("jvm")
    alias(libs.plugins.kover)
    id("maven-publish")
    signing
}

dependencies {
    implementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.test)
}

val javaLanguageVersion: String by project
kotlin {
    explicitApiWarning()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
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
    reports {
        filters {
            excludes {
                packages("dev.dialector.inkt.example")
            }
        }
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
