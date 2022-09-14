package dev.dialector.syntax

import kotlin.reflect.KClass
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
interface Node {
    val parent: Node?
    val properties: Map<String, Any?>
    val children: Map<String, List<Node>>
    val references: Map<String, NodeReference<*>>
}

interface ReferenceResolver {
    fun <T : Node> resolve(reference: NodeReference<T>): T?
}

/**
 * A context that provides a resolution mechanism for [NodeReference]s
 */
interface ReferenceResolutionContext {
    fun <T : Node> NodeReference<T>.resolve(): T?
}

/**
 * A reference to another [Node]. References must be resolved by an external resolver.
 */
interface NodeReference<T : Node> {
    val sourceNode: Node

    val targetIdentifier: String
}

internal class SimpleNodeReference<T : Node>(override val targetIdentifier: String) : NodeReference<T> {
    override lateinit var sourceNode: Node
}

fun <T : Node> nodeReference(targetIdentifier: String): NodeReference<T> = SimpleNodeReference<T>(targetIdentifier)

/**
 * Retrieves the root of the tree containing this node.
 */
fun Node.getRoot(): Node = parent?.getRoot() ?: this

@Suppress("UNCHECKED_CAST")
fun <T> Node.getProperty(property: KProperty<T>): T =
    properties[property.name] as T

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getChildren(child: KProperty<List<T>>): List<T> =
    children[child.name] as List<T>

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getReferences(relation: KProperty<NodeReference<T>>): NodeReference<T> =
    references[relation.name] as NodeReference<T>

fun Node.getAllChildren() : List<Node> = children.values.flatten()

fun Node.getAllReferences() : List<NodeReference<*>> = references.values.toList()

fun Node.getDescendants(inclusive: Boolean = false, ofType: KClass<out Node>) : Sequence<Node> = sequence {
    val node = this@getDescendants
    if (inclusive && ofType.isInstance(node)) { yield (node) }
    val current: MutableList<Node> = if (inclusive) mutableListOf(node) else node.getAllChildren().toMutableList()
    while (current.isNotEmpty()) {
        val value = current.removeFirst()
        if (ofType.isInstance(value)) yield(value)
        current += value.getAllChildren()
    }
}

inline fun <reified T : Node> Node.getDescendants(inclusive: Boolean = false) : Sequence<Node> =
    this@getDescendants.getAllDescendants(inclusive).filterIsInstance(T::class.java)

fun Node.getAllDescendants(inclusive: Boolean = false) : Sequence<Node> = sequence {
    val node = this@getAllDescendants
    if (inclusive) { yield (node) }
    val current: MutableList<Node> = if (inclusive) mutableListOf(node) else node.getAllChildren().toMutableList()
    while (current.isNotEmpty()) {
        val value = current.removeFirst()
        yield(value)
        current += value.getAllChildren()
    }
}


/**
 * Indicates that the target class defines the structure of a [Node]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NodeDefinition(
    /**
     * Determines whether this node should be instantiable or not.
     */
    val abstract: Boolean = false
)

/**
 * Represents a non-node property of this node.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Property()

//enum class Cardinality() {
//    Single,
//    Many
//}

/**
 * Represents a child of this node.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Child()

/**
 * Represents a reference to another node in the model.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Reference()
