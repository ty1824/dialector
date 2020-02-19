package dev.dialector.typesystem

import dev.dialector.util.Cache
import dev.dialector.util.lraCache

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
interface SupertypeRelation<T : Type> {
    val isValidFor: TypeClause<T>
    fun supertypes(type: T): Iterable<Type>
}

fun <T : Type> SupertypeRelation<T>.evaluate(candidate: Type): Iterable<Type>? =
    if (this.isValidFor(candidate)) this.supertypes(candidate as T) else null


interface ReplacementRule {
    fun check(subtype: Type, supertype: Type, lattice: TypeLattice): Boolean
}

infix fun <T : Type> TypeClause<T>.hasSupertypes(supertypeFunction: (type: T) -> Iterable<Type>): SupertypeRelation<T> = object : SupertypeRelation<T> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T): Iterable<Type> = supertypeFunction(type)
}

infix fun <T : Type> TypeClause<T>.hasSupertypes(explicitSupertypes: Iterable<Type>): SupertypeRelation<T> = object : SupertypeRelation<T> {
    override val isValidFor = this@hasSupertypes
    override fun supertypes(type: T): Iterable<Type> = explicitSupertypes
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
