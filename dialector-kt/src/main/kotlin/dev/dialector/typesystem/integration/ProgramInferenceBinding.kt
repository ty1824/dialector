package dev.dialector.typesystem.integration

import dev.dialector.model.Node
import dev.dialector.resolution.Query
import dev.dialector.resolution.SemanticAnalysisContext
import dev.dialector.typesystem.inference.new.BaseInferenceContext
import dev.dialector.typesystem.inference.new.InferenceConstraint
import dev.dialector.typesystem.inference.new.InferenceContext
import dev.dialector.typesystem.inference.new.InferenceVariable

/**
 * Extends [InferenceContext] with model-aware functionality
 */
interface ProgramInferenceContext : InferenceContext {

    /**
     * Retrieves the InferenceVariable bound to the given model Node. There may only be one variable bound to a Node.
     */
    fun typeOf(node: Node): InferenceVariable

    fun <N : Node> typeOf(query: Query<*, out N>): InferenceVariable

    val semantics: SemanticAnalysisContext


}

/**
 * Maintains a binding between an inference context and a program's AST
 */
class BaseProgramInferenceContext(
    override val semantics: SemanticAnalysisContext,
    createVariable: () -> InferenceVariable,
    addConstraint: (InferenceConstraint) -> Unit
) : InferenceContext by BaseInferenceContext(createVariable, addConstraint), ProgramInferenceContext {
    val nodeVariables: MutableMap<Node, InferenceVariable> = mutableMapOf()

    override fun typeOf(node: Node): InferenceVariable = nodeVariables.computeIfAbsent(node) { typeVar() }

    override fun <N : Node> typeOf(query: Query<*, out N>): InferenceVariable {
        TODO("Not yet implemented")
    }
}