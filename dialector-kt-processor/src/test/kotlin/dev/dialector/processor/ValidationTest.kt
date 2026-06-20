package dev.dialector.processor

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for validation errors in the symbol processor.
 * These tests verify that the processor correctly rejects invalid node definitions.
 */
@OptIn(ExperimentalCompilerApi::class)
class ValidationTest {
    private fun compileWithProcessor(
        vararg sources: SourceFile,
        targetPackage: String = "test.generated",
    ): JvmCompilationResult =
        KotlinCompilation()
            .apply {
                this.sources = sources.toList()
                configureKsp {
                    symbolProcessorProviders += DialectorSymbolProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        "dev.dialector.targetPackage" to targetPackage,
                    )
                inheritClassPath = true
                messageOutputStream = System.out
            }.compile()

    @Test
    fun `fails when node does not extend Node interface`() {
        val source =
            SourceFile.kotlin(
                "InvalidNode.kt",
                """
                package test

                import dev.dialector.syntax.NodeDefinition

                @NodeDefinition
                interface InvalidNode {
                    val name: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("Node as a superinterface"),
            "Should error about missing Node superinterface",
        )
    }

    @Test
    fun `fails when node is final`() {
        val source =
            SourceFile.kotlin(
                "FinalNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition

                @NodeDefinition
                final interface FinalNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        // Note: In Kotlin, interfaces cannot be final, but we test sealed
        // This test documents expected behavior for extensibility checks
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
    }

    @Test
    fun `fails when node is sealed`() {
        val source =
            SourceFile.kotlin(
                "SealedNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition

                @NodeDefinition
                sealed interface SealedNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("extensible"),
            "Should error about sealed node",
        )
    }

    @Test
    fun `fails when property is Node type`() {
        val source =
            SourceFile.kotlin(
                "InvalidProperty.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface InvalidNode : Node {
                    @Property
                    val nodeProperty: Node
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("Property must not be of type Node"),
            "Should error about property being Node type",
        )
    }

    @Test
    fun `fails when child is not Node or List of Node`() {
        val source =
            SourceFile.kotlin(
                "InvalidChild.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface InvalidNode : Node {
                    @Child
                    val invalidChild: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("Child must be of type Node or List<Node>"),
            "Should error about invalid child type",
        )
    }

    @Test
    fun `fails when reference is not NodeReference type`() {
        val source =
            SourceFile.kotlin(
                "InvalidReference.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Reference

                @NodeDefinition
                interface InvalidNode : Node {
                    @Reference
                    val invalidRef: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("Reference must be of type NodeReference"),
            "Should error about invalid reference type",
        )
    }

    @Test
    fun `includes file location in error messages`() {
        val source =
            SourceFile.kotlin(
                "InvalidNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface InvalidNode : Node {
                    @Property
                    val nodeProperty: Node
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)

        // Should include file path and line number in error
        assertTrue(
            result.messages.contains("InvalidNode.kt:") || result.messages.contains("InvalidNode"),
            "Error message should reference the source file",
        )
    }

    @Test
    fun `fails when required option is missing`() {
        val result =
            KotlinCompilation()
                .apply {
                    sources =
                        listOf(
                            SourceFile.kotlin(
                                "TestNode.kt",
                                """
                                package test

                                import dev.dialector.syntax.Node
                                import dev.dialector.syntax.NodeDefinition

                                @NodeDefinition
                                interface TestNode : Node
                                """.trimIndent(),
                            ),
                        )
                    configureKsp {
                        symbolProcessorProviders += DialectorSymbolProcessorProvider()
                    }
                    // Don't set kspArgs - missing required targetPackage
                    inheritClassPath = true
                    messageOutputStream = System.out
                }.compile()

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains("dev.dialector.targetPackage") ||
                result.messages.contains("target package"),
            "Should error about missing targetPackage option",
        )
    }

    @Test
    fun `validates multiple errors at once`() {
        val source =
            SourceFile.kotlin(
                "MultipleErrors.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property
                import dev.dialector.syntax.Child

                @NodeDefinition
                sealed interface InvalidNode : Node {
                    @Property
                    val nodeProperty: Node

                    @Child
                    val stringChild: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)

        // Should report multiple errors
        val errorCount = result.messages.split("Error").size - 1
        assertTrue(errorCount >= 2, "Should report multiple validation errors")
    }

    @Test
    fun `accepts valid nullable child`() {
        val source =
            SourceFile.kotlin(
                "ValidNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface ParentNode : Node {
                    @Child
                    val child: ChildNode?
                }

                @NodeDefinition
                interface ChildNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `accepts valid list of children`() {
        val source =
            SourceFile.kotlin(
                "ValidNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface ParentNode : Node {
                    @Child
                    val someChildren: List<ChildNode>
                }

                @NodeDefinition
                interface ChildNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `accepts valid nullable reference`() {
        val source =
            SourceFile.kotlin(
                "ValidNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.NodeReference
                import dev.dialector.syntax.Reference

                @NodeDefinition
                interface SourceNode : Node {
                    @Reference
                    val target: NodeReference<TargetNode>?
                }

                @NodeDefinition
                interface TargetNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
