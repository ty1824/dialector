package dev.dialector.inkt

import kotlin.reflect.KClass

public annotation class QueryGroup

public annotation class Query

public annotation class Input

public annotation class Tracked

public annotation class DatabaseDef(vararg val groups: KClass<*>)

public class NoInputDefinedException(message: String) : RuntimeException(message)

public interface DatabaseQuery<K, V> {
    public val name: String
    public fun get(key: K): V
}

/**
 * This function is used to create a typesafe storage map for a query.
 * The receiver is necessary to properly infer types, even though IntelliJ says otherwise.
 */
internal fun <K, V> DatabaseQuery<K, V>.createMap(): MutableMap<K, Value<V>> = mutableMapOf()

public data class InputQuery<K, V>(
    override val name: String,
    private val query: (K) -> V
) : DatabaseQuery<K, V> {
    override fun get(key: K): V = query(key)
}

public data class DerivedQuery<K, V>(
    override val name: String,
    private val query: (K) -> V
) : DatabaseQuery<K, V> {
    override fun get(key: K): V = query(key)
}

public fun <K, V> inputQuery(
    name: String,
    query: (K) -> V = { throw NoInputDefinedException("No input exists for query $name($it)") }
): InputQuery<K, V> = InputQuery(name, query)

public fun <K, V> derivedQuery(name: String, query: (K) -> V): DerivedQuery<K, V> = DerivedQuery(name, query)
