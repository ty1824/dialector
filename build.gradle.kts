import java.net.URI

plugins {
    idea
    kotlin("jvm") apply false
    id("maven-publish")
}

allprojects {
    group = "dev.dialector"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
    }
}
