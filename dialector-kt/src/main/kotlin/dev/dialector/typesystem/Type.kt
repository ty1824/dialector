package dev.dialector.typesystem

import java.util.*
import kotlin.reflect.KClass

/**
 * Represents a Type in the TypeSystem. Concrete implementations must override equals and hashCode.
 */
interface Type {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * A Type whose instances are always equivalent. Subclasses should be represented as an object or static val.
 */
abstract class IdentityType : Type {
    override fun equals(other: Any?): Boolean = this::class.isInstance(other)

    override fun hashCode(): Int = this::class.hashCode()
}

typealias TypeClass = KClass<out Type>