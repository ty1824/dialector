package dev.dialector.semantic.scope

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeClause
import dev.dialector.semantic.SemanticAnalysisContext
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.TypeClause

@DslMarker
annotation class ScopeGraphDsl

@ScopeGraphDsl
interface ScopeTraversalContext {

    val semantics: SemanticAnalysisContext

    /**
     * Creates a new scope
     */
    fun newScope(): ScopeDescriptor

    /**
     * Retrieves a published scope for the given identifier.
     */
//    fun queryScope(identifier: String): ScopeDescriptor

    /**
     * Traverses to the given node with the given scope to continue evaluating scope.
     *
     * WARNING: This should generally be used with direct children of the current node. Use otherwise may cause
     * performance issues or nondeterministic evaluation.
     */
    suspend fun traverse(node: Node, scope: ScopeDescriptor)
}


/**
 * Defines a rule that modifies how the AST is traversed to produce a scope.
 */
interface ScopeTraversalRule<T : Node> {
    val label: String
    val isValidFor: NodeClause<T>
    val traversal: suspend ScopeTraversalContext.(node: T, incomingScope: ScopeDescriptor) -> Unit

    suspend operator fun invoke(context: ScopeTraversalContext, node: Node, scope: ScopeDescriptor) {
        if (isValidFor(node)) context.traversal(node as T, scope)
    }
}


fun <T : Node> NodeClause<T>.produceScope(label: String, traversal: suspend ScopeTraversalContext.(node: T, incomingScope: ScopeDescriptor) -> Unit) =
    object : ScopeTraversalRule<T> {
        override val label: String = label
        override val isValidFor: NodeClause<T> = this@produceScope
        override val traversal: suspend ScopeTraversalContext.(node: T, incomingScope: ScopeDescriptor) -> Unit = traversal

    }

interface TypeScopeContext {
    val semantics: SemanticAnalysisContext
}

interface TypeScopingRule<T : Type> {
    val label: String
    val isValidFor: TypeClause<T>
    val contribution: TypeScopeContext.(type: T, scope: ScopeDescriptor) -> Unit
}

fun <T : Type> TypeClause<T>.produceScope(label: String, contribution: TypeScopeContext.(type: T, incomingScope: ScopeDescriptor) -> Unit) =
    object : TypeScopingRule<T> {
        override val label: String = label
        override val isValidFor: TypeClause<T> = this@produceScope
        override val contribution: TypeScopeContext.(type: T, scope: ScopeDescriptor) -> Unit = contribution

    }