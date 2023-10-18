package dev.dialector.semantic.scope

import dev.dialector.syntax.Node
import dev.dialector.util.withPropertyName
import dev.dialector.util.wrapperDelegate
import kotlin.properties.ReadOnlyProperty

typealias ScopeBuilder<T, C> = context(C) ScopeBuilderContext<C>.(T) -> Unit

public fun <T, C> defineScope(
    name: String? = null,
    scopeBuilder: ScopeBuilder<T, C>
): ReadOnlyProperty<Any?, ScopeDefinition<T, C>> =
    if (name != null) {
        wrapperDelegate(ScopeDefinitionImpl(name, scopeBuilder))
    } else {
        withPropertyName {
            ScopeDefinitionImpl(it, scopeBuilder)
        }
    }

public interface ScopeDefinition<T, C> {
    public val name: String
    public fun createScopeNode(arg: T, context: C): ScopeNode<C>
}

internal data class ScopeDefinitionImpl<T, C>(
    override val name: String,
    val builder: ScopeBuilder<T, C>
) : ScopeDefinition<T, C> {
    override fun createScopeNode(arg: T, context: C): ScopeNode<C> {
        val declarations: MutableList<Declaration> = mutableListOf()
        val inheritsFrom: MutableList<ScopeNodeReference<C>> = mutableListOf()
        val builderContext = object : ScopeBuilderContext<C> {
            override fun declare(node: Node, identifier: String) {
                TODO("Not yet implemented")
            }

            override fun <T> inherit(label: String, definition: ScopeDefinition<T, C>, deferred: (C) -> T) {
                TODO("Not yet implemented")
            }

            override fun <T> inherit(label: String, definition: ScopeDefinition<T, C>, arg: T) {
                TODO("Not yet implemented")
            }

        }
        builder(context, builderContext, arg)
        return DefinedScopeNode(this, arg, declarations, inheritsFrom)
    }

}

public interface ScopeBuilderContext<C> {
    public companion object {
        /**
         * Declare all pairs of nodes and identifiers as elements in this scope.
         */
        public fun <C> ScopeBuilderContext<C>.declareAll(elements: Iterable<Pair<Node, String>>) {
            elements.forEach { declare(it.first, it.second) }
        }

        /**
         * Inherit from a no-arg [ScopeDefinition]
         */
        public fun <C> ScopeBuilderContext<C>.inherit(label: String, definition: ScopeDefinition<Unit, C>) {
            inherit(label, definition, Unit)
        }
    }

    /**
     * Declare a node as an element of this scope with the given identifier.
     */
    public fun declare(node: Node, identifier: String)

    /**
     * Specify that this scope should inherit from the scope represented by the given definition and argument.
     */
    public fun <T> inherit(label: String, definition: ScopeDefinition<T, C>, arg: T)

    /**
     * Specify that this scope should inherit from the scope represented by the given definition and deferred argument
     * (that will be computed at scope resolution time).
     *
     * This form is useful for handling scopes relative to references.
     */
    public fun <T> inherit(label: String, definition: ScopeDefinition<T, C>, deferred: (C) -> T)
}