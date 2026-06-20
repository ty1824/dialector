package dev.dialector.processor

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for code generation using kotlin-compile-testing.
 * These tests verify that the symbol processor correctly generates code for various node definitions.
 */
@OptIn(ExperimentalCompilerApi::class)
class CodeGenerationTest {
    private fun compileWithProcessor(
        vararg sources: SourceFile,
        targetPackage: String = "test.generated",
        factory: Boolean = false,
    ): JvmCompilationResult =
        KotlinCompilation()
            .apply {
                this.sources = sources.toList()
                configureKsp {
                    symbolProcessorProviders += DialectorSymbolProcessorProvider()
                }
                kspWithCompilation = true
                kspProcessorOptions =
                    mutableMapOf(
                        "dev.dialector.targetPackage" to targetPackage,
                        "dev.dialector.factory" to factory.toString(),
                    )
                inheritClassPath = true
                messageOutputStream = System.out
            }.compile()

    @Test
    fun `generates code for simple node with property`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface TestNode : Node {
                    @Property
                    val name: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, "Compilation failed: ${result.messages}")

        // Verify generated files exist
        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "TestNodeModel.kt" }
        assertTrue(
            generatedFile != null,
            "Generated file TestNodeModel.kt should exist. Generated files: ${generatedFiles.map { it.name }}",
        )

        val generatedCode = generatedFile.readText()

        // Verify implementation class is generated
        assertTrue(generatedCode.contains("private class TestNodeImpl"), "Should generate TestNodeImpl class")

        // Verify initializer class is generated
        assertTrue(generatedCode.contains("class TestNodeInitializer"), "Should generate TestNodeInitializer class")

        // Verify DSL function is generated
        assertTrue(generatedCode.contains("fun testNode("), "Should generate testNode DSL function")

        // Verify property is in implementation
        assertTrue(generatedCode.contains("override var name:"), "Should include name property")
    }

    @Test
    fun `generates code for node with child`() {
        val source =
            SourceFile.kotlin(
                "TestNodes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface ParentNode : Node {
                    @Child
                    val child: ChildNode
                }

                @NodeDefinition
                interface ChildNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, "Compilation failed: ${result.messages}")

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val parentFile = generatedFiles.find { it.name == "ParentNodeModel.kt" }
        assertTrue(parentFile != null)

        val generatedCode = parentFile.readText()

        // Verify child property is in implementation
        assertTrue(generatedCode.contains("override var child:"), "Should include child property")

        // Verify children map includes the child
        assertTrue(generatedCode.contains("\"child\" to"), "Children map should include child")
    }

    @Test
    fun `generates code for node with list of children`() {
        val source =
            SourceFile.kotlin(
                "TestNodes.kt",
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

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val parentFile = generatedFiles.find { it.name == "ParentNodeModel.kt" }
        assertTrue(parentFile != null)

        val generatedCode = parentFile.readText()

        // Verify children property is mutable list
        assertTrue(
            generatedCode.contains("override val someChildren: MutableList<"),
            "List children should be converted to MutableList:\n$generatedCode",
        )
    }

    @Test
    fun `generates code for node with reference`() {
        val source =
            SourceFile.kotlin(
                "TestNodes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.NodeReference
                import dev.dialector.syntax.Reference

                @NodeDefinition
                interface SourceNode : Node {
                    @Reference
                    val target: NodeReference<TargetNode>
                }

                @NodeDefinition
                interface TargetNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val sourceFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "SourceNodeModel.kt" }
        assertTrue(sourceFile != null)

        val generatedCode = sourceFile.readText()

        // Verify reference property is in implementation
        assertTrue(generatedCode.contains("override var target:"), "Should include target reference")

        // Verify NodeReferenceImpl is used
        assertTrue(generatedCode.contains("NodeReferenceImpl"), "Should use NodeReferenceImpl")

        // Verify references map includes the reference
        assertTrue(generatedCode.contains("\"target\" to"), "References map should include target")
    }

    @Test
    fun `generates code for node with optional property`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface TestNode : Node {
                    @Property
                    val name: String?
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify nullable property
        assertTrue(generatedCode.contains("override var name: String?"), "Should handle nullable property type")
    }

    @Test
    fun `generates code for node with default property`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface TestNode : Node {
                    @Property(hasDefault = true)
                    val name: String
                        get() = "default"
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify default property handling with ?: super.name fallback
        assertTrue(
            generatedCode.contains("?: super.name") || generatedCode.contains("?: super."),
            "Should handle default property with fallback to super",
        )
    }

    @Test
    fun `generates factory function when enabled`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface TestNode : Node {
                    @Property
                    val name: String

                    @Child
                    val child: ChildNode?
                }

                @NodeDefinition
                interface ChildNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source, factory = true)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should have two testNode functions (DSL and factory)
        val functionCount = "fun testNode\\(".toRegex().findAll(generatedCode).count()
        assertEquals(2, functionCount, "Should generate both DSL and factory functions")

        // Verify factory has parameters
        assertTrue(
            generatedCode.contains("fun testNode(name: String"),
            "Factory function should have name parameter:\n$generatedCode",
        )
    }

    @Test
    fun `generates code for node without init requirement`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition

                @NodeDefinition
                interface EmptyNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "EmptyNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should not generate initializer for empty node
        assertTrue(
            !generatedCode.contains("class EmptyNodeInitializer"),
            "Should not generate initializer for empty node",
        )

        // DSL function should return directly without lambda
        assertTrue(
            generatedCode.contains("fun emptyNode(): EmptyNode"),
            "DSL function should not take lambda for empty node",
        )
    }

    @Test
    fun `generates toString implementation`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition

                @NodeDefinition
                interface TestNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify toString delegates to toDebugString
        assertTrue(
            generatedCode.contains("override fun toString()"),
            "Should generate toString override",
        )
        assertTrue(
            generatedCode.contains("toDebugString"),
            "toString should delegate to toDebugString",
        )
    }

    @Test
    fun `generates code for node with inherited properties`() {
        val source =
            SourceFile.kotlin(
                "TestNodes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface BaseNode : Node {
                    @Property
                    val baseProp: String
                }

                @NodeDefinition
                interface DerivedNode : BaseNode {
                    @Property
                    val derivedProp: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor

        // Both files should be generated
        val baseFile = generatedFiles.find { it.name == "BaseNodeModel.kt" }
        val derivedFile = generatedFiles.find { it.name == "DerivedNodeModel.kt" }

        assertTrue(baseFile != null, "BaseNodeModel should be generated")
        assertTrue(derivedFile != null, "DerivedNodeModel should be generated")

        val derivedCode = derivedFile.readText()

        // Derived implementation should implement DerivedNode
        assertTrue(
            derivedCode.contains(" : DerivedNode"),
            "Should implement DerivedNode interface",
        )
    }

    @Test
    fun `generates properties map correctly`() {
        val source =
            SourceFile.kotlin(
                "TestNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface TestNode : Node {
                    @Property
                    val prop1: String

                    @Property
                    val prop2: Int
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify properties map implementation
        assertTrue(generatedCode.contains("override val properties:"), "Should have properties getter")
        assertTrue(generatedCode.contains("\"prop1\" to prop1"), "Should include prop1 in map")
        assertTrue(generatedCode.contains("\"prop2\" to prop2"), "Should include prop2 in map")
    }

    @Test
    fun `generates children map correctly`() {
        val source =
            SourceFile.kotlin(
                "TestNodes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface ParentNode : Node {
                    @Child
                    val single: ChildNode

                    @Child
                    val optional: ChildNode?

                    @Child
                    val list: List<ChildNode>
                }

                @NodeDefinition
                interface ChildNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFile = result.sourcesGeneratedBySymbolProcessor.find { it.name == "ParentNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify children map implementation
        assertTrue(generatedCode.contains("override val children:"), "Should have children getter")
        assertTrue(generatedCode.contains("\"single\" to"), "Should include single child")
        assertTrue(generatedCode.contains("\"optional\" to"), "Should include optional child")
        assertTrue(generatedCode.contains("\"list\" to"), "Should include list children")

        // Optional child should use listOfNotNull
        assertTrue(
            generatedCode.contains("listOfNotNull(optional)"),
            "Optional child should use listOfNotNull",
        )
    }
}
