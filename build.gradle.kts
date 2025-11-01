import java.time.LocalDateTime

plugins {
    base
    idea
    kotlin("jvm") version(libs.versions.kotlinVersion) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.maven.publish) apply false
}

/**
 * Provides a semi-readable qualifier for local publications
 */
fun getVersionTimestamp(): String = with(LocalDateTime.now()) {
    year.toString() +
            monthValue.toString().padStart(2, '0') +
            dayOfMonth.toString().padStart(2, '0') +
            hour.toString().padStart(2, '0') +
            minute.toString().padStart(2, '0') +
            second.toString().padStart(2, '0')
}

if (version.toString().isBlank() || version.toString() == "unspecified") {
    // If the version hasn't been specified, set it to a timestamped default
    version = "LOCAL-${getVersionTimestamp()}"
}

allprojects {
    if (project == rootProject) println("Using version for build: $version")
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

subprojects {
    // configure Kotlin to allow these opt-in features throughout the project
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.time.ExperimentalTime",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-Xcontext-receivers"
            )
        }
    }
}

dependencies {
    kover(project(":dialector-kt"))
    kover(project(":inkt"))
}

kover {
    reports {
        filters {
            excludes {
                packages("dev.dialector.inkt.example")
            }
        }
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
    }
}
