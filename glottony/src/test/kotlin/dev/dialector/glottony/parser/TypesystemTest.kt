package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.binaryExpression
import dev.dialector.glottony.ast.integerLiteral
import dev.dialector.glottony.ast.numberType
import dev.dialector.glottony.typesystem.GlottonyTypeInferenceContext
import dev.dialector.glottony.typesystem.IntType
import dev.dialector.glottony.typesystem.NumType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TypesystemTest {
    @Test
    fun basicTest() {
        val node = binaryExpression {
            left = integerLiteral { value = "5" }
            operator = BinaryOperators.Minus
            right = integerLiteral { value = "10" }
        }

        val inferenceContext = GlottonyTypeInferenceContext()
        val result = inferenceContext.inferTypes(node)

        assertEquals(NumType, result[node])
        assertEquals(IntType, result[node.left])
        assertEquals(IntType, result[node.right])
    }
}