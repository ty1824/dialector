package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.binaryExpression
import dev.dialector.glottony.ast.integerLiteral
import dev.dialector.glottony.interpreter.GlottonyInterpreter
import dev.dialector.glottony.interpreter.InterpreterContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InterpreterTest {
    @Test
    fun basicInterpreterTest() {
        val node = binaryExpression {
            left = integerLiteral { value = "5" }
            operator = BinaryOperators.Plus
            right = integerLiteral { value = "10" }
        }

        val result = GlottonyInterpreter.visit(node, object : InterpreterContext {})

        assertEquals(15, result)
    }
}