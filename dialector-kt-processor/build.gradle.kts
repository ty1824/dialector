import java.nio.file.Paths

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.gradle.maven.publish)
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
    implementation(libs.ktlint.rule.engine)
    implementation(libs.ktlint.ruleset.standard)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
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

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask> {
    exclude { it.file.toPath().contains(Paths.get("build")) }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (!System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey").isNullOrBlank()) {
        signAllPublications()
    }
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
