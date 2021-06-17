package dev.dialector.resolution;

import dev.dialector.model.Node

data class SemanticSystemEntry<S : SemanticSystem>(val definition: SemanticSystemDefinition<S>, val system: S)

interface Program {
    val roots: Sequence<Node>
}

class SemanticSolverContext : SolverContext

class SemanticEvaluator(val solvers: List<IterativeSolver>) {

    fun evaluate(program: Program) {
        // Initialize each solver
        solvers.forEach { it.initialize(program) }

        // Iterate on solvers
        var completed = false
        var lastWaitingOn: Set<Query<*, *>> = setOf()
        var waitingOn: Set<Query<*, *>> = setOf()
        do {
            lastWaitingOn = waitingOn
            waitingOn = solvers.flatMap {
                val result = it.iterate(SemanticSolverContext())
                when (result) {
                    is Waiting -> result.on
                    else -> listOf()
                }
            }.toSet()
        } while (waitingOn.isNotEmpty() && waitingOn != lastWaitingOn)

        if (waitingOn.isNotEmpty()) {
            throw RuntimeException("Failed to resolve, waiting on: $waitingOn")
        }

        // Allow solvers to complete
        solvers.forEach { it.conclude(SemanticSolverContext()) }
    }

}
