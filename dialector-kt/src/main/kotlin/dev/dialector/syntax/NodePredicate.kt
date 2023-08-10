package dev.dialector.syntax

import dev.dialector.util.ClassifierPredicate
import dev.dialector.util.InstancePredicate
import dev.dialector.util.LogicalPredicate
import dev.dialector.util.TypesafePredicate
import kotlin.reflect.KClass

/**
 * A clause that matches against [Node]s.
 */
public interface NodePredicate<T : Node, in C> : TypesafePredicate<T, C>

public class NodeInstancePredicate<T : Node>(forNode: T) : NodePredicate<T, Any>, InstancePredicate<T>(forNode)

public class NodeClassifierPredicate<T : Node>(
    forClass: KClass<T>,
) : NodePredicate<T, Any>, ClassifierPredicate<T>(forClass)

public class NodeLogicalPredicate<T : Node, in C>(
    forClass: KClass<T>,
    predicate: C.(T) -> Boolean,
) : NodePredicate<T, C>, LogicalPredicate<T, C>(forClass, predicate)

/**
 * Creates a [NodePredicate] that matches against a specific [Node] instance.
 */
public fun <T : Node> given(forNode: T): NodePredicate<T, Any> =
    NodeInstancePredicate(forNode)

/**
 * Creates a [NodePredicate] that matches against a subclass of [Node].
 */
public inline fun <reified T : Node> given(): NodePredicate<T, Any> =
    NodeClassifierPredicate(T::class)

/**
 * Creates a [NodePredicate] that matches nodes against a given contextual predicate.
 */
public inline fun <reified T : Node, C> given(noinline predicate: C.(T) -> Boolean): NodePredicate<T, C> =
    NodeLogicalPredicate(T::class, predicate)
