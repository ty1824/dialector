package dev.dialector.inkt.next

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * A query that Inkt can track and incrementalize.
 */
public interface QueryDefinition<K : Any, V> {
    public val name: String

    /**
     * Runs the logic for this query directly. Will not perform any caching.
     */
    public fun execute(context: QueryContext, key: K): V
}

/**
 * A context that provides the ability to run queries.
 */
public interface QueryContext {
    /**
     * Runs a query for the given input.
     */
    public fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V

    /**
     * Runs a no-argument query for the given input.
     */
    public fun <V> query(definition: QueryDefinition<Unit, V>): V = query(definition, Unit)
}

/**
 * Defines a no-argument query with the given implementation.
 */
public fun <V : Any> defineQuery(
    name: String? = null,
    implementation: QueryContext.() -> V = { throw NotImplementedError("Query not implemented") }
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, QueryDefinition<Unit, V>>> =
    QueryDefinitionInitializer(name) { implementation() }

/**
 * Defines a query with the given implementation.
 */
public fun <K : Any, V> defineQuery(
    name: String? = null,
    implementation: QueryContext.(K) -> V = { throw NotImplementedError("Query not implemented") }
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, QueryDefinition<K, V>>> =
    QueryDefinitionInitializer(name, implementation)
