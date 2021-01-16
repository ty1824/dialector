package dev.dialector.scoping

import dev.dialector.model.Node
import dev.dialector.model.NodeClause
import dev.dialector.model.NodeReference
import kotlin.coroutines.suspendCoroutine

interface ScopeDescriptor {
    /**
     * Declares that this scope will inherit elements from the given scope.
     *
     * Returns this scope.
     */
    fun inherit(from: ScopeDescriptor, label: String): ScopeDescriptor

    // TODO: Add transformations when inheritng
//    /**
//     * Declares that this scope will inherit elements from the given scope with a transformation applied.
//     */
//    fun ScopeDescriptor.inherit(target: ScopeDescriptor, transformation: (Sequence<ScopeElement<Node>>) -> Sequence<ScopeElement<Node>>)

    /**
     * Adds an element to this scope.
     */
    fun declare(namespace: Namespace, element: Node, identifier: String)

    /**
     * Adds elements to this scope.
     */
    fun declare(namespace: Namespace, elements: Iterable<Pair<Node, String>>)

    /**
     * Aliases the given element in this scope.
     *
     * Aliases are additional names that represent the same target.
     */
    fun alias(namespace: Namespace, element: Node, alias: String)

    /**
     * Declares a NodeReference target as a given identifier.
     */
    fun ScopeDescriptor.reference(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String)

}

abstract class Namespace(name: String)

object Default : Namespace("Default")

@DslMarker
annotation class ScopeGraphDsl

@ScopeGraphDsl
interface ScopeTraversalContext {

    /**
     * Creates a new scope
     */
    fun newScope(): ScopeDescriptor

    /**
     * Evaluates the block with this scope as the context scope.
     */
    suspend fun within(scope: ScopeDescriptor, evaluate: suspend ScopeTraversalContext.(scope: ScopeDescriptor) -> Unit)

    /**
     * Traverses to the given node to continue evaluating scope.
     *
     * WARNING: This should generally be used with direct children of the current node. Use otherwise may cause
     * performance issues or nondeterministic evaluation.
     */
    suspend fun ScopeDescriptor.traverse(node: Node)
}

/**
 * Defines a rule that modifies how the AST is traversed to produce a scope.
 */
interface ScopeTraversalRule<T : Node> {
    val isValidFor: NodeClause<T>
    val traverse: suspend ScopeTraversalContext.(node: T, scope: ScopeDescriptor) -> Unit
}

interface ScopeResolver {
    fun getResolvedTarget(reference: NodeReference<*>): Node?
    fun getVisibleDeclarations(node: Node): Sequence<Pair<Node, String>>
}

class SingleRootScopeGraph(val root: Node) : ScopeResolver {
    val targets: Map<NodeReference<*>, Node>
    init {
        val targetsInit = mutableMapOf<NodeReference<out Node>, Node>()

        targets = targetsInit.toMap()
    }

    override fun getResolvedTarget(reference: NodeReference<*>): Node? = targets[reference]

    override fun getVisibleDeclarations(node: Node): Sequence<Pair<Node, String>> {
        TODO("Not yet implemented")
    }
}

class SimpleScopeDescriptor : ScopeDescriptor {
    val inheritedScopes: MutableMap<SimpleScopeDescriptor, String> = mutableMapOf()
    val declarations: MutableMap<Namespace, MutableList<Pair<Node, String>>> = mutableMapOf()
    val references: MutableMap<NodeReference<*>, Node> = mutableMapOf()

    override fun inherit(from: ScopeDescriptor, label: String): ScopeDescriptor {
        inheritedScopes[from as SimpleScopeDescriptor] = label
        return this
    }

    override fun declare(namespace: Namespace, element: Node, identifier: String) {
        declarations.computeIfAbsent(namespace) { mutableListOf() } += element to identifier
    }

    override fun declare(namespace: Namespace, elements: Iterable<Pair<Node, String>>) {
        declarations.computeIfAbsent(namespace) { mutableListOf() } += elements
    }

    override fun alias(namespace: Namespace, element: Node, alias: String) {
        TODO("Not yet implemented")
    }

    override fun ScopeDescriptor.reference(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String) {
        // If the reference exists, ignore.
        if (!references.contains(reference)) {
            val element = findElement(namespace, targetIdentifier)
            // If the target was found, bind it.
            if (element != null) references[reference] = element
        }
    }

    fun findElement(namespace: Namespace, identifier: String): Node? =
        sequence {
            val remaining = mutableListOf(this@SimpleScopeDescriptor)
            while (remaining.isNotEmpty()) {
                val current = remaining.removeFirst()
                yieldAll(current.declarations[namespace].orEmpty())
                remaining.addAll(current.inheritedScopes.keys)
            }
        }.firstOrNull { (_, string) -> string == identifier}?.first
}

class SimpleScopeTraversalContext(val rules: List<ScopeTraversalRule<Node>>) : ScopeTraversalContext {

    override fun newScope(): ScopeDescriptor = SimpleScopeDescriptor()

    override suspend fun within(scope: ScopeDescriptor, evaluate: suspend ScopeTraversalContext.(scope: ScopeDescriptor) -> Unit) {
        evaluate(scope)
    }

    override suspend fun ScopeDescriptor.traverse(node: Node) {
        for (rule in rules) with(rule) {
            if (isValidFor(node)) traverse(node, this@traverse)
        }
    }
}

suspend fun ScopeTraversalContext.foo(node: Node) {
    val scopeOne = newScope()

    within(newScope().inherit(scopeOne, "parent")) {

    }



}