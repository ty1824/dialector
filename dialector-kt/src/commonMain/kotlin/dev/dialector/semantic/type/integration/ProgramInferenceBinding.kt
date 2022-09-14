package dev.dialector.semantic.type.integration

import dev.dialector.syntax.Node
import dev.dialector.semantic.Query
import dev.dialector.semantic.SemanticAnalysisContext
import dev.dialector.semantic.type.inference.new.BaseInferenceContext
import dev.dialector.semantic.type.inference.new.InferenceConstraint
import dev.dialector.semantic.type.inference.new.InferenceContext
import dev.dialector.semantic.type.inference.new.InferenceVariable

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