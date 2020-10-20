package dev.dialector.model

import kotlin.reflect.KProperty

/*
 * Notes:
 *
 * "Augmentations" kinda like Node Attributes? Slots for externally-driven additional content.
 */

interface Node {
    val parent: Node?
    val properties: Map<KProperty<*>, Any?>
    val children: Map<KProperty<*>, List<Node>>
    val references: Map<KProperty<*>, List<NodeReference<*>>>

    fun allChildren(): List<Node>
    fun allReferences(): List<NodeReference<*>>
}

class PropertyValue<T>(var value: T) {
    operator fun getValue(inst: Node?, property: KProperty<*>): T = value
    operator fun setValue(inst: Node?, property: KProperty<*>, newValue: T) {
        value = newValue
    }
}


fun Node.getRoot(): Node = parent?.getRoot() ?: this

@Suppress("UNCHECKED_CAST")
fun <T> Node.getProperty(property: KProperty<T>): T =
    properties[property] as T

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getChildren(relation: KProperty<T>): List<T> =
    children[relation] as List<T>

@Suppress("UNCHECKED_CAST")
fun <T : Node> Node.getReferences(relation: KProperty<T>): List<NodeReference<T>> =
    references[relation] as List<NodeReference<T>>

fun Node.getAllChildren() : List<Node> = children.values.flatten()

fun Node.getAllReferences() : List<NodeReference<*>> = references.values.flatten()

interface NodeReference<T : Node> {
    fun source(): Node
    fun resolve(): Node?
}



/**
 * Indicates that the target class defines the structure of a [Node]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class NodeDefinition()

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Property()

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Child()

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Reference()
