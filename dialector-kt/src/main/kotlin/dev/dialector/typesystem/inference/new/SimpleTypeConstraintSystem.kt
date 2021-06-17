package dev.dialector.typesystem.inference.new

/**
 * A type constraint system that is aware of program semantics
 */
class SemanticTypeConstraintSystem {
    private val variables: MutableSet<InferenceVariable> = mutableSetOf()
    private val inferenceConstraints: MutableSet<InferenceConstraint> = mutableSetOf()
}