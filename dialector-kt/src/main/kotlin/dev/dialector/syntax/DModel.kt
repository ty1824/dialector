package dev.dialector.syntax

import kotlin.reflect.KProperty

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
    /**
     * The parent of this [Node]. If null, this node represents the root of an AST, but may not be a top-level root.
     */
    public var parent: Node?

    /**
     * Properties are value-based members of a [Node].
     * These represent any arbitrary data, but should not include other nodes.
     */
    public val properties: Map<String, Any?>

    /**
     * Children are other [Node]s that are "owned" by this [Node].
     */
    public val children: Map<String, List<Node>>

    /**
     * References point to other [Node]s in the program graph.
     */
    public val references: Map<String, NodeReference<*>?>

    /**
     *  Used for internal implementation of deep copy. Generated copy() will handle type casting
     *  This will create a deep copy of the node and all of its descendants without the parent reference.
     */
    public fun copyAsNode(): Node = throw UnsupportedOperationException(
        "copyAsNode() is not implemented for ${this::class.simpleName}. " +
                "Use @NodeDefinition to generate an implementation"
    )


    /**
     * A debug-friendly string representation for this node.
     *
     * This is provided as interfaces (used to define `Node` subtypes) cannot override `toString themselves.
     */
    public fun toDebugString(): String = "${this::class.simpleName}@${System.identityHashCode(this)}"
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
     * TODO: Refactor to a string
     */
    public val relation: KProperty<NodeReference<T>?>

    /**
     * The identifier of the target [Node]
     */
    public val targetIdentifier: String
}

public class NodeReferenceImpl<T : Node>(
    override val sourceNode: Node,
    override val relation: KProperty<NodeReference<T>?>,
    override val targetIdentifier: String,
) : NodeReference<T> {
    override fun toString(): String = "nodeRef($sourceNode::${relation.name} -> $targetIdentifier)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NodeReference<*>) return false

        return sourceNode == other.sourceNode && relation == other.relation && targetIdentifier == other.targetIdentifier
    }

    override fun hashCode(): Int {
        var result = sourceNode.hashCode()
        result = 31 * result + relation.hashCode()
        result = 31 * result + targetIdentifier.hashCode()
        return result
    }
}

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
