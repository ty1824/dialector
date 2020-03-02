package dev.dialector.typesystem.lattice

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.TypeClause
import dev.dialector.typesystem.TypeObjectClause
import dev.dialector.typesystem.invoke
import dev.dialector.util.Cache
import dev.dialector.util.lraCache

interface TypeLattice {
    fun isSubtypeOf(candidate: Type, supertype: Type): Boolean
    fun isEquivalent(candidate: Type, other: Type): Boolean
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

@Suppress("UNCHECKED_CAST")
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

    init {
        this.supertypeRelations.asSequence()
                .map { it.isValidFor }
                .filterIsInstance<TypeObjectClause<*>>()
                .map { it.type }
                .forEach { this.directSupertypes(it) }
    }

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

    override fun isEquivalent(candidate: Type, other: Type): Boolean {
        return candidate == other
    }

    override fun leastCommonSupertypes(types: Iterable<Type>): Set<Type> {
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
                        } else true
                    }.toSet()

        } while (frontier.isNotEmpty())

        return result.asSequence().filterRedundantSupertypes().toSet()
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


    override fun directSupertypes(type: Type): Set<Type> = supertypes.computeIfAbsent(type) {
        supertypeRelations.asSequence()
                .flatMap { it.evaluate(type) }
                .toSet()
    }
}
