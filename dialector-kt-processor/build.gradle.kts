import java.nio.file.Paths

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    // TODO: Re-enable when unit tests are added, tests right now depend on running the processor at test compile time.
    //alias(libs.plugins.kover)
    id("maven-publish")
    signing
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":dialector-kt"))
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp.symbol.processing.api)
    implementation(libs.kotlin.reflect)
    implementation(libs.ktlint.core)
    implementation(libs.ktlint.rule.engine)
    implementation(libs.ktlint.ruleset.standard)

    testImplementation(libs.kotlin.test)
    testImplementation(project(":dialector-kt"))
    kspTest(project(":dialector-kt-processor"))
}

ksp {
    arg("dev.dialector.targetPackage", "dev.dialector.processor.ast")
    arg("dev.dialector.factory", "true")
}

val javaLanguageVersion: String by project
kotlin {
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

//tasks.withType<org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask> {
//    exclude { it.file.toPath().contains(Paths.get("build")) }
//    // The following does not work on all OS - path separator is OS-dependent
////    exclude { it.file.path.contains("/build/generated/") }
////    exclude { it.file.path.contains("\\build\\generated\\") }
//}

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
                name.set("dialector-kt-processor")
                description.set("Code generation for Dialector Node interfaces using KSP.")
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
