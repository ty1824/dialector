import java.time.LocalDateTime

plugins {
    base
    idea
    kotlin("jvm") apply false
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("org.jmailen.kotlinter")
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

allprojects {
    if (version.toString().isBlank() || version.toString() == "unspecified") {
        // If the version hasn't been specified, set it to a timestamped default
        version = "LOCAL-${getVersionTimestamp()}"
    } else if (version.toString().startsWith("v")) {
        // TODO: Probably should do this before passing as a parameter
        version = version.toString().drop(1)
    }
    if (project == rootProject) println("Using version for build: $version")

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jmailen.kotlinter")

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

koverReport {
    filters {
        excludes {
            packages("dev.dialector.inkt.example")
        }
    }

    defaults {
        xml {
            onCheck = true
        }
        html {
            onCheck = true
        }
    }
}
