package dev.dialector.syntax

import kotlin.reflect.KProperty

/*
 * Notes:
 *
 * "Augmentations" kinda like Node Attributes? Slots for externally-driven additional content.
 */

/**
 * Represents an element in a program graph.
 *
 * Fields in a node are partitioned into three key types: Properties, Children, and References.
 *
 * Properties are strictly value elements on a node (text, numbers). An example would be the name of a variable.
 * Children are other nodes that belong to this node. An example would be the value expression of a variable.
 * References are pointers to other nodes owned elsewhere in the program. An example would be an expression referencing
 * a variable.
 *
 * Every node maintains a reference to its parent, which is null if the node is a root or has not been added to another
 * node.
 */
public interface Node {
    public var parent: Node?
    public val properties: Map<String, Any?>
    public val children: Map<String, List<Node>>
    public val references: Map<String, NodeReference<*>?>
}

public fun interface ReferenceResolver {
    public fun resolveTarget(reference: NodeReference<*>): Any?
}

/**
 * Resolve a reference using this resolver. If the resolved target is not of the correct type, returns null.
 */
public inline fun <reified T : Node> ReferenceResolver.resolve(reference: NodeReference<T>): T? =
    resolveTarget(reference) as? T

/**
 * Resolve a reference using this resolver. If the resolved target is not of the correct type, returns null.
 */
context(ReferenceResolver)
public inline fun <reified T : Node> NodeReference<T>.resolveTarget(): T? =
    resolveTarget(this) as? T

/**
 * A reference to another [Node]. References must be resolved by an external resolver.
 */
public interface NodeReference<T : Node> {
    /**
     * The [Node] owning this reference
     */
    public val sourceNode: Node

    /**
     * The relation (property) defining this reference
     */
    public val relation: KProperty<NodeReference<T>?>

    /**
     * The identifier of the target [Node]
     */
    public val targetIdentifier: String
}

public data class NodeReferenceImpl<T : Node>(
    override val sourceNode: Node,
    override val relation: KProperty<NodeReference<T>?>,
    override val targetIdentifier: String,
) : NodeReference<T>

/**
 * Retrieves the root of the tree containing this node.
 */
public fun Node.getRoot(): Node = parent?.getRoot() ?: this

/**
 * Retrieves all children of this node.
 */
public fun Node.getAllChildren(): List<Node> = children.values.flatten()

/**
 * Retrieves all references from this node.
 */
public fun Node.getAllReferences(): List<NodeReference<*>> = references.values.filterNotNull().toList()

/**
 * Returns a sequence that iterates through all descendants of this node in a breadth-first traversal.
 */
public fun Node.getAllDescendants(inclusive: Boolean = false): Sequence<Node> = sequence {
    val node = this@getAllDescendants
    if (inclusive) { yield(node) }
    val current: MutableList<Node> = node.getAllChildren().toMutableList()
    while (current.isNotEmpty()) {
        val value = current.removeFirst()
        yield(value)
        current += value.getAllChildren()
    }
}

/**
 * Returns a sequence that iterates through all descendants of this node in a breadth-first traversal, filtered by
 * the given type.
 */
public inline fun <reified T : Node> Node.getDescendants(inclusive: Boolean = false): Sequence<T> =
    this.getAllDescendants(inclusive).filterIsInstance<T>()

/**
 * Returns a sequence that iterates through all ancestors of this node.
 */
public fun Node.getAllAncestors(inclusive: Boolean = false): Sequence<Node> = sequence {
    val node = this@getAllAncestors
    if (inclusive) { yield(node) }
    var current: Node? = node.parent
    while (current != null) {
        yield(current)
        current = current.parent
    }
}

/**
 * Returns a sequence that iterates through all ancestors of this node filtered by the given type.
 */
public inline fun <reified T : Node> Node.getAncestors(inclusive: Boolean = false): Sequence<T> =
    this.getAllAncestors(inclusive).filterIsInstance<T>()
