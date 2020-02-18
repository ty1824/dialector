package dev.dialector.typesystem

import kotlin.reflect.KClass

/**
 * Represents a Type in the TypeSystem. Concrete implementations must override equals and hashCode.
 */
interface Type {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

typealias TypeClass = KClass<out Type>