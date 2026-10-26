package dev.dialector.cst.generator

import dev.dialector.cst.schema.CategorySchema
import dev.dialector.cst.schema.CstSchema
import dev.dialector.cst.schema.FieldSchema
import dev.dialector.cst.schema.FieldType
import dev.dialector.cst.schema.NodeSchema
import dev.dialector.cst.schema.TokenSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CstCodeGeneratorTest {

    @Test
    fun `generator produces complete file bundle for schema`() {
        val schema = CstSchema(
            name = "Calculator",
            packageName = "dev.dialector.calc",
            tokens = listOf(
                TokenSchema("Number", id = 1),
                TokenSchema("Plus", id = 2, fixedText = "+"),
                TokenSchema("Minus", id = 3, fixedText = "-"),
            ),
            categories = listOf(
                CategorySchema("Expression"),
            ),
            nodes = listOf(
                NodeSchema(
                    name = "NumberLiteral",
                    id = 1,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("value", FieldType.ChildToken("Number"), slot = 0),
                    ),
                ),
                NodeSchema(
                    name = "BinaryExpression",
                    id = 2,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("left", FieldType.ChildNode("Expression"), slot = 0),
                        FieldSchema("operator", FieldType.ChildToken(), slot = 1),
                        FieldSchema("right", FieldType.ChildNode("Expression"), slot = 2),
                    ),
                ),
                NodeSchema(
                    name = "UnaryExpression",
                    id = 3,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("operator", FieldType.ChildToken(), slot = 0),
                        FieldSchema("operand", FieldType.ChildNode("Expression"), slot = 1),
                    ),
                ),
            ),
        ).validate()

        val generator = CstCodeGenerator()
        val result = generator.generate(schema)

        assertEquals(3, result.files.size)
        val fileNames = result.files.map { it.fileName }
        assertTrue("CalculatorKinds.kt" in fileNames)
        assertTrue("CalculatorHandles.kt" in fileNames)
        assertTrue("CalculatorExtensions.kt" in fileNames)

        // Check Kinds
        val kindsCode = result.files.find { it.fileName == "CalculatorKinds.kt" }!!.content
        assertTrue(kindsCode.contains("public object CalculatorNodeKinds"))
        assertTrue(kindsCode.contains("public const val NUMBER_LITERAL: Int"))
        assertTrue(kindsCode.contains("public const val BINARY_EXPRESSION: Int"))
        assertTrue(kindsCode.contains("public object CalculatorTokenKinds"))
        assertTrue(kindsCode.contains("public const val NUMBER: Int"))
        assertTrue(kindsCode.contains("public const val PLUS: Int"))
        assertTrue(kindsCode.contains("public fun getTokenText(kindId: Int): String?"))
        assertTrue(kindsCode.contains("PLUS -> \"+\""))
        assertTrue(kindsCode.contains("public object CalculatorCategories"))
        assertTrue(kindsCode.contains("public val EXPRESSION_KINDS: Set<Int>"))
        assertTrue(kindsCode.contains("public fun isExpression(kindId: Int): Boolean"))

        // Check Handles
        val handlesCode = result.files.find { it.fileName == "CalculatorHandles.kt" }!!.content
        assertTrue(handlesCode.contains("@JvmInline"))
        assertTrue(handlesCode.contains("public value class BinaryExpressionHandle("))
        assertTrue(handlesCode.contains("public val nodeIndex: Int"))
        assertTrue(handlesCode.contains("public val asNode: CstNode"))
        assertTrue(handlesCode.contains("get() = CstNode(nodeIndex)"))
        assertTrue(handlesCode.contains("public val isExpression: Boolean"))
        assertTrue(handlesCode.contains("get() = true"))
        assertTrue(handlesCode.contains("public val CstNode.asBinaryExpression: BinaryExpressionHandle"))
        assertTrue(handlesCode.contains("public val CstNode.isExpression: Boolean"))
        assertTrue(handlesCode.contains("public value class NumberTokenHandle("))
        assertTrue(handlesCode.contains("public value class PlusTokenHandle("))
        assertTrue(handlesCode.contains("const val FIXED_TEXT: String = \"+\""))

        // Check Member Accessors inside Handles
        assertTrue(handlesCode.contains("context(tree: ConcreteSyntaxTree)"))
        assertTrue(handlesCode.contains("public val left: ExpressionHandle"))
        assertTrue(handlesCode.contains("get() = ExpressionHandle(tree.getChildBySlot(nodeIndex, SLOT_LEFT))"))
        assertTrue(handlesCode.contains("public val right: ExpressionHandle"))
        assertTrue(handlesCode.contains("get() = ExpressionHandle(tree.getChildBySlot(nodeIndex, SLOT_RIGHT))"))

        // Check Extensions
        val extCode = result.files.find { it.fileName == "CalculatorExtensions.kt" }!!.content
        assertTrue(extCode.contains("public fun ConcreteSyntaxTree.asCalculatorNode(nodeIndex: Int): CstNode"))
        assertTrue(extCode.contains("public inline fun ConcreteSyntaxBuilder.addBinaryExpression("))
        assertTrue(extCode.contains("public fun ConcreteSyntaxBuilder.addMissingBinaryExpression("))
        assertTrue(extCode.contains("public fun ConcreteSyntaxBuilder.addMissingNumberToken("))
    }
}
