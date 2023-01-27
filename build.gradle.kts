import java.time.LocalDateTime
import java.net.URI

plugins {
    idea
    kotlin("jvm") apply false
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
}

/**
 * Provides a semi-readable qualifier for local publications
 */
fun getVersionTimestamp(): String = with(LocalDateTime.now()) {
    year.toString() +
            monthValue.toString().padStart(0, '0') +
            dayOfMonth.toString().padStart(0, '0') +
            hour.toString().padStart(0, '0') +
            minute.toString().padStart(0, '0') +
            second.toString().padStart(0, '0')
}

allprojects {
    if (version.toString().isNullOrBlank()) {
        // If the version hasn't been specified, set it to a timestamped default
        version = "LOCAL-${getVersionTimestamp()}"
    } else if (version.toString().startsWith("v")) {
        // TODO: Probably should do this before passing as a parameter
        version = version.toString().drop(1)
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}
