package dev.dialector.typesystem

import kotlin.reflect.KClass

/**
 * A clause that describes a type.
 *
 * TypeClauses created for specific Types and Typeclasses are preferred as they facilitate caching. Create these
 * using the [type] and [typeClass] factories.
 */
interface TypeClause {
    fun match(candidate: Type): Boolean
}

/**
 * A special TypeClause that matches against a specific Type.
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
data class TypeObjectClause internal constructor(val type: Type) : TypeClause {
    override fun match(candidate: Type): Boolean = type == candidate
}

/**
 * A special TypeClause that matches against a specific Typeclass
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
data class TypeClassClause internal constructor(val typeClass: KClass<out Type>) : TypeClause {
    override fun match(candidate: Type): Boolean = typeClass.isInstance(candidate)
}

/**
 * Creates a TypeClause that matches against a specific Type.
 */
fun type(type: Type): TypeClause = TypeObjectClause(type)

/**
 * Creates a TypeClause that matches against a specific Typeclass.
 */
fun typeClass(typeClass: KClass<out Type>): TypeClause = TypeClassClause(typeClass)

/**
 * Creates a TypeClause that matches types against a given predicate.
 */
fun typeClause(predicate: (Type) -> Boolean) = object : TypeClause {
    override fun match(candidate: Type): Boolean = predicate(candidate)
}

