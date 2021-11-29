package dev.dialector.semantic.type.inference

import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.TypeLattice

/*
 * Incremental inference algorithm is as follows:
 *
 * An empty inference context CTX is created.
 *
 * Upon loading a file:
 * 1) Inference context is seeded with constraints from the file
 * 2) Reduction/Resolution is triggered
 *
 * Upon user change:
 * 1) Diff is computed and then applied (unused constraints are removed and new constraints are added)
 * 2) Constraints and bounds that were derived from removed/changed constraints are removed, recursively
 * 3) Bound sets dependent on invalidated bounds that had been resolved previously are invalidated
 * 3) Reduction/Resolution is triggered
 *
 * Reduction/Resolution:
 * 1) All constraints that have not already undergone reduction are reduced either using a reduction rule.
 *      Reduction rules are functions of the form (constraint, system) -> Unit and may generate bounds or derived constraints
 * 2)
 */

class Variable(val id: String)

sealed class TypeRelation
object EqualityR : TypeRelation()
object SubtypeR : TypeRelation()
object SupertypeR : TypeRelation()

class InferenceSys {
    val bindings: MutableMap<Variable, Type> = mutableMapOf()
    val graph: DataGraph<Variable, TypeRelation> = DataGraph()

    /**
     * Creates a new variable, optionally bound to a type.
     */
    fun variable(id: String, type: Type? = null): Variable {
        val variable = Variable(id)
        if (type != null) bindings[variable] = type
        graph.addNode(variable)
        return variable
    }

    fun relate(left: Variable, right: Variable, relation: TypeRelation, bidirectional: Boolean = false) {
        graph.addEdge(relation, left, right, bidirectional)
    }

    fun solve() : Map<Variable, TypeRes> {
        return mapOf()
    }
}

/**
 * An [InferenceVariable] is a special [Type] that represents a "hole" in the type system.
 */
class InferenceVariable internal constructor(
        val id: Int,
        /*val context: VariableContext*/
) : Type {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = id

    override fun toString(): String = "~var$id"
}

class Term(val type: Type)

/**
 * The direction(s) an inference constraint's effect applies. This is used to optimize constraint resolution.
 */
enum class Directionality {
    /**
     * The effect propagates from the "right" term to the "left" term
     */
    Left,

    /**
     * The effect propagates from the "left" to the "right" term
     */
    Right,

    /**
     * The effect is mutually applied, from "left" to "right" and "right" to "left".
     */
    Bidirectional
}

data class InferenceConstraint(
        val left: Term,
        val right: Term,
        /*val directionality: Directionality = Directionality.Left,*/
        /*val context: ConstraintContext*/
)

class BoundSet(val variable: InferenceVariable) {
    /**
     * If true, this bound set will resolve to the greatest-lower-bound (GLB) rather than the least-upper-bound (LUB).
     */
    var pushDown: Boolean = false
    val upperBounds: MutableSet<Bound> by lazy { mutableSetOf() }
    val lowerBounds: MutableSet<Bound> by lazy { mutableSetOf() }
    val equivalenceBounds: MutableSet<Bound> by lazy { mutableSetOf() }

    var result: Type? = null

    fun getDependencies(): List<InferenceVariable> = (upperBounds.mapNotNull { it.right.type as? InferenceVariable } +
            lowerBounds.mapNotNull { it.left.type as? InferenceVariable } +
            equivalenceBounds.mapNotNull { it.right.type as? InferenceVariable } +
            equivalenceBounds.mapNotNull { it.left.type as? InferenceVariable }).distinct() - variable

    fun resolve(state: InferenceState): TypeRes {
        val properLowerBounds = lowerBounds.filter { it.left.type !is InferenceVariable }.map { it.left}
        val properUpperBounds = upperBounds.filter { it.right.type !is InferenceVariable }.map { it.right }

        if (pushDown) {
            // Our candidate is the greatest subtype given all lower bounds
            val candidate = state.lattice.leastCommonSupertype(properLowerBounds.map { it.type })
            // Check if it is a subtype of all upper bounds
            val failedBounds = properUpperBounds.filter { !state.lattice.isSubtypeOf(candidate, it.type) }

            if (failedBounds.isEmpty()) {
                return Failure("Type inference for $variable failed: $failedBounds")
            }
            return Success(candidate)
        } else {
            // Our candidate is the least supertype given all lower bounds
            val candidate = state.lattice.greatestCommonSubtype(properUpperBounds.map { it.type })
            // Check if it is a supertype of all lower bounds
            val failedBounds = properLowerBounds.filter { !state.lattice.isSubtypeOf(it.type, candidate) }

            if (failedBounds.isEmpty()) {
                return Failure("Type inference for $variable failed: $failedBounds")
            }
            return Success(candidate)
        }
    }
}

data class Bound(val left: Term, val right: Term)
class InferenceError(val message: String)

interface ReductionRule {

}

interface IncorporationRule {

}

class InferenceState(
        val lattice: TypeLattice,
        val reductionRules: List<ReductionRule>,
        val incorporationRules: List<IncorporationRule>
) {
    val constraints: MutableSet<InferenceConstraint> = mutableSetOf()
    val currentConstraints: MutableList<InferenceConstraint> = mutableListOf()
    val boundSets: MutableMap<InferenceVariable, BoundSet> = mutableMapOf()
    val errors: MutableList<InferenceError> = mutableListOf()

    private var variableCounter = 0
    fun createVariable() : InferenceVariable {
        val variable = InferenceVariable(variableCounter++)
        boundSets[variable] = BoundSet(variable)
        return variable
    }

    fun addConstraint(constraint: InferenceConstraint) {
        if (!constraints.contains(constraint)) {
            constraints += constraint
            currentConstraints += constraint
        }
    }

    fun addBound(bound: Bound) {
        if (bound.left.type is InferenceVariable) {
            boundSets[bound.left.type]!!.upperBounds += bound
        }
        if (bound.right.type is InferenceVariable) {
            boundSets[bound.right.type]!!.lowerBounds += bound
        }

    }
}

fun inferenceAlgorithm(state: InferenceState) {
    // collect constraints
    while (state.currentConstraints.isNotEmpty()) {
        reduce(state)
        incorporate(state)
        resolve(state)
    }

}

fun reduce(state: InferenceState) { state.apply {
    while (currentConstraints.isNotEmpty()) {
        val constraint = currentConstraints.removeFirst()
        val leftType = constraint.left.type
        val rightType = constraint.right.type
        if (!(leftType is InferenceVariable || rightType is InferenceVariable)) {
            if (!lattice.isSubtypeOf(leftType, rightType)) {
                errors += InferenceError("Inference error on $constraint. $leftType is not a subtype of $rightType" )
            }
        } else if (leftType is InferenceVariable) {
            addBound(Bound(constraint.left, constraint.right))
        } else if (rightType is InferenceVariable) {
            addBound(Bound(constraint.left, constraint.right))
        }
    }
} }

fun incorporate(state: InferenceState) { state.apply {

} }

fun resolve(state: InferenceState) { state.apply {
    state.boundSets.forEach { (variable, boundSet) ->
        if (boundSet.getDependencies().isEmpty()) {

        }
    }
} }


sealed class TypeRes
class Success(type: Type) : TypeRes()
class Failure(error: String) : TypeRes()


