package dev.dialector.semantic.type

import kotlin.reflect.KClass

/**
 * Represents a Type in the [TypeSystem]. Concrete implementations must override equals and hashCode.
 */
public interface Type {
    /**
     * Returns all child components of this type, e.g. type arguments for a parameterized type.
     */
    public fun getComponents(): Sequence<Type> = sequenceOf()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

/**
 * A [Type] whose instances are always equivalent. Subclasses should be represented as an object or static val.
 */
public abstract class IdentityType(public val name: String) : Type {
    final override fun equals(other: Any?): Boolean = this::class.isInstance(other)
    final override fun hashCode(): Int = this::class.hashCode()
    final override fun toString(): String = this.name
}

public typealias TypeClass = KClass<out Type>
