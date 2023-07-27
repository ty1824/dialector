package dev.dialector.inkt.next

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * A query that can be incrementalized by a QueryDatabase.
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

public typealias QueryFunction<K, V> = QueryContext.(K) -> V
public typealias NoArgQueryFunction<V> = QueryContext.() -> V

/**
 * Defines a query with the given implementation.
 *
 * If no implementation is provided, the query will fail with a NotImplementedException upon execution if there is no
 * explicit value set in the database. This is true regardless of the nullability of the return type.
 *
 * If you want a property to always return null if an explicit value has not been set, define the implementation to
 * return null.
 */
public fun <K : Any, V> defineQuery(
    name: String? = null,
    implementation: QueryFunction<K, V>? = null,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, QueryDefinition<K, V>>> =
    QueryDefinitionInitializer(name, implementation)

/**
 * Defines a no-argument query (Unit-type argument) with the given implementation.
 *
 * If no implementation is provided, the query will fail with a NotImplementedException upon execution if there is no
 * explicit value set in the database. This is true regardless of the nullability of the return type.
 *
 * If you want a property to always return null if an explicit value has not been set, define the implementation to
 * return null.
 */
public fun <V> defineQuery(
    name: String? = null,
    implementation: NoArgQueryFunction<V>? = null,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, QueryDefinition<Unit, V>>> =
    QueryDefinitionInitializer(
        name,
        if (implementation != null) { { implementation() } } else null,
    )
