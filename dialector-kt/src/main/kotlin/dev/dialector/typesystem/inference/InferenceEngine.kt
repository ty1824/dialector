package dev.dialector.typesystem.inference

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice

sealed class InferenceTerm {
    abstract val id: Int
}

data class TypeTerm(override val id: Int, val type: dev.dialector.typesystem.Type) : InferenceTerm()

data class VariableTerm(override val id: Int) : InferenceTerm()

sealed class InferenceResult {
    /**
     * A result of OK indicates that the rule was applied successfully but no type was resolved.
     */
    object Ok : InferenceResult()
    data class TypeResult(val type: Type) : InferenceResult()
    data class Error(val reason: String) : InferenceResult()
    data class UnifyError(val left: InferenceTerm, val right: InferenceTerm) : InferenceResult()
}



sealed class Relation() {
    abstract val left: InferenceTerm
    abstract val right: InferenceTerm
}

data class Equality(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()
data class Subtype(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()
data class Supertype(override val left: InferenceTerm, override val right: InferenceTerm) : Relation()

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
 *     Group(1', 2', 3', 4')
 *
 * This structure includes type bounds.
 */
interface InferenceGroup {
    val variableTerms: Set<VariableTerm>
    val typeTerms: Set<TypeTerm>
    val upperBounds: Set<InferenceGroup>
    val lowerBounds: Set<InferenceGroup>
}

interface InferenceSystem {
    /**
     * The TypeLattice used to resolve relations.
     */
    val lattice: TypeLattice

    /**
     * Creates a VariableTerm
     */
    fun varTerm(): VariableTerm

    /**
     * Creates a TypeTerm
     */
    fun asTerm(type: Type): TypeTerm

    // TODO: Find out how to handle internal/external representation of Group
    fun getInferenceGroups() : Map<VariableTerm, InferenceGroup>

    /**
     * Register equality between two terms
     */
    fun equals(left: InferenceTerm, right: InferenceTerm): InferenceResult

    /**
     * Indicates that the two variables are equivalent. Returns an error if the variables are already equivalent to
     * types that are not also equivalent.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variables are not compatible.
     */
    fun equals(left: VariableTerm, right: VariableTerm): InferenceResult

    /**
     * Indicates that the variable is equal to the type. Returns an error if the variable is already equivalent to a type that
     * is not equivalent to the given type.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variable is not compatible with the type.
     */
    fun equals(left: VariableTerm, right: TypeTerm): InferenceResult

    /**
     * Indicates that the type is equal to the variable. Returns an error if the variable is already equivalent to a type that
     * is not equivalent to the given type.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variable is not compatible with the type.
     */
    fun equals(left: TypeTerm, right: VariableTerm): InferenceResult

    /**
     * Compares the two type terms. Returns an error if the two types are not equivalent according to the TypeLattice.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.Error if the types are not equivalent
     */
    fun equals(left: TypeTerm, right: TypeTerm): InferenceResult

    /**
     * Indicates that the left term must be a subtype of the right term (left <= right)
     */
    fun subtype(left: VariableTerm, right: VariableTerm): InferenceResult

    /**
     * Indicates that the left term must be a supertype of the right term (left >= right)
     */
    fun supertype(left: VariableTerm, right: VariableTerm): InferenceResult
}



interface TypeResult {
    data class Success(val type: Type): TypeResult

    interface Error: TypeResult {
        object NoTypeInferred: Error
        data class TooManyBoundTypes(val terms: Iterable<TypeTerm>) : Error
        data class DoesNotMatchBounds(val type: TypeTerm, val unmatchedLower: Iterable<Type>, val unmatchedUpper: Iterable<Type>) : Error
    }
}

interface InferenceSolver {
    fun solve(system: InferenceSystem): Map<VariableTerm, TypeResult>
}

