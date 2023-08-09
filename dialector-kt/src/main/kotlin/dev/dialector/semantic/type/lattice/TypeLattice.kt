package dev.dialector.semantic.type.lattice

import dev.dialector.semantic.type.Type

/**
 * Defines algorithms for resolving the supremum and infimum of some set of types.
 */
public interface ExtremumSolver<C> {
    /**
     * Resolves the set of least common supertypes, otherwise known as the supremum, for the input types.
     *
     * A Common Supertype of some set of types T0-Tn is defined as some type S that is a supertype of all types T0-Tn.
     *
     * The Least Common Supertype for some set of types T0-Tn is defined as some Common Supertype S that is a subtype of
     * all other Common Supertypes.
     *
     * In some cases, there may be multiple valid candidate types. This method should resolve them using a Join-like
     * type.
     */
    public fun leastCommonSupertype(types: Iterable<Type>, context: C): Type

    /**
     * Resolves the set of greatest common subtypes, otherwise known as the infimum, for input types.
     *
     * A Common Subtype of some set of types T0-Tn is defined as some type S that is a subtype of all types T0-Tn.
     *
     * The Greatest Common Subtype for some set of types T0-Tn is defined as some Common Subtype S that is a supertype
     * of all other Common Subtypes.
     *
     * In some cases, there may be multiple valid concrete candidate types. This method should resolve them using a
     * Meet-like type.
     */
    public fun greatestCommonSubtype(types: Iterable<Type>, context: C): Type
}

/**
 * An object that maintains the type inheritance graph in lattice form.
 */
public interface TypeLattice<C> : ExtremumSolver<C> {
    /**
     * The common supertype of all other types.
     */
    public val topType: Type

    /**
     * The common subtype of all other types.
     */
    public val bottomType: Type

    /**
     * Returns true if the candidate type is a subtype of the expected supertype.
     */
    public fun isSubtypeOf(candidate: Type, supertype: Type, context: C): Boolean

    /**
     * Returns true if the candidate type is considered to be the same type as the other type.
     */
    public fun isEquivalent(candidate: Type, other: Type, context: C): Boolean

    /**
     * Returns supertypes defined by [SupertypingRelation]s, does not include implicit supertypes derived from
     * [SubtypeRule]s
     */
    public fun directSupertypes(type: Type, context: C): Set<Type>
}

public interface SupertypeRule<C> {
    public fun check(subtype: Type, supertype: Type, context: C): Boolean
}
