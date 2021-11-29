import java.net.URI

plugins {
    idea
    kotlin("jvm") apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = URI("https://jitpack.io")
        }
    }
}
