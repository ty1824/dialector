package dev.dialector.scoping

import dev.dialector.model.Node
import dev.dialector.model.NodeReference
import dev.dialector.resolution.Query
import dev.dialector.resolution.SemanticDataDefinition
import dev.dialector.resolution.SemanticSystem
import dev.dialector.resolution.SemanticSystemDefinition

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