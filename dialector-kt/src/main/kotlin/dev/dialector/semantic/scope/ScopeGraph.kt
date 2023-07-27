package dev.dialector.semantic.scope

import dev.dialector.semantic.SemanticAnalysisContext
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.ReferenceResolver

/**
 * Are there two different types of scoping? Flow-based scope evaluation vs
 */

/*
TODO: Handle the difference between scopes that need to be cached cross-root vs ones that can be computed within a root.
 */

interface ScopeDescriptor {
    /**
     * Declares that this scope will inherit elements from the given scope.
     *
     * Returns this scope.
     */
    fun inherit(from: ScopeDescriptor, label: String): ScopeDescriptor

    // TODO: Add transformations when inheriting
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
    fun alias(namespace: Namespace, identifier: String, aliasNamespace: Namespace, alias: String)

    /**
     * Declares a NodeReference target as a given identifier.
     */
    fun reference(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String)
}

abstract class Namespace(name: String)

object Default : Namespace("Default")

interface ScopeGraph : ReferenceResolver {
    fun getVisibleDeclarations(reference: NodeReference<*>): Sequence<Pair<Node, String>>
}

class SingleRootScopeGraph private constructor(
    private val targets: Map<NodeReference<*>, Node>,
    private val visibleElements: Map<NodeReference<*>, Sequence<Pair<Node, String>>>,
) : ScopeGraph {
    companion object {
        suspend operator fun invoke(root: Node, rules: List<ScopeTraversalRule<out Node>>): SingleRootScopeGraph {
            val targetsInit = mutableMapOf<NodeReference<out Node>, Node>()
            val visibleElementsInit = mutableMapOf<NodeReference<*>, Sequence<Pair<Node, String>>>()

            val context = SimpleScopeTraversalContext({
                SimpleScopeDescriptor { namespace, reference, targetIdentifier ->
                    visibleElementsInit[reference] = (this as SimpleScopeDescriptor).visibleElements(namespace)
                    val element = visibleElementsInit[reference]?.first { it.second == targetIdentifier }?.first
                    if (element != null) targetsInit[reference] = element
                }
            }, { node, scope ->
                if (rules.any { it.isValidFor(node) }) {
                    rules.forEach {
                        it(this, node, scope)
                    }
                } else {
                    node.references.values.filterNotNull().forEach { scope.reference(Default, it, it.targetIdentifier) }
                    node.children.forEach { child -> child.value.forEach { traverse(it, scope) } }
                }
            })

            val globalScope = context.newScope()

            with(context) {
                traverse(root, globalScope)
            }

            return SingleRootScopeGraph(targetsInit.toMap(), visibleElementsInit.toMap())
        }
    }

    override fun resolveTarget(reference: NodeReference<*>): Node? =
        targets[reference]

    override fun getVisibleDeclarations(reference: NodeReference<*>): Sequence<Pair<Node, String>> =
        visibleElements[reference].orEmpty()
}

typealias ReferenceHandler = ScopeDescriptor.(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String) -> Unit
class SimpleScopeDescriptor(private val onReference: ReferenceHandler) : ScopeDescriptor {
    private val inheritedScopes: MutableMap<SimpleScopeDescriptor, String> = mutableMapOf()
    private val declarations: MutableMap<Namespace, MutableList<Pair<Node, String>>> = mutableMapOf()
    private val references: MutableMap<NodeReference<*>, Node> = mutableMapOf()

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

    override fun alias(namespace: Namespace, identifier: String, aliasNamespace: Namespace, alias: String) {
        val element = findElement(namespace, identifier)
        if (element != null) {
            declare(aliasNamespace, element, alias)
        }
    }

    override fun reference(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String) {
        // If the reference exists, ignore.
        if (!references.contains(reference)) {
            val element = findElement(namespace, targetIdentifier)
            // If the target was found, bind it.
            if (element != null) references[reference] = element
        }
        onReference(namespace, reference, targetIdentifier)
    }

    fun findElement(namespace: Namespace, identifier: String): Node? =
        visibleElements(namespace).firstOrNull { (_, string) -> string == identifier }?.first

    fun visibleElements(namespace: Namespace): Sequence<Pair<Node, String>> =
        sequence {
            val remaining = mutableListOf(this@SimpleScopeDescriptor)
            while (remaining.isNotEmpty()) {
                val current = remaining.removeFirst()
                yieldAll(current.declarations[namespace].orEmpty())
                remaining.addAll(current.inheritedScopes.keys)
            }
        }
}

/*
Linear scoping - every reference to a scope is tagged with an index. Lookups will begin from the index rather than
looking through the entire scope. In this way, each scope actually represents a small linear subgraph, without the
memory overhead of creating each individual node.
 */

class LinearScopeDescriptor(private val onReference: ReferenceHandler) : ScopeDescriptor {
    private interface Declaration {
        val namespace: Namespace
        val node: Node
        val identifier: String
    }
    private data class ExplicitDeclaration(
        override val namespace: Namespace,
        override val node: Node,
        override val identifier: String,
    ) : Declaration

    private data class AliasDeclaration(
        val forDeclaration: Declaration,
        override val namespace: Namespace,
        override val identifier: String,
    ) : Declaration {
        override val node: Node
            get() = forDeclaration.node
    }

    private data class IncomingReference(
        val namespace: Namespace,
        val index: Int,
    )

    private val declarations: MutableList<Declaration> = mutableListOf()

    private val inheritedScopes: MutableList<LinearScopeDescriptor> = mutableListOf()
    private val inheritingScopes: MutableMap<LinearScopeDescriptor, Int> = mutableMapOf()

    private val incomingReferences: MutableMap<NodeReference<*>, IncomingReference> = mutableMapOf()

    private fun getCurrentIndex(): Int = this.declarations.size

    private fun addInheritingScope(inheriting: LinearScopeDescriptor) {
        inheritingScopes[inheriting] = getCurrentIndex()
    }

    override fun inherit(from: ScopeDescriptor, label: String): ScopeDescriptor {
        if (from is LinearScopeDescriptor) {
            inheritedScopes += from
            from.addInheritingScope(this)
        } else {
            throw RuntimeException("Must be LinearScopeDescriptor: $from")
        }

        return this
    }

    override fun declare(namespace: Namespace, element: Node, identifier: String) {
        declarations += ExplicitDeclaration(namespace, element, identifier)
    }

    override fun declare(namespace: Namespace, elements: Iterable<Pair<Node, String>>) {
        declarations += elements.map { ExplicitDeclaration(namespace, it.first, it.second) }
    }

    override fun alias(namespace: Namespace, identifier: String, aliasNamespace: Namespace, alias: String) {
        val declaration = findDeclaration(namespace, getCurrentIndex(), identifier)
        if (declaration != null) {
            declarations += AliasDeclaration(declaration, aliasNamespace, alias)
        }
    }

    override fun reference(namespace: Namespace, reference: NodeReference<out Node>, targetIdentifier: String) {
        // If the reference exists, ignore.
        if (!incomingReferences.contains(reference)) {
            incomingReferences[reference] = IncomingReference(namespace, getCurrentIndex())
        }
        onReference(namespace, reference, targetIdentifier)
    }

    private fun findDeclaration(namespace: Namespace, index: Int, identifier: String): Declaration? =
        visibleDeclarations(namespace, index).firstOrNull { it.identifier == identifier }

    private fun visibleDeclarations(namespace: Namespace, index: Int): Sequence<Declaration> = sequence {
        yieldAll(declarations.take(index).asReversed())
        val remaining = inheritedScopes.map { it to this@LinearScopeDescriptor }.toMutableList()
        while (remaining.isNotEmpty()) {
            val (current, inheriting) = remaining.removeFirst()
            val index = current.inheritingScopes[inheriting]!!
            yieldAll(current.declarations.take(index).asReversed())
            remaining += current.inheritedScopes.map { it to current }
        }
    }

    fun getReferenceTarget(reference: NodeReference<*>): Node? =
        with(incomingReferences[reference]!!) {
            findElement(namespace, index, reference.targetIdentifier)
        }

    fun getReferenceableElements(reference: NodeReference<*>): Sequence<Pair<Node, String>> =
        with(incomingReferences[reference]!!) {
            visibleElements(namespace, index)
        }

    fun findElement(namespace: Namespace, index: Int, identifier: String): Node? =
        findDeclaration(namespace, index, identifier)?.node

    fun visibleElements(namespace: Namespace, index: Int): Sequence<Pair<Node, String>> =
        visibleDeclarations(namespace, index).map { it.node to it.identifier }
}

class SimpleScopeTraversalContext(
    val createScope: () -> ScopeDescriptor,
    val onTraversal: suspend ScopeTraversalContext.(node: Node, scope: ScopeDescriptor) -> Unit,
) : ScopeTraversalContext {

    override val semantics: SemanticAnalysisContext =
        TODO("Simple traversal does not support semantics")

    override fun newScope(): ScopeDescriptor = createScope()

    override suspend fun traverse(node: Node, scope: ScopeDescriptor) = onTraversal(node, scope)
}

class LinearScopeGraph private constructor(
    private val referenceScopes: Map<NodeReference<*>, LinearScopeDescriptor>,
) : ScopeGraph {
    companion object {
        suspend operator fun invoke(root: Node, rules: List<ScopeTraversalRule<out Node>>): LinearScopeGraph {
            val referenceScopes = mutableMapOf<NodeReference<out Node>, LinearScopeDescriptor>()

            val context = SimpleScopeTraversalContext({
                LinearScopeDescriptor { namespace, reference, targetIdentifier ->
                    referenceScopes[reference] = this as LinearScopeDescriptor
                }
            }, { node, scope ->
                if (rules.any { it.isValidFor(node) }) {
                    rules.forEach {
                        it(this, node, scope)
                    }
                } else {
                    node.references.values.filterNotNull().forEach { scope.reference(Default, it, it.targetIdentifier) }
                    node.children.forEach { child -> child.value.forEach { traverse(it, scope) } }
                }
            })

            val globalScope = context.newScope()

            with(context) {
                traverse(root, globalScope)
            }

            return LinearScopeGraph(referenceScopes.toMap())
        }
    }

    override fun resolveTarget(reference: NodeReference<*>): Node? =
        referenceScopes[reference]?.getReferenceTarget(reference)

    override fun getVisibleDeclarations(reference: NodeReference<*>): Sequence<Pair<Node, String>> =
        referenceScopes[reference]?.getReferenceableElements(reference).orEmpty()
}
//
// suspend fun ScopeTraversalContext.foo(node: Node) {
//    val scopeOne = newScope()
//
//    with(newScope().inherit(scopeOne, "parent")) {
//        declare(Default, node, "hi")
//        alias(Default, "hi", Default, "bye")
//        reference(Default, object : NodeReference<Node> {}, "hi")
//        traverse(node)
//    }
// }
