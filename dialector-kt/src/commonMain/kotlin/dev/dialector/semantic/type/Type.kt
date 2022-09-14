package dev.dialector.semantic.type

import kotlin.reflect.KClass

/**
 * Represents a Type in the [TypeSystem]. Concrete implementations must override equals and hashCode.
 */
interface Type {
    /**
     * Returns all child components of this type, e.g. type arguments for a parameterized type.
     */
    fun getComponents(): Sequence<Type> = sequenceOf()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

/**
 * A [Type] whose instances are always equivalent. Subclasses should be represented as an object or static val.
 */
abstract class IdentityType(val name: String) : Type {
    final override fun equals(other: Any?): Boolean = this::class.isInstance(other)
    final override fun hashCode(): Int = this::class.hashCode()
    final override fun toString(): String = this.name
}

typealias TypeClass = KClass<out Type>