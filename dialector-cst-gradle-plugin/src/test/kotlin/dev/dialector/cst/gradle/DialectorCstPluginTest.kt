package dev.dialector.cst.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DialectorCstPluginTest {
    private lateinit var testProjectDir: File

    @BeforeTest
    fun setUp() {
        testProjectDir = Files.createTempDirectory("dialector-cst-gradle-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        testProjectDir.deleteRecursively()
    }

    @Test
    fun `plugin registers extension and generateCst task with default conventions`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir)
            .build()

        project.pluginManager.apply("dev.dialector.cst")

        val extension = project.extensions.findByType(DialectorCstExtension::class.java)
        assertNotNull(extension, "dialectorCst extension should be registered")
        assertEquals(true, extension.generateTokenHandles.get())
        assertEquals(true, extension.generateCategoryHandles.get())
        assertEquals(true, extension.generateDebugHelpers.get())
        assertEquals(true, extension.generateBuilderExtensions.get())
        assertEquals(true, extension.generateNavigators.get())

        val task = project.tasks.findByName("generateCst") as? GenerateCstTask
        assertNotNull(task, "generateCst task should be registered")
        assertTrue(
            task.outputDirectory.get().asFile.invariantSeparatorsPath.endsWith("build/generated/source/cst/main/kotlin")
        )
    }

    @Test
    fun `plugin wires output directory into kotlin jvm main sourceSet`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir)
            .build()

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("dev.dialector.cst")

        val kotlinExt = project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java)
        val mainSourceSet = kotlinExt.sourceSets.getByName("main")
        val srcDirs = mainSourceSet.kotlin.srcDirs.map { it.invariantSeparatorsPath }

        assertTrue(
            srcDirs.any { it.endsWith("build/generated/source/cst/main/kotlin") },
            "Expected generated CST directory in Kotlin main sourceSet, found: $srcDirs",
        )
    }

    @Test
    fun `generateCst task executes via GradleRunner and supports incremental build caching`() {
        File(testProjectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-cst-consumer"
            """.trimIndent()
        )

        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("dev.dialector.cst")
            }

            dialectorCst {
                packageName.set("com.example.expr")
                schemaName.set("ExprLang")
            }
            """.trimIndent()
        )

        val cstDir = File(testProjectDir, "src/main/cst").apply { mkdirs() }
        File(cstDir, "Expr.cst").writeText(
            """
            trivia Whitespace
            token Number
            token Plus = "+"

            category Expr = LitExpr | AddExpr

            node LitExpr {
                digits: Number
            }

            node AddExpr {
                left: Expr
                plus: '+'
                right: Expr
            }
            """.trimIndent()
        )

        val firstRun = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateCst", "--stacktrace")
            .build()

        assertEquals(TaskOutcome.SUCCESS, firstRun.task(":generateCst")?.outcome)

        val generatedDir = File(testProjectDir, "build/generated/source/cst/main/kotlin/com/example/expr")
        assertTrue(File(generatedDir, "ExprLangKinds.kt").isFile)
        assertTrue(File(generatedDir, "ExprLangHandles.kt").isFile)
        assertTrue(File(generatedDir, "ExprLangExtensions.kt").isFile)

        // Second run with no changes should be UP-TO-DATE
        val secondRun = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateCst")
            .build()

        assertEquals(TaskOutcome.UP_TO_DATE, secondRun.task(":generateCst")?.outcome)
    }

    @Test
    fun `generateCst task fails with clear diagnostics when schema is invalid`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "broken-cst"""")
        File(testProjectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("dev.dialector.cst")
            }
            """.trimIndent()
        )

        val cstDir = File(testProjectDir, "src/main/cst").apply { mkdirs() }
        File(cstDir, "Broken.cst").writeText(
            """
            schema Broken
            package com.example.broken

            node InvalidNode {
                badField: NonExistentType
            }
            """.trimIndent()
        )

        val failureResult = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateCst")
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, failureResult.task(":generateCst")?.outcome)
        assertTrue(failureResult.output.contains("NonExistentType"))
    }
}
