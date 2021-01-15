package dev.dialector.scoping

import dev.dialector.model.Node
import dev.dialector.model.NodeClause
import dev.dialector.model.NodeReference

interface Scope

/**
 * Represents an object contained within a scope, its canonical name, and optionally a list of alternate identifiers.
 */
data class ScopeElement<out T : Node>(val content: T, val name: String, val alternateIdentifiers: Sequence<String>)

interface ScopeTraversalContext {
    /**
     * The current context scope.
     */
    val contextScope: Scope

    /**
     * Creates a new scope
     */
    fun newScope(): Scope

    /**
     * Creates a new scope and evaluates the block with the new scope as the context scope.
     */
    fun newScope(withinScope: ScopeTraversalContext.() -> Unit): Scope

    /**
     * Evaluates the block with this scope as the context scope.
     */
    fun withContext(scope: Scope, evaluate: ScopeTraversalContext.() -> Unit)

    /**
     * Declares that this scope will inherit elements from the given scope.
     */
    fun Scope.inherit(target: Scope = contextScope)

    /**
     * Declares that this scope will inherit elements from the given scope with a transformation applied.
     */
    fun Scope.inherit(target: Scope = contextScope, transformation: (Sequence<ScopeElement<Node>>) -> Sequence<ScopeElement<Node>>)

    /**
     * Adds an element to the [contextScope].
     */
    fun declare(element: ScopeElement<Node>)

    /**
     * Adds an element to this scope.
     */
    fun Scope.declare(element: ScopeElement<Node>)

    /**
     * Adds elements to the [contextScope].
     */
    fun declare(elements: Iterable<ScopeElement<Node>>)

    /**
     * Adds elements to this scope.
     */
    fun Scope.declare(elements: Iterable<ScopeElement<Node>>)

    /**
     * Aliases the element in the [contextScope].
     */
    fun alias(element: ScopeElement<Node>, alias: String)

    /**
     *
     */
    infix fun NodeReference<out Node>.references(name: String)

    /**
     * Traverses to the given node to continue evaluating scope.
     *
     * WARNING: This should generally be used with direct children of the current node. Use otherwise may cause
     * performance issues or nondeterministic evaluation.
     */
    suspend fun traverse(node: Node)
}

/**
 * Defines a rule that modifies how the AST is traversed to produce a scope.
 */
interface ScopeTraversalRule<T : Node> {
    val isValidFor: NodeClause<T>
    val traverse: suspend ScopeTraversalContext.(node: T) -> Unit
}

interface ScopeResolver {
    fun getResolvedTarget(node: NodeReference<*>): Node?
    fun getVisibleDeclarations(node: Node): Sequence<ScopeElement<Node>>
}

class SimpleRootScopeResolver(val root: Node) : ScopeResolver {

    val targets: Map<NodeReference<*>, Node>
    init {
        val targetsInit = mutableMapOf<NodeReference<out Node>, Node>()

        targets = mutableMapOf()
    }

    override fun getResolvedTarget(reference: NodeReference<*>): Node? = targets[reference]

    override fun getVisibleDeclarations(node: Node): Sequence<ScopeElement<Node>> {
        TODO("Not yet implemented")
    }
}

class SimpleScopeTraversalContext : ScopeTraversalContext {
    class SimpleScope : Scope

    override var contextScope: Scope = SimpleScope()

    override fun newScope(): Scope = SimpleScope()

    override fun newScope(withinScope: ScopeTraversalContext.() -> Unit): Scope {
        val newScope = SimpleScope()
        withContext(newScope) { withinScope() }
        val oldScope = contextScope
        return newScope
    }

    override fun withContext(scope: Scope, withinScope: ScopeTraversalContext.() -> Unit) {
        val oldScope = contextScope
        contextScope = scope
        withinScope()
        contextScope = oldScope
    }

    override fun Scope.inherit(target: Scope) {
        TODO("Not yet implemented")
    }

    override fun Scope.inherit(target: Scope, transformation: (Sequence<ScopeElement<Node>>) -> Sequence<ScopeElement<Node>>) {
        TODO("Not yet implemented")
    }

    override fun declare(element: ScopeElement<Node>) {
        TODO("Not yet implemented")
    }

    override fun Scope.declare(element: ScopeElement<Node>) {
        TODO("Not yet implemented")
    }

    override fun declare(elements: Iterable<ScopeElement<Node>>) {
        TODO("Not yet implemented")
    }

    override fun Scope.declare(elements: Iterable<ScopeElement<Node>>) {
        TODO("Not yet implemented")
    }

    override fun alias(element: ScopeElement<Node>, alias: String) {
        TODO("Not yet implemented")
    }

    override fun NodeReference<out Node>.references(name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun traverse(node: Node) {
        TODO("Not yet implemented")
    }
}

fun ScopeTraversalContext.foo(node: Node) {
    val scopeOne = newScope()

    newScope() {

    }.inherit()



}

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

/**
 * A scope resolver with a naive scope traversal implementation that performs no caching.
 */
class SimpleScopeResolver(
    val globalContributionRules: List<GlobalScopeContribution<Node>>,
    val contributionRules: List<ScopeContributionRule<Node, Node>>,
    val transformationRules: List<ScopeTransformationRule<Node>>
) {

    fun getScope(node: Node): Sequence<ScopeElement<Node>> {
        val pathToRoot = getPathToRoot(node).reversed().toMutableList()

        var currentScope: Sequence<ScopeElement<Node>> = sequenceOf()

        // Include global scope
        for (rule in globalContributionRules) {
            currentScope += rule.getContribution()
        }

        while (pathToRoot.isNotEmpty()) {
            val currentNode = pathToRoot.removeFirst()

            // Apply transformations to existing scope
            for (rule in transformationRules) with(rule) {
                if (isValidFor(currentNode)) {
                    currentScope = currentNode.transformScope(currentScope)
                }
            }

            // Add contributions
            for (rule in contributionRules) with(rule) {
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