package dev.dialector.model

import kotlin.reflect.KProperty

/*
 * Notes:
 *
 * "Augmentations" kinda like Node Attributes? Slots for externally-driven additional content.
 */

/**
 * Represents an element in a program graph
 */
interface Node {
    val parent: Node?
    val properties: Map<String, Any?>
    val children: Map<String, List<Node>>
    val references: Map<String, NodeReference<*>>
}

interface NodeReference<T : Node> {
    fun source(): Node
    fun resolve(): Node?
}


fun Node.getRoot(): Node = parent?.getRoot() ?: this

@Suppress("UNCHECKED_CAST")
fun <T> Node.getProperty(property: KProperty<T>): T =
    properties[property.name] as T

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getChildren(child: KProperty<T>): List<T> =
    children[child.name] as List<T>

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getReferences(relation: KProperty<T>): List<NodeReference<T>> =
    references[relation.name] as List<NodeReference<T>>

fun Node.getAllChildren() : List<Node> = children.values.flatten()

fun Node.getAllReferences() : List<NodeReference<*>> = references.values.toList()




/**
 * Indicates that the target class defines the structure of a [Node]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NodeDefinition(
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
