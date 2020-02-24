package dev.dialector.typesystem.lattice

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.TypeClause
import dev.dialector.typesystem.invoke
import dev.dialector.util.Cache
import dev.dialector.util.lraCache

interface TypeLattice {
    fun isSubtypeOf(candidate: Type, supertype: Type): Boolean
    fun leastCommonSupertypes(types: Iterable<Type>): Set<Type>

    /**
     * Returns supertypes defined by [SupertypingRelation]s, does not include implicit supertypes derived from
     * [SubtypeRule]s
     */
    fun directSupertypes(type: Type): Set<Type>
}

/**
 *
 */
interface SupertypeRelation<T : Type> {
    val isValidFor: TypeClause<T>
    fun supertypes(type: T): Sequence<Type>
}

fun <T : Type> SupertypeRelation<T>.evaluate(candidate: Type): Sequence<Type> =
    if (this.isValidFor(candidate)) this.supertypes(candidate as T) else sequenceOf()


interface ReplacementRule {
    fun check(subtype: Type, supertype: Type, lattice: TypeLattice): Boolean
}

infix fun <T : Type> TypeClause<T>.hasSupertypes(supertypeFunction: (type: T) -> Sequence<Type>): SupertypeRelation<T> = object : SupertypeRelation<T> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T): Sequence<Type> = supertypeFunction(type)
}

infix fun <T : Type> TypeClause<T>.hasSupertypes(explicitSupertypes: Sequence<Type>): SupertypeRelation<T> = object : SupertypeRelation<T> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T): Sequence<Type> = explicitSupertypes
}

//val test = type(object : Type {}) hasSupertypes { object : Type {} }

class DefaultTypeLattice(supertypeRelations: Collection<SupertypeRelation<*>>, subtypeRules: Collection<ReplacementRule>) : TypeLattice {
    private val supertypeRelations: List<SupertypeRelation<*>> = supertypeRelations.toList()
    private val subtypeRules: List<ReplacementRule> = subtypeRules.toList()
    private val supertypes: MutableMap<Type, Set<Type>> = mutableMapOf()
    private val subtypeCache: Cache<Pair<Type, Type>, Boolean> = lraCache(100)

    override fun isSubtypeOf(candidate: Type, supertype: Type): Boolean = candidate == supertype ||
            subtypeCache.computeIfAbsent(candidate to supertype) { isSubtypeOf(it.first, it.second, mutableSetOf()) }

    private fun isSubtypeOf(candidate: Type, supertype: Type, visited: MutableSet<Type>): Boolean {
        visited.add(candidate)
        val directSupertypes = directSupertypes(candidate)
        // Check supertypes first, if none apply then check if types are replaceable
        return directSupertypes.contains(supertype) ||
                subtypeRules.any { it.check(candidate, supertype, this ) } ||
                // If no match found, recurse on supertypes.
                directSupertypes.asSequence()
                        .minus(visited)
                        .any { isSubtypeOf(it, supertype, visited) }

    }

    override fun leastCommonSupertypes(types: Iterable<Type>): Set<Type> {
        val initialTypes = types.asSequence()
                .distinct()
                .filter { supertypeFilter(types, it) }

        if (initialTypes.none() || initialTypes.drop(1).none()) {
            return initialTypes.toSet()
        }

        var frontier: Sequence<Type> = initialTypes
        val result: MutableSet<Type> = mutableSetOf()
        do {
            frontier = frontier.flatMap { directSupertypes(it).asSequence() }
                    .distinct()
                    .filter { supertypeFilter(frontier.asIterable(), it)}
                    .filter { type ->
                        // If all of the input types is a subtype of this type, filter it out
                        if (initialTypes.all { isSubtypeOf(it, type) }) {
                            // Additionally, if none of the existing result types is a subtype of this type, add it to result
                            if (result.none { isSubtypeOf(it, type) }) result += type
                            false
                        } else true
                    }

        } while (frontier.none())

        return result.filter { subtypeFilter(result, it) }.toSet()
    }

    private fun supertypeFilter(types: Iterable<Type>, type: Type) = types.none { type != it && isSubtypeOf(it, type) }
    private fun subtypeFilter(types: Iterable<Type>, type: Type) = types.none { type != it && isSubtypeOf(type, it) }


    override fun directSupertypes(type: Type): Set<Type> = supertypes.computeIfAbsent(type) {
        supertypeRelations.asSequence()
                .flatMap {
                    sequence<Type> {
                        it.evaluate(type)?.apply {
                            yieldAll(this)
                        }
                    }
                }
                .toSet()
    }
}
