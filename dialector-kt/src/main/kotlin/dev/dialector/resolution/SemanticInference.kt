package dev.dialector.resolution

/*
Semantic inference is an inference algorithm that can resolve various
interdependent aspects of a program - types and name binding.

Name binding and types are linked in many languages. In code like the following:

fun <T> foo(input: DataStructure<T>): T

fun <T> foo(input: T): T

val data : DataStructure<String>
val example = foo(data).bar.baz

TODO: (Is this necessarily true?)
 */

/**
 * A definition of a Semantic System - a system that performs some well-defined analysis on a program.
 *
 * This type should only be used to create objects.
 *
 * @param T the concrete system type this definition represents.
 */
abstract class SemanticSystemDefinition<T : SemanticSystem>

interface SemanticSystem {

}

/**
 * An edge in the metagraph that signifies a dependency
 */
interface Dependency<T> {
    fun isResolved(): Boolean
    fun getResult(): T
}



interface SemanticAnalysisMetaGraph {
    val systems: Map<SemanticSystemDefinition<*>, SemanticSystem>

}