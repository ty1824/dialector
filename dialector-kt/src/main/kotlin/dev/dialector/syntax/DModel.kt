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
    public val references: Map<String, NodeReference<*>>
}

public interface ReferenceResolver {
    public fun <T : Node> resolve(reference: NodeReference<T>): T?
}

/**
 * A context that provides a resolution mechanism for [NodeReference]s
 */
public interface ReferenceResolutionContext {
    public fun <T : Node> NodeReference<T>.resolve(): T?
}

/**
 * A reference to another [Node]. References must be resolved by an external resolver.
 */
public interface NodeReference<T : Node> {
    public val sourceNode: Node
    public val targetIdentifier: String
}

internal class SimpleNodeReference<T : Node>(override val targetIdentifier: String) : NodeReference<T> {
    override lateinit var sourceNode: Node
}

public fun <T : Node> nodeReference(targetIdentifier: String): NodeReference<T> = SimpleNodeReference<T>(targetIdentifier)

/**
 * Retrieves the root of the tree containing this node.
 */
public fun Node.getRoot(): Node = parent?.getRoot() ?: this

/**
 * Retrieves the Node property defined by the given [KProperty]
 */
@Suppress("UNCHECKED_CAST")
public fun <T> Node.getProperty(property: KProperty<T>): T =
    properties[property.name] as T

/**
 * Retrieves the list of children as defined by the given property.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Node> Node.getChildren(child: KProperty<List<T>>): List<T> =
    children[child.name] as List<T>

/**
 * Retrieves the reference defined by the given property.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Node> Node.getReference(relation: KProperty<NodeReference<T>>): NodeReference<T> =
    references[relation.name] as NodeReference<T>

/**
 * Retrieves all children of this node.
 */
public fun Node.getAllChildren(): List<Node> = children.values.flatten()

/**
 * Retrieves all references from this node.
 */
public fun Node.getAllReferences(): List<NodeReference<*>> = references.values.toList()

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
public inline fun <reified T : Node> Node.getDescendants(inclusive: Boolean = false): Sequence<Node> =
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
