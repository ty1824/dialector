rootProject.name = "dialector"

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val koverVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.devtools.ksp" -> useVersion(kspVersion)
                "org.jetbrains.kotlinx.kover" -> useVersion(koverVersion)
            }
            if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
                useVersion(kotlinVersion)
            }
        }
    }

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

include("dialector-kt")
include("dialector-lsp")
include("dialector-kt-processor")
include("glottony")
include("glottony:server")
include("glottony:client")
