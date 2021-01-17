package dev.dialector.glottony.scopes

import dev.dialector.glottony.ast.Block
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.model.Node
import dev.dialector.model.given
import dev.dialector.scoping.Namespace
import dev.dialector.scoping.ScopeTraversalRule
import dev.dialector.scoping.produceScope

object Unqualified : Namespace("unqualified")

object GlottonyScopeGraph {
    val rules: List<ScopeTraversalRule<out Node>> = listOf(
        given<FunctionDeclaration>().produceScope("functionDeclaration") { node, incomingScope ->
            with(newScope().inherit(incomingScope, "parent")) {
                node.parameters.forEach {
                    declare(Unqualified, it, it.name)
                }
                traverse(node.body)
            }
        },
        given<BlockExpression>().produceScope("blockExpression") { node, incomingScope ->
            incomingScope.traverse(node.block)
        },
        given<Block>().produceScope("block") { node, incomingScope ->
            with(newScope().inherit(incomingScope, "parent")) {
                node.statements.forEach {
                    traverse(it)
                }
            }
        },
        given<ValStatement>().produceScope("valStatement") { node, incomingScope ->
            with(incomingScope) {
                traverse(node.expression)
                declare(Unqualified, node, node.name)
            }
        },
        given<ReturnStatement>().produceScope("returnStatement") { node, incomingScope ->
            with(incomingScope) {
                traverse(node.expression)
            }
        },
        given<ReferenceExpression>().produceScope("referenceExpression") { node, incomingScope ->
            incomingScope.reference(Unqualified, node.target, node.target.targetIdentifier)
        }
    )

    
}