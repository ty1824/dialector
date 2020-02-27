package dev.dialector.typesystem.inference

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice

sealed class InferenceTerm {
    abstract val id: Int
}

data class TypeTerm(override val id: Int, val type: dev.dialector.typesystem.Type) : InferenceTerm()

data class VariableTerm(override val id: Int) : InferenceTerm()

sealed class Relation {
    abstract val left: InferenceTerm
    abstract val right: InferenceTerm

    data class Equality(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()
    data class Subtype(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()
    data class Supertype(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()
}

interface InferenceEnvironment {

}

interface InferenceSolver {
    fun solve(context: InferenceContext): Map<InferenceTerm, InferenceResult>
}

class InferenceContext(val lattice: TypeLattice) {
    private val relationGroups: MutableMap<VariableTerm, Group> = mutableMapOf()

    private fun getRelationGroup(variable: VariableTerm) = relationGroups.computeIfAbsent(variable) { Group(setOf(variable)) }

    /**
     * Register equality between two terms
     */
    public fun equality(left: InferenceTerm, right: InferenceTerm) {
        val leftGroup = if (left is VariableTerm) getRelationGroup(left) else Group(setOf(left))
        val rightGroup = if (right is VariableTerm) getRelationGroup(right)  else Group(setOf(right))

        // If these two are not already in the same group, unify their groups
        if (leftGroup != rightGroup) {
            leftGroup.unify(rightGroup)
        }
    }

    /**
     * Register a lower bound for a variable
     */
    public fun subtype(left: VariableTerm, right: InferenceTerm) {
        getRelationGroup(left).lowerBounds += asBound(right)
    }

    /**
     * Register an upper bound for a variable
     */
    public fun supertype(left: VariableTerm, right: InferenceTerm) {
        getRelationGroup(left).upperBounds += asBound(right)
    }

    public fun getRelationGroups(): Map<VariableTerm, Group> = this.relationGroups.toMap()

    private fun asBound(term: InferenceTerm): Bound =
        when (term) {
            is VariableTerm -> Bound.GroupBound(getRelationGroup(term))
            is TypeTerm -> Bound.TypeBound(term)
        }

    private fun Group.unify(other: Group) {
        this.terms += other.terms
        this.upperBounds += other.upperBounds
        this.lowerBounds += other.lowerBounds
        other.terms.filterIsInstance<VariableTerm>().forEach { relationGroups[it] = this }
    }

    /**
     * Represents a grouping of inference terms and their upper & lower bounds. This construct helps normalize relations
     * like the following:
     *
     *     1' = 2'
     *     2' = 3'
     *     4' = 1'
     *
     * into a structure like
     *
     *     Group(1', 2' 3, 4')
     *
     * This structure includes type bounds.
     */
    class Group(
        terms: Set<InferenceTerm>,
        upperBounds: Set<Bound> = setOf(),
        lowerBounds: Set<Bound> = setOf()
    ) {
        val terms: MutableSet<InferenceTerm> = terms.toMutableSet()
        val upperBounds: MutableSet<Bound> = upperBounds.toMutableSet()
        val lowerBounds: MutableSet<Bound> = lowerBounds.toMutableSet()
    }
}

sealed class Bound {
    data class TypeBound(val type: TypeTerm) : Bound()
    data class GroupBound(val group: InferenceContext.Group) : Bound()
}

interface InferenceResult

class TypeResult(val type: Type) : InferenceResult

class ErrorResult(val reason: String) : InferenceResult

object DefaultInferenceSolver : InferenceSolver {
    override fun solve(context: InferenceContext): Map<InferenceTerm, InferenceResult> {
        val typeMap: MutableMap<InferenceContext.Group, InferenceResult> = mutableMapOf()

        val inequalities = context.getRelationGroups().values.filter { group ->
            val typeTerms = group.terms.filterIsInstance<TypeTerm>()
            if (typeTerms.size > 0) {
                if (typeTerms.size == 1) {
                    typeMap[group] = TypeResult(typeTerms.first().type)
                    false
                } else {
                    typeMap[group] = ErrorResult("Too many bound types: " + typeTerms.toString())
                    false
                }
            }

            true
        }.toMutableSet()

//        while (inequalities.isNotEmpty()) {
//            val current = inequalities.first()
//            val upperBound = current.upperBounds.
//
//
//        }

        return typeMap.keys.flatMap { group ->
            val type = typeMap[group]?.let { it } ?: ErrorResult("No type inferred")
            group.terms.map { it to type }
        }.toMap()
    }

//    private fun Set<Bound>.resolve(typeMap: Map<InferenceContext.Group, InferenceResult>): Set<InferenceResult> {
//        return this.map {
//            when (it) {
//                is Bound.TypeBound -> TypeResult(it.type)
//                is Bound.GroupBound -> typeMap[it.group]
//            }
//        }.toSet()
//    }
}