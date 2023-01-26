import java.net.URI

plugins {
    idea
    kotlin("jvm") apply false
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
}

subprojects {
    group = "dev.dialector"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
    }
}
