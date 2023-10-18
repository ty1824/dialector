package dev.dialector.diagnostic

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodePredicate

public interface DiagnosticSeverity {
    public object Error : DiagnosticSeverity
    public object Warning : DiagnosticSeverity
    public object Info : DiagnosticSeverity
    public object Hint : DiagnosticSeverity
}

public interface DiagnosticContext {
    /**
     * Registers a diagnostic for the given node.
     */
    public fun diagnostic(message: String, node: Node, severity: DiagnosticSeverity = DiagnosticSeverity.Error)
}

/**
 * A rule that produces diagnostics for nodes matching a [NodePredicate]
 */
public interface DiagnosticRule<T : Node, C> {
    public val isValidFor: NodePredicate<T, C>
    public val diagnostics: context(DiagnosticContext) C.(node: T) -> Unit

    public operator fun invoke(node: Node, context: C, diagnosticContext: DiagnosticContext) {
        @Suppress("UNCHECKED_CAST")
        if (isValidFor(node, context)) diagnostics(diagnosticContext, context, node as T)
    }
}

/**
 * Produces a [DiagnosticRule] from the given [NodePredicate] and checking function.
 *
 * @param T The node type being checked.
 * @param C The [DiagnosticContext] type
 */
public infix fun <T : Node, C> NodePredicate<T, C>.check(
    check: context(DiagnosticContext) C.(node: T) -> Unit,
): DiagnosticRule<T, C> =
    object : DiagnosticRule<T, C> {
        override val isValidFor: NodePredicate<T, C> = this@check
        override val diagnostics: context(DiagnosticContext) C.(node: T) -> Unit = check
    }
