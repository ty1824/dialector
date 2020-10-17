package dev.dialector.typesystem.inference.new

import dev.dialector.typesystem.Type
import javax.management.relation.Relation

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

interface InferenceLocation

interface InferenceVariable : Type {
    val id: String
}

enum class TypeRelation {
    SUBTYPE,
    SUPERTYPE,
    EQUIVALENT
}

enum class VariableConstraintKind {
    PULL_UP,
    PUSH_DOWN
}

sealed class InferenceConstraint

data class VariableConstraint(
    val variable: InferenceVariable,
    val kind: VariableConstraintKind) : InferenceConstraint()

data class RelationalConstraint(
    val relation: TypeRelation,
    val left: Type,
    val right: Type
) : InferenceConstraint()

interface Bound {
    val variable: InferenceVariable
    val boundingType: Type
    val relation: TypeRelation
}

interface InferenceSystem {
    fun getInferenceVariables(): Set<InferenceVariable>

    fun getInferenceConstraints(): Set<InferenceConstraint>


//    /**
//     * Register equality between two terms.
//     *
//     * Returns an error if:
//     * - The left and right type are proper types or resolved type variables that are not equivalent.
//     *
//     * @return InferenceResult.Ok if successful or InferenceResult.Error if the types can not be equivalent.
//     */
//    fun equals(left: Type, right: Type): InferenceResult
//
//    /**
//     * Indicates that the left term must be a subtype of the right term (left <= right)
//     */
//    fun subtype(left: Type, right: Type): InferenceResult
//
//    /**
//     * Indicates that the left term must be a supertype of the right term (left >= right)
//     */
//    fun supertype(left: Type, right: Type): InferenceResult
}

interface InferenceSolver {
    fun solve(): InferenceResult
}

interface InferenceResult {
    fun get(variable: InferenceVariable): List<Type>?
}

interface InferenceContext {
    fun typeVar(): InferenceVariable
    fun constraint(routine: ConstraintCreator.() -> RelationalConstraint)
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

class SimpleConstraintCreator : ConstraintCreator {
    override fun pullUp(variable: InferenceVariable): VariableConstraint =
        VariableConstraint(variable, VariableConstraintKind.PULL_UP)

    override fun pushDown(variable: InferenceVariable): VariableConstraint =
        VariableConstraint(variable, VariableConstraintKind.PUSH_DOWN)

    override fun Type.equal(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.EQUIVALENT, this, type)

    override fun Type.subtype(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.SUBTYPE, this, type)

    override fun Type.supertype(type: Type): RelationalConstraint =
        RelationalConstraint(TypeRelation.SUPERTYPE, this, type)
}