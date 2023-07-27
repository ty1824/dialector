package dev.dialector.semantic

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference

/**
 * Describes how scopes can be passed between nodes.
 */
public interface PropagationType

/**
 * A propagation type where a parent node passes a scope to a child node.
 */
public object Parent : PropagationType

public interface ScopeVariable : SemanticVariable {
    val name: String
}

/**
 * Declares that this scope will inherit elements from the given scope.
 */
public data class InheritScopeConstraint(
    val scope: ScopeVariable,
    val inheritFrom: ScopeVariable,
    val label: String,
) : SemanticConstraint

/**
 * Declares an element in the context of the scope and namespace.
 */
public data class DeclareElementConstraint(
    val scope: ScopeVariable,
    val namespace: Namespace,
    val element: Node,
    val name: String,
) : SemanticConstraint

/**
 * Declares a namespaced alias for an element in the given scope and namespace.
 */
public data class AliasElementConstraint(
    val scope: ScopeVariable,
    val namespace: Namespace,
    val original: String,
    val aliasNamespace: Namespace,
    val alias: String,
) : SemanticConstraint

/**
 * Declares a named reference in the context of a scope and namespace.
 */
public data class ReferenceIdentifierConstraint(
    /** The [ScopeVariable] the reference should be looked up in */
    val scope: ScopeVariable,
    /** The [Namespace] the search should use */
    val namespace: Namespace,
    /** The [NodeReference] to resolve */
    val reference: NodeReference<out Node>,
) : SemanticConstraint

public object Scopes : ConstraintCreator {
    public fun ScopeVariable.inherit(inheritFrom: ScopeVariable, label: String): InheritScopeConstraint =
        InheritScopeConstraint(this, inheritFrom, label)

    public fun ScopeVariable.declare(namespace: Namespace, element: Node, name: String): DeclareElementConstraint =
        DeclareElementConstraint(this, namespace, element, name)

    public fun ScopeVariable.alias(namespace: Namespace, original: String, aliasNamespace: Namespace, alias: String): AliasElementConstraint =
        AliasElementConstraint(this, namespace, original, aliasNamespace, alias)

    public fun ScopeVariable.reference(namespace: Namespace, reference: NodeReference<out Node>): ReferenceIdentifierConstraint =
        ReferenceIdentifierConstraint(this, namespace, reference)
}
