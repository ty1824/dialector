package dev.dialector.typesystem

/**
 * Represents a Type in the TypeSystem. Concrete implementations must override equals and hashCode.
 */
interface Type {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}