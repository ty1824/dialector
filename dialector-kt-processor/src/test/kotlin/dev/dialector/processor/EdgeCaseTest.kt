package dev.dialector.processor

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.sourcesGeneratedBySymbolProcessor
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for edge cases in the symbol processor.
 * These tests verify that the processor handles complex scenarios correctly.
 */
@OptIn(ExperimentalCompilerApi::class)
class EdgeCaseTest {
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
                kspProcessorOptions =
                    mutableMapOf(
                        "dev.dialector.targetPackage" to targetPackage,
                        "dev.dialector.factory" to factory.toString(),
                    )
                inheritClassPath = true
                messageOutputStream = System.out
            }.compile()

    @Test
    fun `handles empty node without any members`() {
        val source =
            SourceFile.kotlin(
                "EmptyNode.kt",
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

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "EmptyNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should generate implementation but no initializer
        assertTrue(generatedCode.contains("private class EmptyNodeImpl"))
        assertTrue(!generatedCode.contains("class EmptyNodeInitializer"))

        // DSL function should not take lambda
        assertTrue(generatedCode.contains("fun emptyNode(): EmptyNode"))
    }

    @Test
    fun `handles node with multiple levels of inheritance`() {
        val source =
            SourceFile.kotlin(
                "InheritanceChain.kt",
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
                interface MiddleNode : BaseNode {
                    @Property
                    val middleProp: String
                }

                @NodeDefinition
                interface LeafNode : MiddleNode {
                    @Property
                    val leafProp: String
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor

        // All three files should be generated
        assertTrue(generatedFiles.any { it.name == "BaseNodeModel.kt" })
        assertTrue(generatedFiles.any { it.name == "MiddleNodeModel.kt" })
        assertTrue(generatedFiles.any { it.name == "LeafNodeModel.kt" })

        val leafFile = generatedFiles.find { it.name == "LeafNodeModel.kt" }!!
        val leafCode = leafFile.readText()

        // Leaf implementation should have all three properties
        assertTrue(leafCode.contains("override var baseProp:"))
        assertTrue(leafCode.contains("override var middleProp:"))
        assertTrue(leafCode.contains("override var leafProp:"))
    }

    @Test
    fun `handles node with all annotation types`() {
        val source =
            SourceFile.kotlin(
                "ComplexNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property
                import dev.dialector.syntax.Child
                import dev.dialector.syntax.Reference
                import dev.dialector.syntax.NodeReference

                @NodeDefinition
                interface ComplexNode : Node {
                    @Property
                    val prop: String

                    @Property
                    val nullableProp: String?

                    @Property(hasDefault = true)
                    val defaultProp: String
                        get() = "default"

                    @Child
                    val child: SimpleNode

                    @Child
                    val nullableChild: SimpleNode?

                    @Child
                    val someChildren: List<SimpleNode>

                    @Reference
                    val ref: NodeReference<SimpleNode>

                    @Reference
                    val nullableRef: NodeReference<SimpleNode>?
                }

                @NodeDefinition
                interface SimpleNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "ComplexNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Verify all properties are present
        assertTrue(generatedCode.contains("override var prop:"))
        assertTrue(generatedCode.contains("override var nullableProp:"))
        assertTrue(generatedCode.contains("override var defaultProp:"))

        // Verify all children are present
        assertTrue(generatedCode.contains("override var child:"))
        assertTrue(generatedCode.contains("override var nullableChild:"))
        assertTrue(generatedCode.contains("override val someChildren:"))

        // Verify all references are present
        assertTrue(generatedCode.contains("override var ref:"))
        assertTrue(generatedCode.contains("override var nullableRef:"))

        // Verify maps are populated
        assertTrue(generatedCode.contains("override val properties:"))
        assertTrue(generatedCode.contains("override val children:"))
        assertTrue(generatedCode.contains("override val references:"))
    }

    @Test
    fun `handles node with generic type parameters in children`() {
        val source =
            SourceFile.kotlin(
                "GenericChildren.kt",
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
        val generatedFile = generatedFiles.find { it.name == "ParentNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should handle List generic properly
        assertTrue(generatedCode.contains("MutableList<"))
    }

    @Test
    fun `handles multiple nodes in same file`() {
        val source =
            SourceFile.kotlin(
                "MultipleNodes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface FirstNode : Node {
                    @Property
                    val name: String
                }

                @NodeDefinition
                interface SecondNode : Node {
                    @Property
                    val value: Int
                }

                @NodeDefinition
                interface ThirdNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor

        // All three model files should be generated
        assertTrue(generatedFiles.any { it.name == "FirstNodeModel.kt" })
        assertTrue(generatedFiles.any { it.name == "SecondNodeModel.kt" })
        assertTrue(generatedFiles.any { it.name == "ThirdNodeModel.kt" })
    }

    @Test
    fun `handles nodes with different primitive property types`() {
        val source =
            SourceFile.kotlin(
                "PrimitiveTypes.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property

                @NodeDefinition
                interface TypesNode : Node {
                    @Property
                    val stringProp: String

                    @Property
                    val intProp: Int

                    @Property
                    val longProp: Long

                    @Property
                    val boolProp: Boolean

                    @Property
                    val doubleProp: Double

                    @Property
                    val listProp: List<String>

                    @Property
                    val mapProp: Map<String, Int>
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "TypesNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // All properties should be present
        assertTrue(generatedCode.contains("override var stringProp:"))
        assertTrue(generatedCode.contains("override var intProp:"))
        assertTrue(generatedCode.contains("override var longProp:"))
        assertTrue(generatedCode.contains("override var boolProp:"))
        assertTrue(generatedCode.contains("override var doubleProp:"))
        assertTrue(generatedCode.contains("override var listProp:"))
        assertTrue(generatedCode.contains("override var mapProp:"))
    }

    @Test
    fun `handles self-referential child types`() {
        val source =
            SourceFile.kotlin(
                "SelfRef.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Child

                @NodeDefinition
                interface TreeNode : Node {
                    @Child
                    val left: TreeNode?

                    @Child
                    val right: TreeNode?
                }
                """.trimIndent(),
            )

        val result = compileWithProcessor(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "TreeNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should handle self-referential types
        assertTrue(generatedCode.contains("override var left:"))
        assertTrue(generatedCode.contains("override var right:"))
    }

    @Test
    fun `handles factory with all parameter types`() {
        val source =
            SourceFile.kotlin(
                "FactoryNode.kt",
                """
                package test

                import dev.dialector.syntax.Node
                import dev.dialector.syntax.NodeDefinition
                import dev.dialector.syntax.Property
                import dev.dialector.syntax.Child
                import dev.dialector.syntax.Reference
                import dev.dialector.syntax.NodeReference

                @NodeDefinition
                interface FactoryNode : Node {
                    @Property
                    val name: String

                    @Property
                    val optional: String?

                    @Child
                    val child: SimpleNode

                    @Child
                    val someChildren: List<SimpleNode>

                    @Reference
                    val ref: NodeReference<SimpleNode>
                }

                @NodeDefinition
                interface SimpleNode : Node
                """.trimIndent(),
            )

        val result = compileWithProcessor(source, factory = true)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "FactoryNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Factory function should have all parameters
        assertTrue(generatedCode.contains("fun factoryNode(\n    name:"))
        assertTrue(generatedCode.contains("optional:"))
        assertTrue(generatedCode.contains("child:"))
        assertTrue(generatedCode.contains("someChildren:"))
        assertTrue(generatedCode.contains("ref:"))

        // Factory should have default values for optionals and lists
        assertTrue(generatedCode.contains("= null") || generatedCode.contains("null"))
        assertTrue(generatedCode.contains("listOf()"))
    }

    @Test
    fun `generates files in correct target package`() {
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

        val result = compileWithProcessor(source, targetPackage = "com.example.generated")

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Should be in the correct package
        assertTrue(generatedCode.startsWith("package com.example.generated"))
    }

    @Test
    fun `handles parent property setter correctly`() {
        val source =
            SourceFile.kotlin(
                "ParentTest.kt",
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

        val generatedFiles = result.sourcesGeneratedBySymbolProcessor
        val generatedFile = generatedFiles.find { it.name == "TestNodeModel.kt" }
        assertTrue(generatedFile != null)

        val generatedCode = generatedFile.readText()

        // Parent property should have custom setter
        assertTrue(generatedCode.contains("override var parent:"))
        assertTrue(generatedCode.contains("set(`value`)"), "Parent setter should exist:\n$generatedCode")
        assertTrue(
            generatedCode.contains("throw RuntimeException"),
            "Parent setter should prevent reassignment",
        )
    }
}
