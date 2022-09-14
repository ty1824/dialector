plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

val kspVersion: String by project

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

                implementation(project(":dialector-kt"))
                implementation("com.squareup:kotlinpoet:1.12.0")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}