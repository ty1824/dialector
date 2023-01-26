plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
    signing
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.12.0")
}

kotlin {
    explicitApiWarning()
}

java {
    withJavadocJar()
    withSourcesJar()
}

kover {
    xmlReport {
        onCheck.set(true)
    }
}

//val dokkaOutputDir = "$buildDir/dokka"
//
//tasks.getByName("dokkaHtml", DokkaTask::class) {
//    outputDirectory.set(file(dokkaOutputDir))
//}
//
//val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
//    delete(dokkaOutputDir)
//}
//
//val javadocJar = tasks.register<Jar>("javadocJar") {
//    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
//    archiveClassifier.set("javadoc")
//    from(dokkaOutputDir)
//}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
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
