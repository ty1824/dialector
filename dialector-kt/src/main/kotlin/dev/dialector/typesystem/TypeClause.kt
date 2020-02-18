package dev.dialector.typesystem

import kotlin.reflect.KClass

/**
 * A clause that describes a type.
 */
interface TypeClause<T : Type> {
    val typeClass: KClass<out T>
    fun constraint(candidate: T): Boolean
}

operator fun <T : Type> TypeClause<T>.invoke(candidate: Type) = this.typeClass.isInstance(candidate) && this.constraint(candidate as T)

/**
 * A special TypeClause that matches against a specific Type.
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
data class TypeObjectClause<T : Type> internal constructor(override val typeClass: KClass<out T>, val type: T) : TypeClause<T> {
    override fun constraint(candidate: T): Boolean = type == candidate
}

/**
 * A special TypeClause that matches against a specific Typeclass
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
data class TypeClassClause<T : Type> internal constructor(override val typeClass: KClass<T>) : TypeClause<T> {
    override fun constraint(candidate: T): Boolean = true
}

/**
 * Creates a TypeClause that matches against a specific Type.
 */
fun <T : Type> type(type: T): TypeClause<T> = TypeObjectClause(type::class, type)

/**
 * Creates a TypeClause that matches against a specific Typeclass.
 */
fun <T : Type> typeClass(typeClass: KClass<T>): TypeClause<T> = TypeClassClause(typeClass)

/**
 * Creates a TypeClause that matches types against a given predicate.
 */
inline fun <reified T : Type> typeClause(crossinline predicate: (T) -> Boolean) = object : TypeClause<T> {
    override val typeClass = T::class
    override fun constraint(candidate: T): Boolean = predicate(candidate)
}

