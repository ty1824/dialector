package dev.dialector.inkt

internal data class QueryKey<K, V>(val queryDef: DatabaseQuery<K, V>, val key: K)

internal sealed interface Value<V> {
    var value: V
    var changedAt: Int
}

internal data class InputValue<V>(override var value: V, override var changedAt: Int) : Value<V>

internal data class DerivedValue<V>(
    override var value: V,
    val dependencies: MutableList<QueryKey<*, *>>,
    var verifiedAt: Int,
    override var changedAt: Int
) : Value<V>

internal class QueryFrame<K>(
    val queryKey: QueryKey<K, *>,
    var maxRevision: Int = 0,
    val dependencies: MutableList<QueryKey<*, *>> = mutableListOf()
)
