package dev.dialector.semantic.type.lattice

import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.TypeObjectClause
import dev.dialector.util.Cache
import dev.dialector.util.lraCache

public object AnyType : IdentityType("any")
public object NoneType : IdentityType("none")

public data class AndType(val members: Set<Type>) : Type
public data class OrType(val members: Set<Type>) : Type

public class SimpleTypeLattice(
    supertypeRelations: Collection<SupertypeRelation<*>>,
    subtypeRules: Collection<SupertypeRule>,
    override val topType: Type = AnyType,
    override val bottomType: Type = NoneType,
) : TypeLattice {
    private val supertypeRelations: List<SupertypeRelation<*>> = supertypeRelations.toList()
    private val supertypeRule: List<SupertypeRule> = subtypeRules.toList()
    private val supertypes: MutableMap<Type, Set<Type>> = mutableMapOf()
    private val subtypeCache: Cache<Pair<Type, Type>, Boolean> = lraCache(100)

    init {
        this.supertypeRelations.asSequence()
            .map { it.isValidFor }
            .filterIsInstance<TypeObjectClause<*>>()
            .map { it.type }
            .forEach { this.directSupertypes(it) }
    }

    override fun isSubtypeOf(candidate: Type, supertype: Type): Boolean =
        candidate == supertype ||
            supertype == topType ||
            candidate == bottomType ||
            subtypeCache.computeIfAbsent(candidate to supertype) { isSubtypeOf(it.first, it.second, mutableSetOf()) }

    private fun isSubtypeOf(candidate: Type, supertype: Type, visited: MutableSet<Type>): Boolean {
        visited.add(candidate)
        val directSupertypes = directSupertypes(candidate)
        // Check supertypes first, if none apply then check if types are replaceable
        return directSupertypes.contains(supertype) ||
            supertypeRule.any { it.check(candidate, supertype, this) } ||
            // If no match found, recurse on supertypes.
            directSupertypes.asSequence()
                .minus(visited)
                .any { isSubtypeOf(it, supertype, visited) }
    }

    override fun isEquivalent(candidate: Type, other: Type): Boolean {
        return candidate == other
    }

    public fun leastCommonSupertypes(types: Iterable<Type>): Set<Type> {
        assert(!types.none()) { "May not call leastCommonSupertypes without at least one argument type" }

        val initialTypes = types.asSequence().filterRedundantSubtypes()

        if (initialTypes.none() || initialTypes.drop(1).none()) {
            return initialTypes.toSet()
        }

        var frontier: Set<Type> = initialTypes.toSet()
        val result: MutableSet<Type> = mutableSetOf()
        do {
            frontier = frontier.asSequence()
                .flatMap { directSupertypes(it).asSequence() }
                .filterRedundantSupertypes()
                .filter { type ->
                    // If all of the input types is a subtype of this type, filter it out
                    if (initialTypes.all { isSubtypeOf(it, type) }) {
                        // Additionally, if none of the existing result types is a subtype of this type, add it to result
                        if (result.none { isSubtypeOf(it, type) }) result += type
                        false
                    } else {
                        true
                    }
                }.toSet()
        } while (frontier.isNotEmpty())

        return result.asSequence().filterRedundantSupertypes().toSet()
    }

    override fun leastCommonSupertype(types: Iterable<Type>): Type {
        assert(!types.none()) { "May not call leastCommonSupertype without at least one argument type" }
        val commonSupertypes = leastCommonSupertypes(types)

        return if (commonSupertypes.size > 1) {
            OrType(commonSupertypes)
        } else {
            commonSupertypes.first()
        }
    }

    override fun greatestCommonSubtype(types: Iterable<Type>): Type {
        assert(!types.none()) { "May not call greatestCommonSubtype without at least one argument type" }
        val filteredTypes = types.asSequence().filterRedundantSupertypes().toSet()

        return if (filteredTypes.size > 1) {
            AndType(filteredTypes)
        } else {
            filteredTypes.first()
        }
    }

    private fun Sequence<Type>.filterRedundantSupertypes(): Sequence<Type> =
        this.distinct().filter { type ->
            this.none {
                type != it && isSubtypeOf(it, type)
            }
        }

    private fun Sequence<Type>.filterRedundantSubtypes(): Sequence<Type> =
        this.distinct().filter { type ->
            this.none {
                type != it && isSubtypeOf(type, it)
            }
        }

    override fun directSupertypes(type: Type): Set<Type> =
        supertypes.computeIfAbsent(type) {
            supertypeRelations.asSequence()
                .flatMap { it.evaluate(type) }
                .toSet()
                .ifEmpty { setOf(this.topType) }
        }
}
