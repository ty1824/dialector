package dev.dialector.typesystem.inference.new

import dev.dialector.typesystem.Type
import dev.dialector.typesystem.inference.DataGraph

interface BoundCreator {
    infix fun InferenceVariable.exactBound(type: Type)
    infix fun InferenceVariable.upperBound(type: Type)
    infix fun InferenceVariable.lowerBound(type: Type)
}

class SimpleBoundCreator : BoundCreator {
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
    val isValidFor: (RelationalConstraint) -> Boolean
    val reduce: ReductionContext.(constraint: RelationalConstraint) -> Unit
}

interface IncorporationContext {
    fun constraint(routine: ConstraintCreator.() -> RelationalConstraint)
}

interface IncorporationRule {
    val incorporate: IncorporationContext.(variable: InferenceVariable, bounds: Set<Bound>) -> Unit
}



private class BaseInferenceVariable(override val id: String) : InferenceVariable {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "tv.$id"
}

private data class BaseBound(
    override val relation: TypeRelation,
    override val variable: InferenceVariable,
    override val boundingType: Type
) : Bound

sealed class BoundGraphNode {
    abstract val type: Type
}
class TypeNode(override val type: Type) : BoundGraphNode()
class VariableNode(val variable: Type) : BoundGraphNode() {
    /**
     * Determines whether the solution for this variable should be the greatest lower bound rather than the least upper bound.
     */
    var pushDown: Boolean = false
    var resolvedType: Type? = null

    override val type: Type
        get() = resolvedType ?: variable

    fun isResolved(): Boolean = resolvedType != null
}

class BoundSystemGraph {
    val graph: DataGraph<BoundGraphNode, Bound> = DataGraph()
    val typeNodes: MutableMap<Type, TypeNode> = mutableMapOf()
    val variableNodes: MutableMap<InferenceVariable, VariableNode> = mutableMapOf()

    fun addVariable(variable: InferenceVariable) {
        graph.addNode(nodeFor(variable)).data
    }

    fun addBound(bound: Bound) {
        graph.addEdge(bound, nodeFor(bound.variable), nodeFor(bound.boundingType), true)
    }

    fun setPushDown(variable: InferenceVariable, pushDown: Boolean) {
        nodeFor(variable).pushDown = pushDown
    }

    fun getAllVariables(): Sequence<InferenceVariable> = variableNodes.keys.asSequence()

    fun isResolved(variable: InferenceVariable): Boolean = nodeFor(variable).isResolved()

    fun getUnresolvedVariables(): Sequence<InferenceVariable> =
        variableNodes.asSequence()
            .filter { (_, value) -> value.isResolved() }
            .map { (key, _) -> key }

    fun getBounds(variable: InferenceVariable): Sequence<Bound> = graph.getEdges(nodeFor(variable)).map { it.data }

    fun getDependencies(variable: InferenceVariable): Sequence<InferenceVariable> {
        val node = nodeFor(variable)
        if (node.pushDown) {
            getBounds(variable).filter {
                it.relation == TypeRelation.SUBTYPE || it.relation == TypeRelation.EQUIVALENT
            }
        } else {
            getBounds(variable).filter {
                it.relation == TypeRelation.SUPERTYPE || it.relation == TypeRelation.EQUIVALENT
            }
        }.map { it. }

    }

    private fun nodeFor(type: Type): BoundGraphNode =
        if (type is InferenceVariable) {
            variableNodes.computeIfAbsent(type) { VariableNode(type) }
        } else {
            typeNodes.computeIfAbsent(type) { TypeNode(type) }
        }

    private fun nodeFor(variable: InferenceVariable): VariableNode =
        variableNodes.computeIfAbsent(variable) { VariableNode(variable) }
}

class BaseInferenceContext(
    val createVariable: () -> InferenceVariable,
    val addConstraint: (RelationalConstraint) -> Unit
) : InferenceContext, ConstraintCreator by SimpleConstraintCreator() {
    override fun typeVar(): InferenceVariable = createVariable()

    override fun constraint(routine: ConstraintCreator.() -> RelationalConstraint) {
        addConstraint(routine())
    }
}

class BaseReductionContext(
    val constraint: RelationalConstraint,
    val addConstraint: (RelationalConstraint, RelationalConstraint) -> Unit,
    val addBound: (Bound, RelationalConstraint) -> Unit
) : ReductionContext, IncorporationContext, ConstraintCreator by SimpleConstraintCreator(), BoundCreator by SimpleBoundCreator(){
    override fun constraint(routine: ConstraintCreator.() -> RelationalConstraint) {
        addConstraint(routine(), constraint)
    }

    override fun bound(routine: BoundCreator.() -> Bound) {
        addBound(routine(), constraint)
    }
}

class BaseIncorporationContext(
    val addConstraint: (RelationalConstraint) -> Unit,
) : IncorporationContext, ConstraintCreator by SimpleConstraintCreator() {
    override fun constraint(routine: ConstraintCreator.() -> RelationalConstraint) {
        addConstraint(routine())
    }
}

class BaseInferenceSystem : InferenceSystem {
    private val variables: MutableSet<InferenceVariable> = mutableSetOf()
    private val inferenceConstraint: MutableSet<InferenceConstraint> = mutableSetOf()
    private var variableIdCounter = 0

    override fun getInferenceVariables(): Set<InferenceVariable> {
        return variables.toSet()
    }

    override fun getInferenceConstraints(): Set<InferenceConstraint> = inferenceConstraint.toSet()

    fun createVariable(): InferenceVariable = BaseInferenceVariable("${variableIdCounter++}")

    fun registerConstraint(constraint: InferenceConstraint) {
        inferenceConstraint += constraint
    }

    fun solve(reductionRules: List<ReductionRule>, incorporationRules: List<IncorporationRule>) : InferenceResult {
        val constraints = mutableListOf<RelationalConstraint>()
        constraints += constraints

        val bounds = BoundSystemGraph()
        variables.forEach(bounds::addVariable)

        while (constraints.isNotEmpty()) {
            while (constraints.isNotEmpty()) {
                reduce(reductionRules, constraints, bounds)
                incorporate(incorporationRules, constraints, bounds)
            }
            resolve(constraints, bounds)
        }

        return object : InferenceResult {
            val resultTable = bounds.variableNodes.map { it.key to listOf(it.value.type)}.toMap()
            override fun get(variable: InferenceVariable): List<Type>? = resultTable[variable]
        }
    }

    private fun reduce(
        reductionRules: List<ReductionRule>,
        constraints: MutableList<RelationalConstraint>,
        bounds: BoundSystemGraph
    ) {
        while (constraints.isNotEmpty()) {
            val currentConstraint = constraints.removeFirst()
            val reductionContext = BaseReductionContext(
                currentConstraint,
                { constraint, _ ->
                    constraints.add(constraint)
                },
                { bound, _ ->
                    bounds.addBound(bound)
                }
            )
            // Apply each rule if valid for this constraint
            reductionRules.forEach {
                it.apply {
                    if (isValidFor(currentConstraint)) reductionContext.reduce(currentConstraint)
                }
            }
        }
    }

    private fun incorporate(
        incorporationRules: List<IncorporationRule>,
        constraints: MutableList<RelationalConstraint>,
        boundSystem: BoundSystemGraph
    ) {
        // For each bound system
        for (variable in boundSystem.getUnresolvedVariables()) {
            val incorporationContext = BaseIncorporationContext { constraint ->
                constraints.add(constraint)
            }

//            // Run each incorporation rule
//            for (it in incorporationRules) {
//                it.apply {
//                    incorporationContext.incorporate(variable, boundSystem.graph.getAllEdges().map { it.data }.toSet())
//                }
//            }


        }
    }

    private fun resolve(constraints: MutableList<RelationalConstraint>, boundSystem: BoundSystemGraph) {
        val variables = boundSystem.getUnresolvedVariables()
            .sortedBy { variable ->
                // Sort by the number of variables this is dependent upon
                boundSystem.getBounds(variable)
                    .flatMap {
                        val boundingType =
                            if (it.variable == variable)
                                it.boundingType
                            else
                                it.variable
                        sequence {
                            yield(boundingType)
                            yieldAll(boundingType.getComponents())
                        }
                    }.filter {
                        it is InferenceVariable && !boundSystem.isResolved(it)
                    }.distinct().count()
            }

        val variable = variables.first()
    }
}


       