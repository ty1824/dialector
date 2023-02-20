package dev.dialector.syntax

import dev.dialector.util.ClassifierClause
import dev.dialector.util.InstanceClause
import dev.dialector.util.TypesafeClause

/**
 * A clause that matches against [Node]s.
 */
public interface NodeClause<T : Node> : TypesafeClause<T> {
    public operator fun invoke(candidate: Node): Boolean =
        clauseClass.isInstance(candidate) && constraint(candidate as T)
}

/**
 * Creates a [NodeClause] that matches against a specific [Node] instance.
 */
public fun <T : Node> given(forNode: T): NodeClause<T> =
    object : InstanceClause<T>(forNode), NodeClause<T> {
        override fun invoke(candidate: Node): Boolean = forNode == candidate
    }

/**
 * Creates a [NodeClause] that matches against a subclass of [Node].
 */
public inline fun <reified T : Node> given(): NodeClause<T> =
    object : ClassifierClause<T>(T::class), NodeClause<T> {
        override fun invoke(candidate: Node): Boolean = clauseClass.isInstance(candidate)
    }

/**
 * Creates a [NodeClause] that matches nodes against a given predicate.
 */
public inline fun <reified T : Node> given(crossinline predicate: (T) -> Boolean): NodeClause<T> =
    object : NodeClause<T> {
        override val clauseClass = T::class
        override fun constraint(candidate: T): Boolean = predicate(candidate)
    }
