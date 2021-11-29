package dev.dialector.semantic.type.inference.new

import dev.dialector.semantic.type.Type

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

interface InferenceVariable : Type {
    val id: String
}

enum class TypeRelation(val symbol: String) {
    SUBTYPE("<"),
    SUPERTYPE(">"),
    EQUIVALENT("=");

    fun opposite(): TypeRelation = when (this) {
        SUBTYPE -> SUPERTYPE
        SUPERTYPE -> SUBTYPE
        EQUIVALENT -> EQUIVALENT
    }

    override fun toString(): String = symbol
}

enum class VariableConstraintKind {
    PULL_UP,
    PUSH_DOWN
}

interface InferenceOrigin

sealed class InferenceConstraint {}

data class VariableConstraint(
    val variable: InferenceVariable,
    val kind: VariableConstraintKind,
) : InferenceConstraint()

data class RelationalConstraint(
    val relation: TypeRelation,
    val left: Type,
    val right: Type,
    val mutual: Boolean = false
) : InferenceConstraint()

interface Bound {
    val variable: InferenceVariable
    val boundingType: Type
    val relation: TypeRelation

    fun lowerType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) variable else boundingType
    fun upperType(): Type = if (relation == TypeRelation.SUBTYPE || relation == TypeRelation.EQUIVALENT) boundingType else variable
}

interface InferenceConstraintSystem {
    fun getInferenceVariables(): Set<InferenceVariable>

    fun getInferenceConstraints(): Set<InferenceConstraint>
}

interface InferenceSolver {
    fun solve(constraintSystem: InferenceConstraintSystem): InferenceResult
}

interface InferenceVariableSolution

interface InferenceResult {
    operator fun get(variable: InferenceVariable): List<Type>?
}

interface InferenceContext {
    fun typeVar(): InferenceVariable
    fun constraint(routine: ConstraintCreator.() -> InferenceConstraint)
}

/**
 * An API for creating constraints.
 */
interface ConstraintCreator {
    /**
     * Indicates that the variable's optimal solution should resolve using its upper bounds.
     */
    fun pullUp(variable: InferenceVariable): VariableConstraint

    /**
     * Indicates that the variable's optimal solution should resolve using its lower bounds.
     */
    fun pushDown(variable: InferenceVariable): VariableConstraint

    /**
     * Creates a constraint between the two types with the context [TypeRelation]
     */
    fun relate(relation: TypeRelation, left: Type, right: Type): RelationalConstraint

    /**
     * Indicates that two types should be considered equivalent.
     */
    infix fun Type.equal(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a subtype of the right-hand-side type.
     */
    infix fun Type.subtype(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a supertype of the right-hand-side type.
     */
    infix fun Type.supertype(type: Type): RelationalConstraint
}

object SimpleConstraintCreator : ConstraintCreator {
    override fun pullUp(variable: InferenceVariable): VariableConstraint =
        VariableConstraint(variable, VariableConstraintKind.PULL_UP)

    override fun pushDown(variable: InferenceVariable): VariableConstraint =
        VariableConstraint(variable, VariableConstraintKind.PUSH_DOWN)

    override fun relate(relation: TypeRelation, left: Type, right: Type): RelationalConstraint =
        RelationalConstraint(relation, left, right)

    override fun Type.equal(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.EQUIVALENT, this, type)

    override fun Type.subtype(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.SUBTYPE, this, type)

    override fun Type.supertype(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.SUPERTYPE, this, type)
}