package dev.dialector.cst.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/**
 * Gradle plugin (`dev.dialector.cst`) that registers the `dialectorCst` extension,
 * creates the `generateCst` task (and test-scoped `generateTestCst` when applicable),
 * and wires generated Kotlin directories into the project's source sets.
 */
class DialectorCstPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            DialectorCstExtension::class.java,
        )

        val cliConfiguration = project.configurations.maybeCreate(CLI_CONFIGURATION_NAME).apply {
            isCanBeConsumed = false
            isCanBeResolved = true
            description = "Classpath containing the dialector-cst CLI tool."
            attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }
            defaultDependencies { deps ->
                val cliVersion = project.version.toString().takeIf { it != "unspecified" && !it.startsWith("unspecified") }
                val depNotation = if (cliVersion != null) {
                    "dev.dialector:dialector-cst-cli:$cliVersion"
                } else {
                    "dev.dialector:dialector-cst-cli"
                }
                deps.add(project.dependencies.create(depNotation))
            }
        }
        extension.cliClasspath.from(cliConfiguration)

        extension.sourceDirectories.from(project.layout.projectDirectory.dir("src/main/cst"))
        extension.outputDirectory.convention(
            project.layout.buildDirectory.dir("generated/source/cst/main/kotlin")
        )
        extension.testSourceDirectories.from(project.layout.projectDirectory.dir("src/test/cst"))
        extension.testOutputDirectory.convention(
            project.layout.buildDirectory.dir("generated/source/cst/test/kotlin")
        )

        val generateCstTask = project.tasks.register(TASK_NAME, GenerateCstTask::class.java) { task ->
            task.cliClasspath.from(extension.cliClasspath)
            task.sourceDirectories.from(extension.sourceDirectories)
            task.outputDirectory.set(extension.outputDirectory)
            task.packageName.set(extension.packageName)
            task.schemaName.set(extension.schemaName)
            task.generateTokenHandles.set(extension.generateTokenHandles)
            task.generateCategoryHandles.set(extension.generateCategoryHandles)
            task.generateDebugHelpers.set(extension.generateDebugHelpers)
            task.generateBuilderExtensions.set(extension.generateBuilderExtensions)
            task.generateNavigators.set(extension.generateNavigators)
            task.nodeSuffix.set(extension.nodeSuffix)
            task.categorySuffix.set(extension.categorySuffix)
            task.tokenSuffix.set(extension.tokenSuffix)
            task.tokenCategorySuffix.set(extension.tokenCategorySuffix)
        }

        val generateTestCstTask = project.tasks.register(TEST_TASK_NAME, GenerateCstTask::class.java) { task ->
            task.description = "Generates test CST Kotlin source files from Dialector .cst schema files."
            task.cliClasspath.from(extension.cliClasspath)
            task.sourceDirectories.from(extension.testSourceDirectories)
            task.outputDirectory.set(extension.testOutputDirectory)
            task.packageName.set(extension.packageName)
            task.schemaName.set(extension.schemaName)
            task.generateTokenHandles.set(extension.generateTokenHandles)
            task.generateCategoryHandles.set(extension.generateCategoryHandles)
            task.generateDebugHelpers.set(extension.generateDebugHelpers)
            task.generateBuilderExtensions.set(extension.generateBuilderExtensions)
            task.generateNavigators.set(extension.generateNavigators)
            task.nodeSuffix.set(extension.nodeSuffix)
            task.categorySuffix.set(extension.categorySuffix)
            task.tokenSuffix.set(extension.tokenSuffix)
            task.tokenCategorySuffix.set(extension.tokenCategorySuffix)
        }

        // Wire into Kotlin JVM if present
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlinExt = project.extensions.findByType(KotlinProjectExtension::class.java)
            kotlinExt?.sourceSets?.findByName("main")?.kotlin?.srcDir(generateCstTask.flatMap { it.outputDirectory })
            kotlinExt?.sourceSets?.findByName("test")?.kotlin?.srcDir(generateTestCstTask.flatMap { it.outputDirectory })
        }

        // Wire into Kotlin Multiplatform if present
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kmpExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            val mainSourceSet = kmpExt?.sourceSets?.findByName("commonMain")
                ?: kmpExt?.sourceSets?.findByName("main")
            mainSourceSet?.kotlin?.srcDir(generateCstTask.flatMap { it.outputDirectory })

            val testSourceSet = kmpExt?.sourceSets?.findByName("commonTest")
                ?: kmpExt?.sourceSets?.findByName("test")
            testSourceSet?.kotlin?.srcDir(generateTestCstTask.flatMap { it.outputDirectory })
        }

        // Fallback: also wire into standard Java SourceSetContainer if neither Kotlin plugin is applied yet
        project.pluginManager.withPlugin("java") {
            val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
            sourceSets?.findByName("main")?.java?.srcDir(generateCstTask.flatMap { it.outputDirectory })
            sourceSets?.findByName("test")?.java?.srcDir(generateTestCstTask.flatMap { it.outputDirectory })
        }
    }

    companion object {
        const val EXTENSION_NAME: String = "dialectorCst"
        const val CLI_CONFIGURATION_NAME: String = "dialectorCstCli"
        const val TASK_NAME: String = "generateCst"
        const val TEST_TASK_NAME: String = "generateTestCst"
    }
}
