package dev.dialector.typesystem

import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import dev.dialector.util.Cache
import dev.dialector.util.lruCache

import kotlin.reflect.KClass

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
interface SupertypeRelation {
    val isValidFor: TypeClause<*>
    fun supertypes(type: Type): Iterable<Type>
}

interface ReplacementRule {
    fun check(subtype: Type, supertype: Type, lattice: TypeLattice): Boolean
}

infix fun <T : Type> TypeClause<T>.hasSupertypes(supertypes: (type: T) -> Iterable<Type>): SupertypeRelation = object : SupertypeRelation {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: Type): Iterable<Type> = supertypes(type)
}

//val test = type(object : Type {}) hasSupertypes { object : Type {} }

class SampleTypeLattice(supertypeRelations: Collection<SupertypeRelation>, subtypeRules: Collection<ReplacementRule>) : TypeLattice {
    private val supertypeRelations: List<SupertypeRelation> = supertypeRelations.toList()
    private val subtypeRules: List<ReplacementRule> = subtypeRules.toList()
    private val supertypes: MutableMap<Type, Set<Type>> = mutableMapOf()
    private val subtypeCache: Cache<Pair<Type, Type>, Boolean> = lruCache(100)

    override fun isSubtypeOf(candidate: Type, supertype: Type): Boolean =
            subtypeCache.computeIfAbsent(candidate to supertype) { isSubtypeOf(it.first, it.second, mutableSetOf()) }

    private fun isSubtypeOf(candidate: Type, supertype: Type, visited: MutableSet<Type>): Boolean {
        visited.add(candidate)
        val directSupertypes = directSupertypes(candidate)
        // Check supertypes first, if none apply then check if types are replaceable
        if (directSupertypes.contains(supertype) || subtypeRules.any { it.check(candidate, supertype, this ) }) {
            return true
        } else {
            // If no match found, recurse on supertypes.
            return directSupertypes.asSequence().minus(visited).any { isSubtypeOf(it, supertype, visited) } || subtypeRules.any { it.check(candidate, supertype, this ) }
        }

    }

    override fun leastCommonSupertypes(types: Iterable<Type>): Set<Type> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun directSupertypes(type: Type): Set<Type> = supertypes.computeIfAbsent(type) {
        supertypeRelations.asSequence()
                .filter { it.isValidFor(type) }
                .flatMap { it.supertypes(type).asSequence() }
                .toSet()
    }
}
