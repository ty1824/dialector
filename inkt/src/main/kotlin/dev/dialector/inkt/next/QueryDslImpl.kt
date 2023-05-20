package dev.dialector.inkt.next

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal fun <K : Any> notImplementedMessage(name: String, key: K) =
    "Query '$name' not implemented or value not set for key '$key'"

/**
 * A delegate provider that extracts the property name to use as the query name.
 */
internal class QueryDefinitionInitializer<K : Any, V> internal constructor(
    private val name: String?,
    private val logic: QueryFunction<K, V>?
) : PropertyDelegateProvider<Any?, QueryDefinitionDelegate<K, V>> {
    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): QueryDefinitionDelegate<K, V> {
        val actualName = name ?: property.name
        return QueryDefinitionDelegate(
            QueryDefinitionImpl(
                actualName,
                logic ?: { throw NotImplementedError(notImplementedMessage(actualName, it)) }
            )
        )
    }
}

/**
 * A property delegate that allows queries defined as properties to self-reference/recurse.
 */
internal class QueryDefinitionDelegate<K : Any, V>(private val value: QueryDefinition<K, V>) :
    ReadOnlyProperty<Any?, QueryDefinition<K, V>> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): QueryDefinition<K, V> = value
}

internal data class QueryDefinitionImpl<K : Any, V>(
    override val name: String,
    val logic: QueryFunction<K, V>
) : QueryDefinition<K, V> {
    override fun execute(context: QueryContext, key: K): V = context.logic(key)
}
