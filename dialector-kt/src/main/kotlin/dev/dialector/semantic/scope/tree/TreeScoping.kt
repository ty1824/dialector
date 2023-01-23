package dev.dialector.semantic.scope.tree

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeClause

/**
 * Represents an object contained within a scope, its canonical name, and optionally a list of alternate identifiers.
 */
data class ScopeElement<out T : Node>(val content: T, val name: String, val alternateIdentifiers: Sequence<String>)

/**
 * Defines [ScopeElement]s that are always in scope at the root of a program AST.
 * Elements may be filtered out or modified by [ScopeTransformationRule]s
 */
interface GlobalScopeContribution<V : Node> {
    val getContribution: () -> Sequence<ScopeElement<V>>
}

/**
 * A rule that defines a scope contribution for a [Node].
 * Scope contributions impact the current [Node] and all of its children.
 *
 * [ScopeTransformationRule]s are applied prior to [ScopeContributionRule]s
 */
interface ScopeContributionRule<T : Node, V : Node> {
    val isValidFor: NodeClause<T>
    val getContribution: T.() -> Sequence<ScopeElement<V>>
}


/**
 * A rule that defines a scope transformation for a [Node].
 * Transformations impact the current [Node] and all of its children.
 *
 * [ScopeTransformationRule]s are applied prior to [ScopeContributionRule]s
 */
interface ScopeTransformationRule<T : Node> {
    val isValidFor: NodeClause<T>
    val transformScope: T.(incomingScope: Sequence<ScopeElement<Node>>) -> Sequence<ScopeElement<Node>>
}

//infix fun <T : Node> NodeClause<T>

fun getPathToRoot(node: Node): List<Node> {
    var current: Node = node
    val result = mutableListOf(node)
    while (current.parent != null) {
        result += current
        current = current.parent!!
    }
    return result.toList()
}

interface ScopingConfiguration {
    /**
     *
     */
    val globalContributionRules: List<GlobalScopeContribution<Node>>
    val contributionRules: List<ScopeContributionRule<Node, Node>>
    val transformationRules: List<ScopeTransformationRule<Node>>
}

/**
 * A scope resolver with a naive scope traversal implementation that performs no caching.
 */
class SimpleScopeResolver(val configuration: ScopingConfiguration) {

    fun getScope(node: Node): Sequence<ScopeElement<Node>> {
        val pathToRoot = getPathToRoot(node).reversed().toMutableList()

        var currentScope: Sequence<ScopeElement<Node>> = sequenceOf()

        // Include global scope
        for (rule in configuration.globalContributionRules) {
            currentScope += rule.getContribution()
        }

        while (pathToRoot.isNotEmpty()) {
            val currentNode = pathToRoot.removeFirst()

            // Apply transformations to existing scope
            for (rule in configuration.transformationRules) with(rule) {
                if (isValidFor(currentNode)) {
                    currentScope = currentNode.transformScope(currentScope)
                }
            }

            // Add contributions
            for (rule in configuration.contributionRules) with(rule) {
                if (isValidFor(currentNode)) {
                    currentScope += currentNode.getContribution()
                }
            }
        }

        return currentScope
    }

    inline fun <reified T : Node> getFilteredScope(node: Node): Sequence<ScopeElement<T>> {

        @Suppress("UNCHECKED_CAST")
        return getScope(node).filter { it.content is T } as Sequence<ScopeElement<T>>
    }
}