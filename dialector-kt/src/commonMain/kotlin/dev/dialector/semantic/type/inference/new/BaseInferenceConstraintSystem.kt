package dev.dialector.semantic.type.inference.new

import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.inference.DataGraph

object InferredTopType : IdentityType("InferredTop")

object InferredBottomType : IdentityType("InferredBottom")

data class InferredGreatestLowerBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredUnion<${types.joinToString(" | ")}>"
}

data class InferredLeastUpperBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredIntersection<${types.joinToString(" & ")}>"
}

interface BoundCreator {
    infix fun InferenceVariable.exactBound(type: Type)
    infix fun InferenceVariable.upperBound(type: Type)
    infix fun InferenceVariable.lowerBound(type: Type)
}

object SimpleBoundCreator : BoundCreator {
    override fun InferenceVariable.exactBound(type: Type) {
        BaseBound(TypeRelation.EQUIVALENT, this, type)
    }

    override fun InferenceVariable.upperBound(type: Type) {
        BaseBound(TypeRelation.SUPERTYPE, this, type)
    }

    override fun InferenceVariable.lowerBound(type: Type) {
        BaseBound(TypeRelation.SUBTYPE, this, type)
    }
}

interface ReductionContext {
    fun constraint(routine: ConstraintCreator.() -> RelationalConstraint)
    fun bound(routine: BoundCreator.() -> Bound)
}

interface ReductionRule {
    val isValidFor: RelationalConstraintClause
    val reduce: ReductionRoutine
}

typealias RelationalConstraintClause = (RelationalConstraint) -> Boolean
typealias ReductionRoutine = ReductionContext.(constraint: RelationalConstraint) -> Unit

private class SimpleReductionRule(
    override val isValidFor: RelationalConstraintClause,
    override val reduce: ReductionRoutine
) : ReductionRule

infix fun RelationalConstraintClause.reducesTo(routine: ReductionRoutine): ReductionRule = SimpleReductionRule(this, routine)

val redundantElimination: ReductionRule =
    { constraint: RelationalConstraint -> constraint.left == constraint.right } reducesTo {
        /* Nothing */
    }

val leftReduction: ReductionRule =
    { constraint: RelationalConstraint -> constraint.left is InferenceVariable } reducesTo {
        bound { BaseBound(it.relation, it.left as InferenceVariable, it.right)}
    }
val rightReduction: ReductionRule =
    { constraint: RelationalConstraint -> constraint.right is InferenceVariable } reducesTo {
        bound { BaseBound(it.relation.opposite(), it.right as InferenceVariable, it.left)}
    }

private class BaseInferenceVariable(override val id: String) : InferenceVariable {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "tv.$id"
}

data class BaseBound(
    override val relation: TypeRelation,
    override val variable: InferenceVariable,
    override val boundingType: Type,
) : Bound

sealed class BoundGraphNode {
    abstract val type: Type
    abstract fun isProper(): Boolean
}

class TypeNode(override val type: Type) : BoundGraphNode() {
    override fun isProper(): Boolean = true
}

class VariableNode(val variable: InferenceVariable) : BoundGraphNode() {
    val equivalentTo: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    val upperBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    val lowerBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    /**
     * Determines whether the solution for this variable should be the greatest lower bound rather than the least upper bound.
     */
    var pushDown: Boolean = false

    override val type: Type
        get() = variable

    fun getDependencies(): Sequence<VariableNode> =
        if (pushDown) {
            lowerBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } else {
            upperBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } + equivalentTo.asSequence().filter {
            !it.first.isProper()
        }.map {
            it.first as VariableNode
        }

    override fun isProper(): Boolean = equivalentTo.any { it.first is TypeNode }
}

class BoundSystemGraph {
    private val graph: DataGraph<BoundGraphNode, Bound> = DataGraph()
    private val typeNodes: MutableMap<Type, TypeNode> = mutableMapOf()
    val variableNodes: MutableMap<InferenceVariable, VariableNode> = mutableMapOf()

    fun addVariable(variable: InferenceVariable, pushDown: Boolean = false) {
        val data = nodeFor(variable)
        data.pushDown = pushDown
        graph.addNode(data)
    }

    fun addBound(bound: Bound, origin: InferenceOrigin) {
        val lower = nodeFor(bound.lowerType())
        val upper = nodeFor(bound.upperType())
        graph.addEdge(bound, lower, upper, true)
        if (bound.relation != TypeRelation.EQUIVALENT) {
            if (lower is VariableNode) lower.upperBounds += upper to bound
            if (upper is VariableNode) upper.lowerBounds += lower to bound
        } else {
            if (lower is VariableNode) lower.equivalentTo += upper to bound
            if (upper is VariableNode) upper.equivalentTo += lower to bound
        }
    }

    fun setPushDown(variable: InferenceVariable, pushDown: Boolean) {
        nodeFor(variable).pushDown = pushDown
    }

    fun getAllVariables(): Sequence<VariableNode> = variableNodes.values.asSequence()

    fun isResolved(variable: InferenceVariable): Boolean = nodeFor(variable).isProper()

    fun getUnresolvedVariables(): Sequence<VariableNode> =
        variableNodes.values.asSequence()
            .filter { !it.isProper() }

    fun getBounds(variable: InferenceVariable): Sequence<Bound> = graph.getEdges(nodeFor(variable)).map { it.data }

    fun getDependencyGroups(): Sequence<Set<VariableNode>> = sequence {
        val simpleNodes = getUnresolvedVariables().filter {
            it.getDependencies().count() == 0
        }.toList()

        // Return nodes with no dependencies first
        if (simpleNodes.isNotEmpty()) {
            for (node in simpleNodes) {
                yield(setOf(node))
            }
        }

        // Form dependency groups
    }

    private fun nodeFor(type: Type): BoundGraphNode =
        if (type is InferenceVariable) {
            variableNodes.computeIfAbsent(type) { VariableNode(type) }
        } else {
            typeNodes.computeIfAbsent(type) { TypeNode(type) }
        }

    private fun nodeFor(variable: InferenceVariable): VariableNode =
        variableNodes.computeIfAbsent(variable) { VariableNode(variable) }

    override fun toString(): String {
        val builder = StringBuilder("Bounds:\n")

        this.graph.getAllEdges().groupBy { it.data.lowerType() }.forEach { (_, value) ->
            value.forEach {
                if (it.data.variable == it.data.lowerType()) {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation} ${it.data.upperType()}\n")
                } else {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation.opposite()} ${it.data.upperType()}\n")
                }
            }
        }

        return builder.toString()
    }
}

class BaseInferenceContext(
    val createVariable: () -> InferenceVariable,
    val addConstraint: (InferenceConstraint) -> Unit
) : InferenceContext, ConstraintCreator by SimpleConstraintCreator {
    override fun typeVar(): InferenceVariable = createVariable()

    override fun constraint(routine: ConstraintCreator.() -> InferenceConstraint) {
        addConstraint(routine())
    }
}

data class ReducedFromConstraint(val constraint: InferenceConstraint, val rule: ReductionRule) : InferenceOrigin

class BaseReductionContext(
    constraint: RelationalConstraint,
    rule: ReductionRule,
    private val addConstraint: (RelationalConstraint, InferenceOrigin) -> Unit,
    private val addBound: (Bound, InferenceOrigin) -> Unit
) : ReductionContext {
    private val origin = ReducedFromConstraint(constraint, rule)
    override fun constraint(routine: ConstraintCreator.() -> RelationalConstraint) {
        addConstraint(SimpleConstraintCreator.routine(), origin)
    }

    override fun bound(routine: BoundCreator.() -> Bound) {
        addBound(SimpleBoundCreator.routine(), origin)
    }
}

class ConstraintSystem(initialConstraints: Iterable<InferenceConstraint>) {
    private val seenConstraints: MutableSet<InferenceConstraint> = initialConstraints.toMutableSet()
    private val unresolvedConstraints: MutableList<InferenceConstraint> = initialConstraints.toMutableList()

    fun add(constraint: InferenceConstraint) {
        if (!seenConstraints.contains(constraint)) {
            seenConstraints += constraint
            unresolvedConstraints += constraint
        }
    }

    fun reduce(reducer: (InferenceConstraint) -> Unit) {
        while (anyUnresolved()) {
            reducer(unresolvedConstraints.removeFirst())
        }
    }

    fun anyUnresolved(): Boolean = unresolvedConstraints.isNotEmpty()

    override fun toString(): String = "Constraints:\n\t${unresolvedConstraints.joinToString("\n\t")}"
}

class BaseInferenceConstraintSystem : InferenceConstraintSystem {
    private val variables: MutableSet<InferenceVariable> = mutableSetOf()
    private val inferenceConstraints: MutableSet<InferenceConstraint> = mutableSetOf()
    private var variableIdCounter = 0

    override fun getInferenceVariables(): Set<InferenceVariable> = variables.toSet()

    override fun getInferenceConstraints(): Set<InferenceConstraint> = inferenceConstraints.toSet()

    fun createVariable(): InferenceVariable = BaseInferenceVariable("${variableIdCounter++}")

    fun registerConstraint(constraint: InferenceConstraint) {
        inferenceConstraints += constraint
    }

    fun solve(reductionRules: List<ReductionRule>) : InferenceResult {
        println("Solving")
        val initialConstraints = getInferenceConstraints()
        println(initialConstraints)
        val constraints = ConstraintSystem(initialConstraints)

        val bounds = BoundSystemGraph()
        getInferenceVariables().forEach(bounds::addVariable)

        var iteration = 0
        while (constraints.anyUnresolved()) {
            iteration++
            var subIteration = 0
            while (constraints.anyUnresolved()) {
                subIteration++
                println("ITERATION: ${iteration}.${subIteration} - Constraints")
                println(constraints)
                println(bounds)
                reduce(reductionRules, constraints, bounds)
                println("ITERATION: ${iteration}.${subIteration} - Incorporation")
                println(constraints)
                println(bounds)
                incorporate(constraints, bounds)
            }
            println("ITERATION $iteration - Solving")
            println(constraints)
            println(bounds)
            resolve(constraints, bounds)
        }
        println("Solving complete")
        // TODO: Verify initial constraints are satisfied by the solution
        return object : InferenceResult {
            val resultTable = bounds.variableNodes.map { (key, value) ->
                key to value.equivalentTo.filter { it.first is TypeNode }.map { it.first.type }.toList()
            }.toMap()
            override fun get(variable: InferenceVariable): List<Type>? = resultTable[variable]
        }
    }

    private fun reduce(
        reductionRules: List<ReductionRule>,
        constraints: ConstraintSystem,
        bounds: BoundSystemGraph
    ) {
        constraints.reduce { constraint ->
            when (val currentConstraint = constraint) {
                is RelationalConstraint ->
                    // Apply each rule if valid for this constraint
                    reductionRules.firstOrNull {
                        it.isValidFor(currentConstraint)
                    }?.apply {
                        val reductionContext = BaseReductionContext(
                            currentConstraint,
                            this,
                            { constraint, _ ->
                                constraints.add(constraint)
                            },
                            { bound, origin ->
                                bounds.addBound(bound, origin)
                            }
                        )
                        reductionContext.reduce(currentConstraint)
                    }
                is VariableConstraint ->
                    bounds.setPushDown(currentConstraint.variable , currentConstraint.kind == VariableConstraintKind.PUSH_DOWN)
            }
        }
    }

    private fun incorporate(
        constraints: ConstraintSystem,
        boundSystem: BoundSystemGraph
    ) {
        SimpleConstraintCreator.apply {
            // TODO: Handle constraint origin once supported
            // For each bound system
            for (variable in boundSystem.getAllVariables()) {
                // Transitive incorporation (if a == b && b == c then a == c), etc
                variable.equivalentTo.forEach { equivalentVar ->
                    // if (x == a) && (l < x) then (l < a)
                    variable.lowerBounds.forEach { lowerBound ->
                        constraints.add(lowerBound.first.type subtype equivalentVar.first.type)
                    }

                    // if (x == a) && (x < u) then (a < u)
                    variable.upperBounds.forEach { upperBound ->
                        constraints.add(equivalentVar.first.type subtype upperBound.first.type)
                    }

                    // if (x == a) && (x == b) then (a == b)
                    variable.equivalentTo.forEach { otherVar ->
                        if (equivalentVar.first != otherVar.first) {
                            constraints.add(equivalentVar.first.type equal otherVar.first.type)
                        }
                    }
                }

                // if (x < u) && (l < x) then (l > u)
                variable.upperBounds.forEach { upperBound ->
                    variable.lowerBounds.forEach {lowerBound ->
                        constraints.add(lowerBound.first.type subtype upperBound.first.type)
                    }
                }

//            // Run each incorporation rule
//            for (it in incorporationRules) {
//                it.apply {
//                    incorporationContext.incorporate(variable, boundSystem.graph.getAllEdges().map { it.data }.toSet())
//                }
//            }


            }
        }

    }

    private fun resolve(constraints: ConstraintSystem, boundSystem: BoundSystemGraph) {
        // The goal of this method is to generate equality constraints for unresolved variables.
        while (!constraints.anyUnresolved() && boundSystem.getUnresolvedVariables().any()) {
            val toSolve = boundSystem.getUnresolvedVariables().sortedWith {
                a, b -> a.getDependencies().count() - b.getDependencies().count()
            }.first()
            SimpleConstraintCreator.apply {
                if (toSolve.pushDown) {
                    val types: List<Type> = toSolve.upperBounds.filter { it.first is TypeNode }.map { it.first.type }
                    if (types.size > 1) {
                        constraints.add(toSolve.variable equal InferredGreatestLowerBound(types))
                    } else if (types.size == 1) {
                        constraints.add(toSolve.variable equal types.first())
                    } else {
                        println("Failed to solve: ${toSolve.variable} given $types")
                        constraints.add(toSolve.variable equal InferredBottomType)
                    }
                } else {
                    val types: List<Type> = toSolve.lowerBounds.filter { it.first is TypeNode }.map { it.first.type }
                    if (types.size > 1) {
                        constraints.add(toSolve.variable equal InferredLeastUpperBound(types))
                    } else if (types.size == 1) {
                        constraints.add(toSolve.variable equal types.first())
                    } else {
                        println("Failed to solve: ${toSolve.variable} given $types")
                        constraints.add(toSolve.variable equal InferredTopType)
                    }
                }
            }
        }


    }
}


       