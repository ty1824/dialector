package dev.dialector.inkt.next

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A delegate provider that extracts the property name to use as the query name.
 */
internal class QueryDefinitionInitializer<K, V> internal constructor(
    private val name: String?,
    private val logic: QueryContext.(K) -> V
) : PropertyDelegateProvider<Any?, QueryDefinitionDelegate<K, V>> {
    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): QueryDefinitionDelegate<K, V> =
        QueryDefinitionDelegate(QueryDefinitionImpl(name ?: property.name, logic))
}

/**
 * A property delegate that allows queries defined as properties to self-reference/recurse.
 */
internal class QueryDefinitionDelegate<K, V>(private val value: QueryDefinition<K, V>) :
    ReadOnlyProperty<Any?, QueryDefinition<K, V>> {
    public override operator fun getValue(thisRef: Any?, property: KProperty<*>): QueryDefinition<K, V> = value
}

internal data class QueryDefinitionImpl<K, V>(
    override val name: String,
    val logic: QueryContext.(K) -> V
) : QueryDefinition<K, V>, ReadOnlyProperty<Any?, QueryDefinition<K, V>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): QueryDefinition<K, V> = this
    override fun execute(context: QueryContext, key: K): V = context.logic(key)
}
