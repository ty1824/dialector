rootProject.name = "dialector"

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            } else {
                when (requested.id.id) {
                    "com.google.devtools.ksp" ->
                        useVersion(kspVersion)
                }
            }
        }
    }

    repositories {
        gradlePluginPortal()
        google()
    }
}

include("dialector-kt")
include("dialector-lsp")
include("dialector-kt-processor")
include("glottony")
include("glottony:server")
include("glottony:client")
