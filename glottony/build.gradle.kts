plugins {
    idea
    java
//    kotlin("jvm") apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}


val installLocalServer by tasks.registering(Copy::class)