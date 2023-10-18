package dev.dialector.semantic.scope

import dev.dialector.syntax.Node
import java.lang.ref.WeakReference

public interface ScopeNode<C> {
    public val declarations: List<Declaration>
    public val inheritsFrom: List<ScopeNodeReference<C>>
    public fun getAllDeclarations(context: C): Sequence<Declaration> = sequence {
        yieldAll(declarations)
        inheritsFrom.forEach {
            yieldAll(it.resolve(context).getAllDeclarations(context))
        }
    }.distinct()
}

public data class Declaration(val node: Node, val identifier: String)

public interface ScopeNodeReference<C> {
    public val label: String
    public fun resolve(context: C): ScopeNode<C>
}

internal data class DefinedScopeNode<T, C>(
    val definition: ScopeDefinition<T, C>,
    val forArg: T,
    override val declarations: List<Declaration>,
    override val inheritsFrom: List<ScopeNodeReference<C>>
) : ScopeNode<C>

internal data class LocalScopeNode<C>(
    override val declarations: List<Declaration>,
    override val inheritsFrom: List<ScopeNodeReference<C>>
) : ScopeNode<C>

internal class DefinedScopeNodeReference<T, C>(
    override val label: String,
    val definition: ScopeDefinition<T, C>,
    val forArg: (C) -> T
) : ScopeNodeReference<C> {
    override fun resolve(context: C): ScopeNode<C> = definition.createScopeNode(forArg(context), context)
}


