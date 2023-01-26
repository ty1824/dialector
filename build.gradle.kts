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
    // If the version hasn't been specified
    if (version.toString().isNullOrBlank()) {
        version = "LOCAL-${getVersionTimestamp()}"
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
    }
}
