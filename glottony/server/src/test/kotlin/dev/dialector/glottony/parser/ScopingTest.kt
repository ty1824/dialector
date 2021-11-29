package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.block
import dev.dialector.glottony.ast.blockExpression
import dev.dialector.glottony.ast.functionDeclaration
import dev.dialector.glottony.ast.numberType
import dev.dialector.glottony.ast.parameter
import dev.dialector.glottony.ast.referenceExpression
import dev.dialector.glottony.ast.returnStatement
import dev.dialector.glottony.ast.stringLiteral
import dev.dialector.glottony.ast.stringType
import dev.dialector.glottony.ast.valStatement
import dev.dialector.syntax.nodeReference
import org.junit.jupiter.api.Test

class ScopingTest {
    @Test
    fun functionDeclarationParameterScopingTest() {
        val node = functionDeclaration {
            name = "foo"
            parameters += parameter {
                name = "param1"
                type = numberType {}
            }
            type = stringType {  }
            body = blockExpression { block = block {
                statements += valStatement {
                    name = "v"
                    type = stringType { }
                    expression = stringLiteral { value = "abc" }
                }
                statements += returnStatement {
                    expression = referenceExpression { target = nodeReference("v") }
                }
            } }
        }
//
//        val scopeGraph = runBlocking { GlottonyScopeGraph.resolveNode(node) }
//
//        val block = (node.body as BlockExpression).block
//        val refExpression = (block.statements[1] as ReturnStatement).expression as ReferenceExpression
//
//        val visibleDeclarations = scopeGraph.getVisibleDeclarations(refExpression.target).toList()
//        assertEquals(2, visibleDeclarations.count(),
//            "Incorrect scope.\nExpected\n${listOf(block.statements[0], node.parameters[0])}\n$visibleDeclarations}")
//
//        println(visibleDeclarations)
//        assertEquals(block.statements[0], refExpression.target.resolve(scopeGraph::resolve))
    }
}