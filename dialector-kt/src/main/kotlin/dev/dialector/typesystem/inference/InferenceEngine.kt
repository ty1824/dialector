package dev.dialector.typesystem.inference

import dev.dialector.typesystem.Type

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

typealias Relations = Pair<InferenceTerm, InferenceTerm>

sealed class Bound {
    abstract val variable: VariableTerm
    abstract val bound: InferenceTerm
}

data class UpperBound(override val variable: VariableTerm, override val bound: InferenceTerm) : Bound()
data class LowerBound(override val variable: VariableTerm, override val bound: InferenceTerm) : Bound()

interface InferenceEnvironment {

}

interface InferenceSolver {
    fun solve(context: InferenceContext): Map<InferenceTerm, Type>
}

class InferenceContext {
    val relationGroups: MutableMap<VariableTerm, Group> = mutableMapOf()

    /**
     * Register equality between two terms
     */
    public fun equality(left: InferenceTerm, right: InferenceTerm) {
        val leftGroup = if (left is VariableTerm) relationGroups.computeIfAbsent(left) { Group(setOf(left)) } else Group(setOf(left))
        val rightGroup = if (right is VariableTerm) relationGroups.computeIfAbsent(right) { Group(setOf(right)) }  else Group(setOf(right))

        // If these two are not already in the same group, unify their groups
        if (leftGroup != rightGroup) {
            leftGroup.unify(rightGroup)
        }
    }

    /**
     * Register a lower bound for a variable
     */
    public fun lowerBound(left: VariableTerm, right: InferenceTerm) {
        relationGroups.computeIfAbsent(left) { Group(setOf(left)) }.lowerBounds += right
    }

    /**
     * Register an upper bound for a variable
     */
    public fun upperBound(left: VariableTerm, right: InferenceTerm) {
        relationGroups.computeIfAbsent(left) { Group(setOf(left)) }.upperBounds += right
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
        upperBounds: Set<InferenceTerm> = setOf(),
        lowerBounds: Set<InferenceTerm> = setOf()
    ) {
        val terms: MutableSet<InferenceTerm> = terms.toMutableSet()
        val upperBounds: MutableSet<InferenceTerm> = upperBounds.toMutableSet()
        val lowerBounds: MutableSet<InferenceTerm> = lowerBounds.toMutableSet()
    }
}

object DefaultInferenceSolver : InferenceSolver {
    override fun solve(context: InferenceContext): Map<InferenceTerm, Type> {
        TODO("Not yet implemented")
    }
}