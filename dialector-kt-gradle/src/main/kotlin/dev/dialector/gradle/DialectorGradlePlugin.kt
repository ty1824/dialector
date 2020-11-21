package dev.dialector.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*

class DialectorGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        TODO("Not yet implemented")
    }
}

class DialectorCompilerSupportPlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        println("ExampleSubplugin loaded")
        return kotlinCompilation.target.project.provider {
            listOf(SubpluginOption("exampleKey", "exampleValue"))
        }
    }

    override fun getCompilerPluginId(): String = "dialector.modelgen"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("dev.dialector", "kotlin-compiler-plugin", "0.0.1")
}