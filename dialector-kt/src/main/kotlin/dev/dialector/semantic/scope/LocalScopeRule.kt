package dev.dialector.semantic.scope

import dev.dialector.semantic.type.Type
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodePredicate

public data class ScopeType(val name: String)

public interface LocalScopeContributionRule<T : Node, C> {
    public val isValidFor: NodePredicate<T, C>

    public fun scoping(node: T, context: C, scopeContributionContext: LocalScopeContributionContext)
}

@Suppress("UNCHECKED_CAST")
public fun <T : Node, C> LocalScopeContributionRule<T, C>.evaluate(
    candidate: Type,
    context: C,
    scopeContributionContext: LocalScopeContributionContext
): Unit {
    if (isValidFor(candidate, context)) {
        this.scoping(candidate as T, context, scopeContributionContext)
    }
}

public interface LocalScopeContributionContext {

    public fun scopeFor(node: Node, type: ScopeType)


}

public interface ScopeReference

public interface ScopeDescriptor : ScopeReference {
    public fun declare(node: Node, name: String)

    public fun declareAll(elements: Iterable<Pair<Node, String>>) {
        elements.forEach { declare(it.first, it.second) }
    }

    public fun inherit(label: String, reference: ScopeReference)
}