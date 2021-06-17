package dev.dialector.glottony.scopes

import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.Block
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.MemberAccessExpression
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.model.Node
import dev.dialector.model.given
import dev.dialector.scoping.LinearScopeGraph
import dev.dialector.scoping.Namespace
import dev.dialector.scoping.ScopeGraph
import dev.dialector.scoping.ScopeTraversalRule
import dev.dialector.scoping.TypeScopingRule
import dev.dialector.scoping.produceScope
import dev.dialector.typesystem.NodeType
import dev.dialector.typesystem.Type

object Unqualified : Namespace("unqualified")
object Declarations : Namespace("declarations")

class GlottonyScopeGraph {
    val typeRules: List<TypeScopingRule<out Type>> = listOf(

    )

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