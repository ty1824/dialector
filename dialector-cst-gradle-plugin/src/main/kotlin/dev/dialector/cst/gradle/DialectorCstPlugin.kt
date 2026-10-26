package dev.dialector.cst.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/**
 * Gradle plugin (`dev.dialector.cst`) that registers the `dialectorCst` extension,
 * creates the `generateCst` task (and test-scoped `generateTestCst` when applicable),
 * and wires generated Kotlin directories into the project's source sets.
 */
public class DialectorCstPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            DialectorCstExtension::class.java,
        )

        extension.sourceDirectories.from(project.layout.projectDirectory.dir("src/main/cst"))
        extension.outputDirectory.convention(
            project.layout.buildDirectory.dir("generated/source/cst/main/kotlin")
        )

        val generateCstTask = project.tasks.register(TASK_NAME, GenerateCstTask::class.java) { task ->
            task.sourceDirectories.from(extension.sourceDirectories)
            task.outputDirectory.set(extension.outputDirectory)
            task.packageName.set(extension.packageName)
            task.schemaName.set(extension.schemaName)
            task.generateTokenHandles.set(extension.generateTokenHandles)
            task.generateCategoryHandles.set(extension.generateCategoryHandles)
            task.generateDebugHelpers.set(extension.generateDebugHelpers)
            task.generateBuilderExtensions.set(extension.generateBuilderExtensions)
            task.generateNavigators.set(extension.generateNavigators)
        }

        // Wire into Kotlin JVM if present
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlinExt = project.extensions.findByType(KotlinProjectExtension::class.java)
            kotlinExt?.sourceSets?.findByName("main")?.kotlin?.srcDir(generateCstTask.flatMap { it.outputDirectory })
        }

        // Wire into Kotlin Multiplatform if present
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kmpExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            val targetSourceSet = kmpExt?.sourceSets?.findByName("commonMain")
                ?: kmpExt?.sourceSets?.findByName("main")
            targetSourceSet?.kotlin?.srcDir(generateCstTask.flatMap { it.outputDirectory })
        }

        // Fallback: also wire into standard Java SourceSetContainer if neither Kotlin plugin is applied yet
        project.pluginManager.withPlugin("java") {
            val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
            sourceSets?.findByName("main")?.java?.srcDir(generateCstTask.flatMap { it.outputDirectory })
        }
    }

    public companion object {
        public const val EXTENSION_NAME: String = "dialectorCst"
        public const val TASK_NAME: String = "generateCst"
    }
}
