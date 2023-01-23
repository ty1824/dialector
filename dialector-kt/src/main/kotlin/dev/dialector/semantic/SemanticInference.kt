package dev.dialector.semantic

import dev.dialector.syntax.Node

/*
Semantic inference is an inference algorithm that can resolve various
interdependent aspects of a program - types and name binding.

Name binding and types are linked in many languages. In code like the following:

fun <T> foo(input: DataStructure<T>): T

fun <T> foo(input: T): T

val data : DataStructure<String>
val example = foo(data).bar.baz

TODO: (Is this necessarily true?)

There two levels of interaction between systems.

The analysis systems will depend on each other to complete analysis. Dependency targets are represented by queries,
which are strongly-defined computations that may not be immediately resolved. Systems must be designed in such a way
that they can handle waiting for a queried dependency to be resolved before resolving themselves.

Beyond this layered single resolution, an incremental system will need to be able to respond to
changes - additions, modifications, or deletions - to the program being analyzed. Changes may affect analysis results
for a particular part of the program, which in turn may affect the result of queries that were used in subsequent
analysis.

Thus semantic analysis is not simply a computation but a complex network with the capability to invalidate
a sub-graph based on a change to the AST.


A change to the AST results in a series of invalidations - stepping through the query/analysis graph
 */

/**
 * A definition of a Semantic System - a system that performs some well-defined analysis on a program.
 *
 * This type should only be used to create objects.
 *
 * @param T the concrete system type this definition represents.
 */
abstract class SemanticSystemDefinition<S : SemanticSystem> {
    companion object {

    }
    open inner class SemanticDataDef<A, D>(query: (system: S, argument: A) -> Query<A, D>) {
        val forSystem: SemanticSystemDefinition<S> = this@SemanticSystemDefinition
    }
}

/**
 * A definition of Semantic Data that can be provided by this Semantic System.
 *
 * @param S The [SemanticSystem] that can provide this data.
 * @param A The argument data that must be provided to a query for this data.
 * @param D The data type returned by a query on this definition.
 */
abstract class SemanticDataDefinition<S : SemanticSystem, A, D>(val forSystem: SemanticSystemDefinition<S>) {
    abstract fun query(system: S, argument: A): Query<A, D>
}

interface ProgramChange {
    val nodesAdded: Sequence<Node>
    val nodesRemoved: Sequence<Node>
}

interface SemanticSystem {
    val semantics: SemanticAnalysisContext
}

interface Query<A, D> {
    val type: SemanticDataDefinition<*, A, D>
    val argument: A
    fun isResolved(): Boolean
    fun getResult(): D
}


interface SemanticAnalysisMetaGraph {
    val systems: Map<SemanticSystemDefinition<*>, SemanticSystem>

    fun <S: SemanticSystem, A, D> query(data: SemanticDataDefinition<S, A, D>, argument: A): Query<A, D> {
        return data.query((systems[data.forSystem] ?: error("System implementation not found: ${data.forSystem}")) as S, argument)
    }
}

interface SolverContext

sealed class IterationResult

data class Waiting(val on: Iterable<Query<*, *>>) : IterationResult()

object Completed : IterationResult()

interface RuleProvider {

}

interface IterativeSolver {
    fun initialize(program: Program)

    fun iterate(context: SolverContext): IterationResult

    fun conclude(context: SolverContext)
}
