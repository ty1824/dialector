package dev.dialector.typesystem.inference

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice
import java.util.concurrent.atomic.AtomicInteger

sealed class InferenceTerm {
    abstract val id: Int
}

data class TypeTerm(override val id: Int, val type: dev.dialector.typesystem.Type) : InferenceTerm()

data class VariableTerm(override val id: Int) : InferenceTerm()

sealed class InferenceResult {
    data class TypeResult(val type: Type) : InferenceResult()
    object Ok : InferenceResult()
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
 *     Group(1', 2' 3', 4')
 *
 * This structure includes type bounds.
 */
interface InferenceGroup {
    val variableTerms: Set<VariableTerm>
    val typeTerms: Set<TypeTerm>
    val upperBounds: Set<InferenceGroup>
    val lowerBounds: Set<InferenceGroup>
}

/**
 * Represents internal structure, should not be exposed without finalizing.
 */
internal class MutableInferenceGroup(
    variableTerms: Set<VariableTerm>,
    typeTerms: Set<TypeTerm> = setOf(),
    upperBounds: Set<MutableInferenceGroup> = setOf(),
    lowerBounds: Set<MutableInferenceGroup> = setOf()
) : InferenceGroup {
    override val variableTerms: MutableSet<VariableTerm> = variableTerms.toMutableSet()
    override val typeTerms: MutableSet<TypeTerm> = typeTerms.toMutableSet()
    override val upperBounds: MutableSet<MutableInferenceGroup> = upperBounds.toMutableSet()
    override val lowerBounds: MutableSet<MutableInferenceGroup> = lowerBounds.toMutableSet()

    fun finalize(): InferenceGroup = object : InferenceGroup {
        override val variableTerms: Set<VariableTerm> = this@MutableInferenceGroup.variableTerms.toSet()
        override val typeTerms: Set<TypeTerm> = this@MutableInferenceGroup.typeTerms.toSet()
        override val upperBounds: Set<InferenceGroup> = this@MutableInferenceGroup.upperBounds.toSet()
        override val lowerBounds: Set<InferenceGroup> = this@MutableInferenceGroup.lowerBounds.toSet()
    }
}

interface InferenceContext {
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
    fun subtype(left: VariableTerm, right: VariableTerm)

    /**
     * Indicates that the left term must be a supertype of the right term (left >= right)
     */
    fun supertype(left: VariableTerm, right: VariableTerm)
}

internal class BaseInferenceContext(override val lattice: TypeLattice) : InferenceContext {
    private var termIndex: AtomicInteger = AtomicInteger(0)

    private val groups: MutableMap<VariableTerm, MutableInferenceGroup> = mutableMapOf()

    /**
     * Retrieves the relation group that corresponds to this type variable, or creates one if not present.
     */
    private fun getRelationGroup(variable: VariableTerm): MutableInferenceGroup =
        groups.computeIfAbsent(variable) { MutableInferenceGroup(setOf(variable)) }

    override fun varTerm(): VariableTerm = VariableTerm(termIndex.getAndIncrement())

    override fun asTerm(type: Type): TypeTerm = TypeTerm(termIndex.getAndIncrement(), type)

    override fun getInferenceGroups(): Map<VariableTerm, InferenceGroup> = this.groups.mapValues { it.value.finalize() }

    /**
     * Register equality between two terms
     */
    override fun equals(left: InferenceTerm, right: InferenceTerm): InferenceResult =
        when (left) {
            is VariableTerm -> when (right) {
                // variable == variable
                is VariableTerm -> equals(left, right)
                // variable == type
                is TypeTerm -> equals(left, right)
            }
            is TypeTerm -> when(right) {
                // type == variable
                is VariableTerm -> equals(left, right)
                // type == type
                is TypeTerm -> equals(left, right)
            }
//            // variable == variable
//            left is VariableTerm && right is VariableTerm -> {
//                getRelationGroup(left).unify(getRelationGroup(right))
//            }
//            // variable == type
//            left is VariableTerm && right is TypeTerm -> {
//                getRelationGroup(left).unify(right)
//            }
//            // type == variable
//            left is TypeTerm && right is VariableTerm -> {
//                getRelationGroup(right).unify(left)
//            }
//            // type == type
//            left is TypeTerm && right is TypeTerm -> {
//                if (lattice.isEquivalent(left.type, right.type)) {
//                    InferenceResult.Ok
//                } else InferenceResult.Error("Types not equivalent: ${left.type} and ${right.type}")
//            }
        }

    /**
     * Indicates that the two variables are equivalent. Returns an error if the variables are already equivalent to
     * types that are not also equivalent.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variables are not compatible.
     */
    override fun equals(left: VariableTerm, right: VariableTerm): InferenceResult =
        if (getRelationGroup(left).unify(getRelationGroup(right)))
            InferenceResult.Ok
        else
            InferenceResult.UnifyError(left, right)

    /**
     * Indicates that the variable is equal to the type. Returns an error if the variable is already equivalent to a type that
     * is not equivalent to the given type.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variable is not compatible with the type.
     */
    override fun equals(left: VariableTerm, right: TypeTerm): InferenceResult =
        if (getRelationGroup(left).unify(right))
            InferenceResult.Ok
        else
            InferenceResult.UnifyError(left, right)


    /**
     * Indicates that the type is equal to the variable. Returns an error if the variable is already equivalent to a type that
     * is not equivalent to the given type.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.UnifyError if the variable is not compatible with the type.
     */
    override fun equals(left: TypeTerm, right: VariableTerm): InferenceResult =
        if (getRelationGroup(right).unify(left))
            InferenceResult.Ok
        else
            InferenceResult.UnifyError(left, right)


    /**
     * Compares the two type terms. Returns an error if the two types are not equivalent according to the TypeLattice.
     *
     * @return InferenceResult.Ok if successful or InferenceResult.Error if the types are not equivalent
     */
    override fun equals(left: TypeTerm, right: TypeTerm): InferenceResult =
            if (left.unify(right))
                InferenceResult.Ok
            else
                InferenceResult.Error("Types not equivalent: ${left.type} and ${right.type}")

    /**
     * Indicates that the left term must be a subtype of the right term (left <= right)
     */
    override fun subtype(left: VariableTerm, right: VariableTerm) {
        getRelationGroup(left).upperBounds += getRelationGroup(right)
    }

    /**
     * Indicates that the left term must be a supertype of the right term (left >= right)
     */
    override fun supertype(left: VariableTerm, right: VariableTerm) {
        getRelationGroup(left).lowerBounds += getRelationGroup(right)
    }

    private fun MutableInferenceGroup.unify(other: MutableInferenceGroup): Boolean =
        if (this.typeTerms.all { left -> other.typeTerms.all { right -> lattice.isEquivalent(left.type, right.type) } }) {
            // Merge the Groups together
            this.variableTerms += other.variableTerms
            this.typeTerms += other.typeTerms
            this.upperBounds += other.upperBounds
            this.lowerBounds += other.lowerBounds
            // Replace usages of the right Group with the left Group
            other.variableTerms.forEach { groups[it] = this }
            true
        } else false

    private fun MutableInferenceGroup.unify(other: TypeTerm): Boolean =
        if (this.typeTerms.all { lattice.isEquivalent(it.type, other.type) }) {
            this.typeTerms += other
            true
        } else false

    private fun TypeTerm.unify(other: TypeTerm): Boolean =
        lattice.isEquivalent(this.type, other.type)


}

sealed class TypeResult {
    data class Success(val type: Type): TypeResult()
    data class Error(val reason: String): TypeResult()
}

interface InferenceSolver {
    fun solve(context: InferenceContext): Map<VariableTerm, TypeResult>
}

object DefaultInferenceSolver : InferenceSolver {
    override fun solve(context: InferenceContext): Map<VariableTerm, TypeResult> {
        val typeMap: MutableMap<InferenceGroup, TypeResult> = mutableMapOf()

        // Handle types without inequalities and isolate inequalities
        var inequalities: MutableSet<InferenceGroup> = context.getInferenceGroups()
            .values
            .filter { group ->
                val typeTerms = group.typeTerms
                if (typeTerms.size > 0) {
                    if (typeTerms.size == 1) {
                        val groupType = typeTerms.first().type
                        typeMap[group] = TypeResult.Success(groupType)
                        false
                    } else {
                        typeMap[group] = TypeResult.Error("Too many bound types: $typeTerms")
                        false
                    }
                } else true
            }.toMutableSet()


//        while (inequalities.isNotEmpty()) {
//            val current = inequalities.first()
//            val upperBound = current.upperBounds.
//
//
//        }

        return typeMap.keys.flatMap { group ->
            val type = typeMap[group]?.let { it } ?: TypeResult.Error("No type inferred")
            group.variableTerms.map { it to type }
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