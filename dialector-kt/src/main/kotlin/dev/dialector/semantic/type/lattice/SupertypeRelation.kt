package dev.dialector.semantic.type.lattice

import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.TypePredicate

/**
 * Defines a relation between some category of types matching a [TypePredicate] and a set of valid supertypes for all types
 * in that category. Multiple relations may apply to the same type.
 */
public interface SupertypeRelation<T : Type, in C> {
    public val isValidFor: TypePredicate<T, C>
    public fun supertypes(type: T, context: C): Sequence<Type>
}

@Suppress("UNCHECKED_CAST")
public fun <T : Type, C> SupertypeRelation<T, C>.evaluate(candidate: Type, context: C): Sequence<Type> =
    if (isValidFor(candidate, context)) this.supertypes(candidate as T, context) else sequenceOf()

/**
 * Creates a [SupertypeRelation] indicating that the left-hand type is a subtype of all types produced by the function.
 */
public infix fun <T : Type, C> TypePredicate<T, C>.hasSupertypes(
    supertypeFunction: C.(type: T) -> Iterable<Type>,
): SupertypeRelation<T, C> = object : SupertypeRelation<T, C> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T, context: C): Sequence<Type> = context.supertypeFunction(type).asSequence()
}

/**
 * Creates a [SupertypeRelation] indicating that the left-hand type is a subtype of all of the right-hand types.
 */
public infix fun <T : Type, C> TypePredicate<T, C>.hasSupertypes(
    explicitSupertypes: Iterable<Type>,
): SupertypeRelation<T, C> = object : SupertypeRelation<T, C> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T, context: C): Sequence<Type> = explicitSupertypes.asSequence()
}

/**
 * Creates a [SupertypeRelation] indicating that the left-hand type is a subtype of all of the right-hand types.
 */
public fun <T : Type, C> TypePredicate<T, C>.hasSupertypes(
    vararg explicitSupertypes: Type,
): SupertypeRelation<T, C> = this hasSupertypes explicitSupertypes.asIterable()

/**
 * Creates a [SupertypeRelation] indicating that the left-hand type is a subtype of the right-hand type.
 */
public infix fun <T : Type, C> TypePredicate<T, C>.hasSupertype(
    explicitSupertype: Type,
): SupertypeRelation<T, C> = this.hasSupertypes(explicitSupertype)
