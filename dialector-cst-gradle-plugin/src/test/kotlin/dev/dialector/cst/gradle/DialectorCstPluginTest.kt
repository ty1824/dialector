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

    private fun testBuildScript(extraConfig: String = ""): String {
        val runner = GradleRunner.create().withPluginClasspath()
        val cpList = runner.pluginClasspath.joinToString { "\"${it.absolutePath.replace("\\", "\\\\")}\"" }
        return """
            plugins {
                id("dev.dialector.cst")
            }

            dependencies {
                dialectorCstCli(files($cpList))
            }

            $extraConfig
        """.trimIndent()
    }

    @Test
    fun `plugin registers extension and generateCst task with default conventions`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir)
            .build()

        project.pluginManager.apply("dev.dialector.cst")

        val extension = project.extensions.findByType(DialectorCstExtension::class.java)
        assertNotNull(extension, "dialectorCst extension should be registered")
        assertNotNull(project.configurations.findByName(DialectorCstPlugin.CLI_CONFIGURATION_NAME))
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

        val testTask = project.tasks.findByName("generateTestCst") as? GenerateCstTask
        assertNotNull(testTask, "generateTestCst task should be registered")
        assertTrue(
            testTask.outputDirectory.get().asFile.invariantSeparatorsPath.endsWith("build/generated/source/cst/test/kotlin")
        )
    }

    @Test
    fun `plugin wires output directory into kotlin jvm main and test sourceSets`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir)
            .build()

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("dev.dialector.cst")

        val kotlinExt = project.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java)
        val mainSourceSet = kotlinExt.sourceSets.getByName("main")
        val mainSrcDirs = mainSourceSet.kotlin.srcDirs.map { it.invariantSeparatorsPath }

        assertTrue(
            mainSrcDirs.any { it.endsWith("build/generated/source/cst/main/kotlin") },
            "Expected generated CST directory in Kotlin main sourceSet, found: $mainSrcDirs",
        )

        val testSourceSet = kotlinExt.sourceSets.getByName("test")
        val testSrcDirs = testSourceSet.kotlin.srcDirs.map { it.invariantSeparatorsPath }

        assertTrue(
            testSrcDirs.any { it.endsWith("build/generated/source/cst/test/kotlin") },
            "Expected generated test CST directory in Kotlin test sourceSet, found: $testSrcDirs",
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
            testBuildScript(
                """
                dialectorCst {
                    packageName.set("com.example.expr")
                    schemaName.set("ExprLang")
                }
                """.trimIndent()
            )
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
        assertTrue(File(generatedDir, "ExprLangSyntax.kt").isFile)
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
    fun `generateTestCst task compiles src test cst schemas into test output directory`() {
        File(testProjectDir, "settings.gradle.kts").writeText(
            """
            rootProject.name = "test-cst-test-sourceset"
            """.trimIndent()
        )

        File(testProjectDir, "build.gradle.kts").writeText(testBuildScript())

        val testCstDir = File(testProjectDir, "src/test/cst").apply { mkdirs() }
        File(testCstDir, "TestOnly.cst").writeText(
            """
            schema TestOnlyLang
            package com.example.testonly

            token Ident
            node TestNode {
                id: Ident
            }
            """.trimIndent()
        )

        val run = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateTestCst", "--stacktrace")
            .build()

        assertEquals(TaskOutcome.SUCCESS, run.task(":generateTestCst")?.outcome)

        val generatedTestDir = File(testProjectDir, "build/generated/source/cst/test/kotlin/com/example/testonly")
        assertTrue(File(generatedTestDir, "TestOnlyLangKinds.kt").isFile)
        assertTrue(File(generatedTestDir, "TestOnlyLangSyntax.kt").isFile)
        assertTrue(File(generatedTestDir, "TestOnlyLangExtensions.kt").isFile)
    }

    @Test
    fun `generateCst task fails with clear diagnostics when schema is invalid`() {
        File(testProjectDir, "settings.gradle.kts").writeText("""rootProject.name = "broken-cst"""")
        File(testProjectDir, "build.gradle.kts").writeText(testBuildScript())

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
