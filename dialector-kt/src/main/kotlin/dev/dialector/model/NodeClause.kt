package dev.dialector.model

import dev.dialector.util.ClassifierClause
import dev.dialector.util.InstanceClause
import dev.dialector.util.TypesafeClause
import kotlin.reflect.KClass


/**
 * A clause that matches against [Node]s.
 */
interface NodeClause<T : Node> : TypesafeClause<T> {
    operator fun invoke(candidate: Node): Boolean =
        clauseClass.isInstance(candidate) && constraint(candidate as T)
}

/**
 * Creates a Clause that matches against a specific [Node].
 */
fun <T : Node> given(forNode: T): NodeClause<T> =
    object : InstanceClause<T>(forNode), NodeClause<T> {
        override fun invoke(candidate: Node): Boolean = forNode == candidate
    }

/**
 * Creates a Clause that matches against a specific Node class.
 */
inline fun <reified T : Node> given(): NodeClause<T> =
    object : ClassifierClause<T>(T::class), NodeClause<T> {
        override fun invoke(candidate: Node): Boolean = clauseClass.isInstance(candidate)
    }

/**
 * Creates a Clause that matches nodes against a given predicate.
 */
inline fun <reified T : Node> given(crossinline predicate: (T) -> Boolean) = object : NodeClause<T> {
    override val clauseClass = T::class
    override fun constraint(candidate: T): Boolean = predicate(candidate)
}