package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.binaryExpression
import dev.dialector.glottony.ast.block
import dev.dialector.glottony.ast.blockExpression
import dev.dialector.glottony.ast.functionDeclaration
import dev.dialector.glottony.ast.integerLiteral
import dev.dialector.glottony.ast.numberLiteral
import dev.dialector.glottony.ast.returnStatement
import dev.dialector.glottony.ast.stringLiteral
import dev.dialector.glottony.ast.stringType
import dev.dialector.glottony.ast.valStatement
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.glottony.typesystem.IntType
import dev.dialector.glottony.typesystem.NumType
import dev.dialector.glottony.typesystem.StrType
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

        val result = GlottonyTypesystem().inferTypes(node)

        assertEquals(IntType, result[node])
        assertEquals(IntType, result[node.left])
        assertEquals(IntType, result[node.right])
    }

    @Test
    fun nestedExpressionTest() {
        val node = binaryExpression {
            left = binaryExpression {
                left = integerLiteral { value = "1" }
                operator = BinaryOperators.Minus
                right = integerLiteral { value = "2" }
            }
            operator = BinaryOperators.Minus
            right = binaryExpression {
                left = integerLiteral { value = "5"}
                operator = BinaryOperators.Multiply
                right = numberLiteral { value = "10.5"}
            }
        }

        val result = GlottonyTypesystem().inferTypes(node)

        assertEquals(NumType, result[node])
        assertEquals(IntType, result[node.left])
        assertEquals(NumType, result[node.right])
    }

    @Test
    fun functionTypeInferenceTest() {
        val node = functionDeclaration {
            name = "foo"
            type = stringType {  }
            body = blockExpression { block = block {
                statements += valStatement {
                    name = "v"
                    type = stringType { }
                    expression = stringLiteral { value = "abc" }
                }
                statements += returnStatement {
                    expression = stringLiteral { value = "def" }
                }
            } }
        }

        val result = GlottonyTypesystem().inferTypes(node)

        val block = (node.body as BlockExpression).block
        assertEquals(StrType, result[node.body])
        assertEquals(StrType, result[block.statements[0]])
    }

    @Test
    fun functionInvalidReturnTypeTest() {
        val node = functionDeclaration {
            name = "foo"
            type = stringType {  }
            body = blockExpression { block = block {
                statements += valStatement {
                    name = "v"
                    type = stringType { }
                    expression = stringLiteral { value = "abc" }
                }
                statements += returnStatement {
                    expression = numberLiteral { value = "123" }
                }
            } }
        }

        val result = GlottonyTypesystem().inferTypes(node)

        val block = (node.body as BlockExpression).block
        assertEquals(NumType, result[node.body])
        assertEquals(NumType, result[block.statements[0]])
    }
}