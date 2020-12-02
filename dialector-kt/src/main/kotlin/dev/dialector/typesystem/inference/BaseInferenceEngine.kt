package dev.dialector.typesystem.inference

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice
import java.util.concurrent.atomic.AtomicInteger

/*
Java type inference algorithm:

    Resolution:
        
        Given a set of variables V
            If there are

 */


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

internal class IncrementalInferenceSystem(override val lattice: TypeLattice) : InferenceSystem {
    override fun varTerm(): VariableTerm {
        TODO("Not yet implemented")
    }

    override fun asTerm(type: Type): TypeTerm {
        TODO("Not yet implemented")
    }

    override fun getInferenceGroups(): Map<VariableTerm, InferenceGroup> {
        TODO("Not yet implemented")
    }

    override fun equals(left: InferenceTerm, right: InferenceTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun equals(left: VariableTerm, right: VariableTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun equals(left: VariableTerm, right: TypeTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun equals(left: TypeTerm, right: VariableTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun equals(left: TypeTerm, right: TypeTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun subtype(left: VariableTerm, right: VariableTerm): InferenceResult {
        TODO("Not yet implemented")
    }

    override fun supertype(left: VariableTerm, right: VariableTerm): InferenceResult {
        TODO("Not yet implemented")
    }
}

internal class BaseInferenceSystem(override val lattice: TypeLattice) : InferenceSystem {
    private var termIndex: AtomicInteger = AtomicInteger(0)

    private val groups: MutableMap<VariableTerm, MutableInferenceGroup> = mutableMapOf()

    /**
     * Retrieves the relation group that corresponds to this type variable, or creates one if not present.
     */
    private fun getRelationGroup(variable: VariableTerm): MutableInferenceGroup =
        groups.computeIfAbsent(variable) { MutableInferenceGroup(setOf(variable)) }

    override fun varTerm(): VariableTerm = VariableTerm(termIndex.getAndIncrement())

    override fun asTerm(type: Type): TypeTerm = TypeTerm(termIndex.getAndIncrement(), type)

    override fun getInferenceGroups(): Map<VariableTerm, InferenceGroup> = this.groups.mapValues { it.value }

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
    override fun subtype(left: VariableTerm, right: VariableTerm): InferenceResult {
        val leftGroup = getRelationGroup(left)
        val rightGroup = getRelationGroup(right)

        // Check if this is invalid given existing state
//        if (leftGroup.)

        leftGroup.upperBounds += rightGroup
        rightGroup.lowerBounds += leftGroup

        return InferenceResult.Ok
    }

    /**
     * Indicates that the left term must be a supertype of the right term (left >= right)
     */
    override fun supertype(left: VariableTerm, right: VariableTerm): InferenceResult {
        val leftGroup = getRelationGroup(left)
        val rightGroup = getRelationGroup(right)
        leftGroup.lowerBounds += rightGroup
        rightGroup.upperBounds += leftGroup

        return InferenceResult.Ok
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

    override fun toString(): String {
        return StringBuilder("Inference System State:\n").let { builder ->
            var i = 0
            this.groups.values.distinct().forEach {
                builder.append("Group ${i++} (${it}):\n")
                builder.append("  Type terms:${it.typeTerms}\n")
                builder.append("  Variable terms:${it.variableTerms}\n")
                builder.append("  Upper Bounds:${it.upperBounds}\n")
                builder.append("  Lower Bounds:${it.lowerBounds}\n")
            }
            builder
        }.toString()
    }

}

object DefaultInferenceSolver : InferenceSolver {
    override fun solve(system: InferenceSystem): Map<VariableTerm, TypeResult> {
        val typeMap: MutableMap<InferenceGroup, TypeResult> = mutableMapOf()

        val remaining = system.getInferenceGroups().values.toMutableList()


        while (remaining.isNotEmpty()) {
            val numRemaining = remaining.size
            val iter = remaining.listIterator()
            // TODO: May need to do this in two stages - compute candidate types then check bounds? Hard to ensure bounds are correct until we've computed everything :)
            while (iter.hasNext()) {
                val group = iter.next()
                val typeTerms = group.typeTerms

                if (typeTerms.isNotEmpty()) {
                    // Dealing with a concrete type
                    typeMap[group] = if (typeTerms.size == 1) {
                        val term = typeTerms.first()
                        val unmatchedBounds = group.unmatchedBounds(term, typeMap, system)
                        if (unmatchedBounds.first.isEmpty() && unmatchedBounds.second.isEmpty()) {
                           TypeResult.Success(term.type)
                        } else {
                            TypeResult.Error.DoesNotMatchBounds(term, unmatchedBounds.first, unmatchedBounds.second)
                        }
                    } else {
                        TypeResult.Error.TooManyBoundTypes(typeTerms)
                    }
                    iter.remove()
                } else if (group.upperBounds.isEmpty() && group.lowerBounds.isEmpty()) {
                    // No concrete term & no bounds
                    typeMap[group] = TypeResult.Error.NoTypeInferred
                    iter.remove()
                } else {
                    // Dealing with an inferred type
                    if (group.boundsResolved(typeMap)) {
                        typeMap[group] = if (group.upperBounds.isNotEmpty()) {
                            // Resolve using upper bounds
                            val candidate = system.lattice.greatestCommonSubtype(group.upperBounds.map { (typeMap[it] as? TypeResult.Success)!!.type })
                            // TODO: Validate lower bounds
                            TypeResult.Success(candidate)
                        } else {
                            // Resolve using lower bounds
                            val candidate = system.lattice.leastCommonSupertype(group.upperBounds.map { (typeMap[it] as? TypeResult.Success)!!.type })
                            TypeResult.Success(candidate)
                        }
                        iter.remove()
                    }
                }

            }


            if (remaining.size == numRemaining) {
                // TODO: We haven't resolved anything this step. All remaining groups must apply inference or report an error
                remaining.forEach {
                    typeMap[it] = TypeResult.Error.NoTypeInferred
                }
                remaining.clear()
            }
        }

        return typeMap.keys.flatMap { group ->
            val type = typeMap[group]?.let { it } ?: TypeResult.Error.NoTypeInferred
            group.variableTerms.map { it to type }
        }.toMap()
    }

    private fun InferenceGroup.unmatchedBounds(term: TypeTerm, typeMap: Map<InferenceGroup, TypeResult>, system: InferenceSystem): Pair<Set<Type>, Set<Type>> {
        // TODO: Check if resolved term does not match any bounds
        val lowerBounds = this.lowerBounds.mapNotNull { (typeMap[it] as? TypeResult.Success)?.type }.filter { !system.lattice.isSubtypeOf(it, term.type) }.toSet()
        val upperBounds = this.upperBounds.mapNotNull { (typeMap[it] as? TypeResult.Success)?.type }.filter { !system.lattice.isSubtypeOf(term.type, it) }.toSet()
        return Pair(lowerBounds, upperBounds)
    }

    private fun InferenceGroup.boundsResolved(typeMap: Map<InferenceGroup, TypeResult>): Boolean {
        return this.upperBounds.all(typeMap::containsKey) && this.lowerBounds.all(typeMap::containsKey)
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