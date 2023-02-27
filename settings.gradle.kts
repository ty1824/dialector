rootProject.name = "dialector"

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val koverVersion: String by settings
    val kotlinterVersion: String by settings
    val dokkaVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("dialector-kt")
include("dialector-lsp")
include("dialector-kt-processor")
include("inkt")
