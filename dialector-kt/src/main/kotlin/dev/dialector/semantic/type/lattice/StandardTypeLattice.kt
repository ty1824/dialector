package dev.dialector.semantic.type.lattice

import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.util.Cache
import dev.dialector.util.lraCache

public object AnyType : IdentityType("any")
public object NoneType : IdentityType("none")

public data class AndType(val members: Set<Type>) : Type
public data class OrType(val members: Set<Type>) : Type

public class StandardTypeLattice<C>(
    supertypeRelations: Collection<SupertypeRelation<*, in C>>,
    subtypeRules: Collection<SupertypeRule<in C>>,
    override val topType: Type = AnyType,
    override val bottomType: Type = NoneType,
) : TypeLattice<C> {
    private val supertypeRelations: List<SupertypeRelation<*, in C>> = supertypeRelations.toList()
    private val supertypeRule: List<SupertypeRule<in C>> = subtypeRules.toList()
    private val supertypes: MutableMap<Type, Set<Type>> = mutableMapOf()
    private val subtypeCache: Cache<Pair<Type, Type>, Boolean> = lraCache(100)

    override fun isSubtypeOf(candidate: Type, supertype: Type, context: C): Boolean =
        isEquivalent(candidate, supertype, context) ||
            supertype == topType ||
            candidate == bottomType ||
            subtypeCache.computeIfAbsent(candidate to supertype) {
                isSubtypeOf(it.first, it.second, mutableSetOf(), context)
            }

    private fun isSubtypeOf(candidate: Type, supertype: Type, visited: MutableSet<Type>, context: C): Boolean {
        visited.add(candidate)
        val directSupertypes = directSupertypes(candidate, context)
        // Check supertypes first, if none apply then check if types are replaceable
        return directSupertypes.contains(supertype) ||
            supertypeRule.any { it.check(candidate, supertype, context) } ||
            // If no match found, recurse on supertypes.
            directSupertypes.asSequence()
                .minus(visited)
                .any { isSubtypeOf(it, supertype, visited, context) }
    }

    override fun isEquivalent(candidate: Type, other: Type, context: C): Boolean {
        return candidate == other
    }

    public fun leastCommonSupertypes(types: Iterable<Type>, context: C): Set<Type> {
        assert(!types.none()) { "May not call leastCommonSupertypes without at least one argument type" }

        val initialTypes = types.asSequence().filterRedundantSubtypes(context)

        if (initialTypes.none() || initialTypes.drop(1).none()) {
            return initialTypes.toSet()
        }

        var frontier: Set<Type> = initialTypes.toSet()
        val result: MutableSet<Type> = mutableSetOf()
        do {
            frontier = frontier.asSequence()
                .flatMap { directSupertypes(it, context).asSequence() }
                .filterRedundantSupertypes(context)
                .filter { type ->
                    // If all the input types are subtypes of this type, filter it out
                    if (initialTypes.all { isSubtypeOf(it, type, context) }) {
                        // Additionally, if none of the existing result types is a subtype of this type, add it to result
                        if (result.none { isSubtypeOf(it, type, context) }) result += type
                        false
                    } else {
                        true
                    }
                }.toSet()
        } while (frontier.isNotEmpty())

        return result.asSequence().filterRedundantSupertypes(context).toSet()
    }

    override fun leastCommonSupertype(types: Iterable<Type>, context: C): Type {
        assert(!types.none()) { "May not call leastCommonSupertype without at least one argument type" }
        val commonSupertypes = leastCommonSupertypes(types, context)

        return if (commonSupertypes.size > 1) {
            OrType(commonSupertypes)
        } else {
            commonSupertypes.first()
        }
    }

    override fun greatestCommonSubtype(types: Iterable<Type>, context: C): Type {
        assert(!types.none()) { "May not call greatestCommonSubtype without at least one argument type" }
        val filteredTypes = types.asSequence().filterRedundantSupertypes(context).toSet()

        return if (filteredTypes.size > 1) {
            AndType(filteredTypes)
        } else {
            filteredTypes.first()
        }
    }

    private fun Sequence<Type>.filterRedundantSupertypes(context: C): Sequence<Type> =
        this.distinct().filter { type ->
            this.none {
                type != it && isSubtypeOf(it, type, context)
            }
        }

    private fun Sequence<Type>.filterRedundantSubtypes(context: C): Sequence<Type> =
        this.distinct().filter { type ->
            this.none {
                type != it && isSubtypeOf(type, it, context)
            }
        }

    override fun directSupertypes(type: Type, context: C): Set<Type> =
        supertypes.computeIfAbsent(type) {
            supertypeRelations.asSequence()
                .flatMap { it.evaluate(type, context) }
                .toSet()
                .ifEmpty { setOf(this.topType) }
        }
}
