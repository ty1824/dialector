package dev.dialector.semantic.scope

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import dev.dialector.semantic.Query
import dev.dialector.semantic.SemanticDataDefinition
import dev.dialector.semantic.SemanticSystem
import dev.dialector.semantic.SemanticSystemDefinition

interface ScopeElement {
    val name: String
    val target: Node
}

object ScopeSystemDefinition : SemanticSystemDefinition<ScopeSystem>() {
    val ReferencedNode = SemanticDataDef<NodeReference<*>, Node> { system, argument -> TODO("") }
}

object ReferencedNode : SemanticDataDefinition<ScopeSystem, NodeReference<*>, Node>(ScopeSystemDefinition) {
    override fun query(system: ScopeSystem, argument: NodeReference<*>): Query<NodeReference<*>, Node> {
        // TODO: This implementation will need to call some function in ScopeSystem that can create a Query
        // for a given NodeReference and can wait until scopes have been computed to resolve.
        // system.resolve(argument)
        TODO("Not yet implemented")
    }
}

object ReferenceScope : SemanticDataDefinition<ScopeSystem, NodeReference<*>, Sequence<Pair<Node, String>>>(ScopeSystemDefinition) {
    override fun query(system: ScopeSystem, argument: NodeReference<*>): Query<NodeReference<*>, Sequence<Pair<Node, String>>> {
        TODO("Not yet implemented") //system.getVisibleDeclarations(argument)
    }
}

interface ScopeSystem : SemanticSystem {
    fun getVisibleDeclarations(reference: NodeReference<*>): Sequence<Pair<Node, String>>

    fun <T : Node> resolve(reference: NodeReference<T>): T?
}