package dev.dialector.model

import kotlin.reflect.KProperty

/**
 * A collection of metadata about a [Node] class
 */
interface NodeDef {
    val propertyDefinitions: List<KProperty<*>>
    val childDefinitions: List<KProperty<*>>
    val referenceDefinitions: List<KProperty<*>>
}