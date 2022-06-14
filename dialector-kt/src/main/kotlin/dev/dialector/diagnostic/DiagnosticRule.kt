package dev.dialector.diagnostic

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeClause
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.TypeLattice

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
    val diagnostics: DiagnosticEvaluationContext.(node: T) -> Unit


    operator fun invoke(context: DiagnosticEvaluationContext, node: Node) {
        @Suppress("UNCHECKED_CAST")
        if (isValidFor(node)) context.diagnostics(node as T)
    }
}

infix fun <T : Node> NodeClause<T>.check(check: DiagnosticEvaluationContext.(node: T) -> Unit): DiagnosticRule<T> =
    object : DiagnosticRule<T> {
        override val isValidFor: NodeClause<T> = this@check
        override val diagnostics: DiagnosticEvaluationContext.(node: T) -> Unit = check
    }