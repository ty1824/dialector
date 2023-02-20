package dev.dialector.semantic.type.inference.new

import dev.dialector.semantic.Completed
import dev.dialector.semantic.IterationResult
import dev.dialector.semantic.IterativeSolver
import dev.dialector.semantic.Program
import dev.dialector.semantic.Query
import dev.dialector.semantic.SolverContext
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.TypeSystem
import dev.dialector.semantic.type.integration.BaseProgramInferenceContext
import dev.dialector.semantic.type.integration.ProgramInferenceContext
import dev.dialector.syntax.getAllDescendants

class IterativeTypeConstraintSolver(
    private val typeSystem: TypeSystem,
    private val onComplete: (InferenceResult) -> Unit
) : IterativeSolver {

    private lateinit var inferenceConstraintSystem: BaseInferenceConstraintSystem
    private lateinit var context: ProgramInferenceContext

    private lateinit var initialConstraints: List<InferenceConstraint>
    private lateinit var solverConstraintSystem: ConstraintSystem
    private lateinit var bounds: BoundSystemGraph

    override fun initialize(program: Program) {
        inferenceConstraintSystem = BaseInferenceConstraintSystem()
        context = BaseProgramInferenceContext(typeSystem.semantics, inferenceConstraintSystem::createVariable, inferenceConstraintSystem::registerConstraint)

        context.apply {
            for (root in program.roots) {
                for (currentNode in root.getAllDescendants(true)) {
                    for (rule in typeSystem.inferenceRules) {
                        rule(this, currentNode)
                    }
                }
            }
        }

        solverConstraintSystem = ConstraintSystem(inferenceConstraintSystem.getInferenceConstraints())
        bounds = BoundSystemGraph()
        inferenceConstraintSystem.getInferenceVariables().forEach(bounds::addVariable)
    }

    override fun iterate(context: SolverContext): IterationResult {
        var iteration = 0
        while (solverConstraintSystem.anyUnresolved()) {
            iteration++
            var subIteration = 0
            while (solverConstraintSystem.anyUnresolved()) {
                subIteration++
                println("ITERATION: $iteration.$subIteration - Constraints")
                println(solverConstraintSystem)
                println(bounds)
                reduce(typeSystem.reductionRules, solverConstraintSystem, bounds)
                println("ITERATION: $iteration.$subIteration - Incorporation")
                println(solverConstraintSystem)
                println(bounds)
                incorporate(solverConstraintSystem, bounds)
            }
            println("ITERATION $iteration - Solving")
            println(solverConstraintSystem)
            println(bounds)
            resolve(solverConstraintSystem, bounds)
        }

        return Completed
    }

    override fun conclude(context: SolverContext) {
        println("Solving complete")
        onComplete(object : InferenceResult {
            val resultTable = bounds.variableNodes.map { (key, value) ->
                key to value.equivalentTo.filter { it.first is TypeNode }.map { it.first.type }.toList()
            }.toMap()
            override fun get(variable: InferenceVariable): List<Type>? = resultTable[variable]
        })
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
                bounds.setPushDown(currentConstraint.variable, currentConstraint.kind == VariableConstraintKind.PUSH_DOWN)
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
                variable.lowerBounds.forEach { lowerBound ->
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

private fun resolve(constraints: ConstraintSystem, boundSystem: BoundSystemGraph): List<Query<*, *>> {
    // The goal of this method is to generate equality constraints for unresolved variables.
    while (!constraints.anyUnresolved() && boundSystem.getUnresolvedVariables().any()) {
        val toSolve = boundSystem.getUnresolvedVariables().sortedWith {
                a, b ->
            a.getDependencies().count() - b.getDependencies().count()
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

    return listOf()
}
