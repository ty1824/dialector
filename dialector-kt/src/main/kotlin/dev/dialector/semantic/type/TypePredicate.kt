package dev.dialector.semantic.type

import dev.dialector.util.ClassifierPredicate
import dev.dialector.util.InstancePredicate
import dev.dialector.util.LogicalPredicate
import dev.dialector.util.TypesafePredicate
import kotlin.reflect.KClass

/**
 * A clause that describes a type.
 */
public interface TypePredicate<T : Type, in C> : TypesafePredicate<T, C>

/**
 * A special TypeClause that matches against a specific Type.
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
public class TypeObjectPredicate<T : Type>(type: T) : TypePredicate<T, Any>, InstancePredicate<T>(type)

/**
 * A special TypeClause that matches against a specific Typeclass
 *
 * This specialization facilitates optimizations in TypeLattice implementation.
 */
public class TypeClassPredicate<T : Type>(
    typeClass: KClass<T>,
) : TypePredicate<T, Any>, ClassifierPredicate<T>(typeClass)

public class TypeLogicalPredicate<T : Type, in C>(
    typeClass: KClass<T>,
    predicate: C.(T) -> Boolean,
) : TypePredicate<T, C>, LogicalPredicate<T, C>(typeClass, predicate)

/**
 * Creates a TypeClause that matches against a specific Type.
 */
public fun <T : Type> type(type: T): TypePredicate<T, Any> = TypeObjectPredicate(type)

/**
 * Creates a TypeClause that matches against a specific Typeclass.
 */
public inline fun <reified T : Type> typeClass(): TypePredicate<T, Any> = TypeClassPredicate(T::class)

/**
 * Creates a TypeClause that matches types against a given predicate.
 */
public inline fun <reified T : Type, C> typeClause(
    noinline predicate: C.(T) -> Boolean,
): TypePredicate<T, C> = TypeLogicalPredicate(T::class, predicate)
