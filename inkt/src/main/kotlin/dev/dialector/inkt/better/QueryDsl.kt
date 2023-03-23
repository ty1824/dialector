package dev.dialector.inkt.better

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

public interface QueryContext {
    public operator fun <K, V> QueryDefinition<K, V>.invoke(key: K): V
}

/**
 * Defines a query with the given implementation.
 */
public fun <K, V> defineQuery(
    name: String? = null,
    implementation: QueryContext.(K) -> V = { throw NotImplementedError("Query not implemented") }
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, QueryDefinition<K, V>>> =
    QueryDefinitionInitializer(name, implementation)

/**
 * A query that Inkt can track and incrementalize.
 */
public interface QueryDefinition<K, V> {
    public val name: String
    public fun execute(context: QueryContext, key: K): V
}
