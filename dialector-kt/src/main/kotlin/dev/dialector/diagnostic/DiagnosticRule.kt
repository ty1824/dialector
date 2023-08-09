package dev.dialector.diagnostic

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodePredicate

public interface DiagnosticContext {
    /**
     * Registers a diagnostic for the given node.
     */
    public fun diagnostic(message: String, node: Node)
}

/**
 * A rule that defines [ModelDiagnostics] that should be produced for nodes matching a [NodePredicate]
 */
public interface DiagnosticRule<T : Node, C : DiagnosticContext> {
    public val isValidFor: NodePredicate<T, in C>
    public val diagnostics: C.(node: T) -> Unit

    public operator fun invoke(node: Node, context: C) {
        @Suppress("UNCHECKED_CAST")
        if (isValidFor(node, context)) context.diagnostics(node as T)
    }
}

/**
 * Produces a [DiagnosticRule] from the given [NodePredicate] and checking function.
 *
 * @param T The node type being checked.
 * @param C The [DiagnosticContext] type
 */
public infix fun <T : Node, C : DiagnosticContext> NodePredicate<T, in C>.check(
    check: C.(node: T) -> Unit,
): DiagnosticRule<T, C> =
    object : DiagnosticRule<T, C> {
        override val isValidFor: NodePredicate<T, in C> = this@check
        override val diagnostics: C.(node: T) -> Unit = check
    }
