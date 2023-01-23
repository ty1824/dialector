package dev.dialector.semantic

import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.inference.new.VariableConstraintKind

public interface TypeVariable : Type, SemanticVariable {
    public val id: String
}

public enum class TypeRelation(public val symbol: String) {
    SUBTYPE("<"),
    SUPERTYPE(">"),
    EQUIVALENT("=");

    public fun opposite(): TypeRelation = when (this) {
        SUBTYPE -> SUPERTYPE
        SUPERTYPE -> SUBTYPE
        EQUIVALENT -> EQUIVALENT
    }

    override fun toString(): String = symbol
}

public enum class VariableConstraintKind {
    PULL_UP,
    PUSH_DOWN
}

/**
 * A constraint indicating how a type variable should be resolved.
 *
 * PULL_UP indicates the result should be the lowest common supertype of all upper bounds.
 * PUSH_DOWN indicates the result should be the greatest common supertype of all lower bounds.
 */
public data class TypeVariableConstraint(
    val variable: TypeVariable,
    val kind: VariableConstraintKind,
) : SemanticConstraint

/**
 * A constraint describing a relation between two types.
 *
 * If mutual is false, the relation is assumed to propagate only from the left to the right.
 * If mutual is true, the relation propagates bidirectionally.
 */
public data class TypeRelationConstraint(
    val relation: TypeRelation,
    val left: Type,
    val right: Type,
    val mutual: Boolean = false
) : SemanticConstraint

public object Types : ConstraintCreator {
    /**
     * Indicates that the variable's optimal solution should resolve using its upper bounds.
     */
    public fun pullUp(variable: TypeVariable): TypeVariableConstraint =
        TypeVariableConstraint(variable, VariableConstraintKind.PULL_UP)

    /**
     * Indicates that the variable's optimal solution should resolve using its lower bounds.
     */
    public fun pushDown(variable: TypeVariable): TypeVariableConstraint =
        TypeVariableConstraint(variable, VariableConstraintKind.PUSH_DOWN)

    /**
     * Creates a constraint between the two types with the context [TypeRelation]
     */
    public fun relate(left: Type, relation: TypeRelation, right: Type): TypeRelationConstraint =
        TypeRelationConstraint(relation, left, right)

    /**
     * Indicates that two types should be considered equivalent.
     */
    public infix fun Type.equal(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.EQUIVALENT, this, type)

    /**
     * Indicates that the left-hand-side type should be a subtype of the right-hand-side type.
     */
    public infix fun Type.subtype(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.SUBTYPE, this, type)

    /**
     * Indicates that the left-hand-side type should be a supertype of the right-hand-side type.
     */
    public infix fun Type.supertype(type: Type): TypeRelationConstraint =
        TypeRelationConstraint(TypeRelation.SUPERTYPE, this, type)
}