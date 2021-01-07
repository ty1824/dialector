package dev.dialector.glottony.diagnostics

import dev.dialector.model.Node
import dev.dialector.model.NodeClause
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.integration.ProgramInferenceContext
import dev.dialector.typesystem.lattice.TypeLattice
import org.jetbrains.annotations.Contract

interface DiagnosticEvaluationContext {

    val typeLattice: TypeLattice

    /**
     * Retrieves the resolved type of the given node.
     */
    fun getTypeOf(node: Node): Type?

    /**
     * Registers a diagnostic for the given node.
     */
    fun diagnostic(message: String, node: Node)
}

/**
 * A rule that defines [ModelDiagnostics] that should be produced for nodes matching a [NodeClause]
 */
interface DiagnosticRule<T : Node> {
    val isValidFor: NodeClause<T>
    val infer: DiagnosticEvaluationContext.(node: T) -> Unit


    operator fun invoke(context: DiagnosticEvaluationContext, node: Node): Unit {
        if (isValidFor(node)) context.infer(node as T)
    }
}

infix fun <T : Node> NodeClause<T>.check(check: DiagnosticEvaluationContext.(node: T) -> Unit): DiagnosticRule<T> =
    object : DiagnosticRule<T> {
        override val isValidFor: NodeClause<T> = this@check
        override val infer: DiagnosticEvaluationContext.(node: T) -> Unit = check
    }