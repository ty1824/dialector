package dev.dialector.semantic.type

import dev.dialector.util.TypesafeClause
import kotlin.reflect.KClass

/**
 * A clause that describes a type.
 */
public interface TypeClause<T : Type> : TypesafeClause<T> {
    public operator fun invoke(candidate: Type): Boolean =
        clauseClass.isInstance(candidate) && constraint(candidate as T)
}

/**
 * A special TypeClause that matches against a specific Type.
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
public data class TypeObjectClause<T : Type> constructor(override val clauseClass: KClass<out T>, val type: T) : TypeClause<T> {
    override fun constraint(candidate: T): Boolean = type == candidate

    override fun invoke(candidate: Type): Boolean = type == candidate
}

/**
 * A special TypeClause that matches against a specific Typeclass
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
public data class TypeClassClause<T : Type> constructor(override val clauseClass: KClass<T>) : TypeClause<T> {
    override fun constraint(candidate: T): Boolean = true

    override fun invoke(candidate: Type): Boolean = clauseClass.isInstance(candidate)
}

/**
 * Creates a TypeClause that matches against a specific Type.
 */
public fun <T : Type> type(type: T): TypeClause<T> = TypeObjectClause(type::class, type)

/**
 * Creates a TypeClause that matches against a specific Typeclass.
 */
public inline fun <reified T : Type> typeClass(): TypeClause<T> = TypeClassClause(T::class)

/**
 * Creates a TypeClause that matches types against a given predicate.
 */
public inline fun <reified T : Type> typeClause(crossinline predicate: (T) -> Boolean): TypeClause<T> = object : TypeClause<T> {
    override val clauseClass = T::class
    override fun constraint(candidate: T): Boolean = predicate(candidate)
}

