package dev.dialector.glottony.scopes

import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.Block
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.MemberAccessExpression
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.semantic.PropagationType
import dev.dialector.semantic.scope.LinearScopeGraph
import dev.dialector.semantic.scope.Namespace
import dev.dialector.semantic.scope.ScopeGraph
import dev.dialector.semantic.scope.ScopeTraversalRule
import dev.dialector.semantic.scope.produceScope
import dev.dialector.semantic.type.NodeType
import dev.dialector.syntax.Node
import dev.dialector.syntax.given

object Unqualified : Namespace("unqualified")
object Declarations : Namespace("declarations")

object OutgoingScope : PropagationType

class GlottonyScopeGraph {
    val traversalRules: List<ScopeTraversalRule<out Node>> = listOf(
        given<FunctionDeclaration>().produceScope("functionDeclaration") { node, incomingScope ->
            with(newScope().inherit(incomingScope, "parent")) {
                node.parameters.forEach {
                    declare(Declarations, it, it.name)
                }
                traverse(node.body, this)
            }
        },
        given<BlockExpression>().produceScope("blockExpression") { node, incomingScope ->
            traverse(node.block, incomingScope)
        },
        given<Block>().produceScope("block") { node, incomingScope ->
            with(newScope().inherit(incomingScope, "parent")) {
                node.statements.forEach {
                    traverse(it, this)
                }
            }
        },
        given<ValStatement>().produceScope("valStatement") { node, incomingScope ->
            with(incomingScope) {
                traverse(node.expression, this)
                declare(Declarations, node, node.name)
            }
        },
        given<ReturnStatement>().produceScope("returnStatement") { node, incomingScope ->
            with(incomingScope) {
                traverse(node.expression, this)
            }
        },
        given<ReferenceExpression>().produceScope("referenceExpression") { node, incomingScope ->
            incomingScope.reference(Declarations, node.target, node.target.targetIdentifier)
        },
        given<MemberAccessExpression>().produceScope("memberAccessExpression") { node, incomingScope ->
            val contextType = this.semantics.query(NodeType, node.context)
            with(newScope().inherit(incomingScope, "parent")) {

                node.context
            }
        }
    )

    suspend fun resolveRoot(root: GlottonyRoot): ScopeGraph {
        return LinearScopeGraph.invoke(root.rootNode, traversalRules)
    }

    internal suspend fun resolveNode(node: Node): ScopeGraph {
        return LinearScopeGraph.invoke(node, traversalRules)
    }
}