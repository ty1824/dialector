package dev.dialector.diagnostic

import dev.dialector.semantic.SemanticModel
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeClause
import dev.dialector.syntax.SyntacticModel

public interface DiagnosticEvaluationContext : SyntacticModel, SemanticModel {
    /**
     * Registers a diagnostic for the given node.
     */
    public fun diagnostic(message: String, node: Node)
}

/**
 * A rule that defines [ModelDiagnostics] that should be produced for nodes matching a [NodeClause]
 */
public interface DiagnosticRule<T : Node> {
    public val isValidFor: NodeClause<T>
    public val diagnostics: DiagnosticEvaluationContext.(node: T) -> Unit

    public operator fun invoke(context: DiagnosticEvaluationContext, node: Node) {
        @Suppress("UNCHECKED_CAST")
        if (isValidFor(node)) context.diagnostics(node as T)
    }
}

public infix fun <T : Node> NodeClause<T>.check(check: DiagnosticEvaluationContext.(node: T) -> Unit): DiagnosticRule<T> =
    object : DiagnosticRule<T> {
        override val isValidFor: NodeClause<T> = this@check
        override val diagnostics: DiagnosticEvaluationContext.(node: T) -> Unit = check
    }
