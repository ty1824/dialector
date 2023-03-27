package dev.dialector.inkt.next

internal data class QueryKey<K, V>(val queryDef: QueryDefinition<K, V>, val key: K)

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

internal class QueryDatabaseContext(val db: QueryDatabaseImpl) : DatabaseContext {
    override fun <K, V> QueryDefinition<K, V>.set(key: K, value: V) = db.set(this, key, value)

    override fun <K, V> QueryDefinition<K, V>.remove(key: K) = db.remove(this, key)

    override fun <K, V> QueryDefinition<K, V>.invoke(key: K): V = db.query(this@QueryDatabaseContext, this, key)
}

public class QueryDatabaseImpl : QueryDatabase {
    private val storage: MutableMap<QueryDefinition<*, *>, MutableMap<*, out Value<*>>> = mutableMapOf()

    private var currentRevision = 0

    private val currentlyActiveQuery: MutableList<QueryFrame<*>> = mutableListOf()

    public override fun <T> run(body: DatabaseContext.() -> T): T {
        return QueryDatabaseContext(this).body()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K, V> getQueryStorage(query: QueryDefinition<K, V>): MutableMap<K, Value<V>> =
        storage.getOrPut(query) { mutableMapOf<K, Value<V>>() } as MutableMap<K, Value<V>>

    private fun <K, V> get(queryKey: QueryKey<K, V>): Value<V>? = getQueryStorage(queryKey.queryDef)[queryKey.key]

    internal fun <K, V> set(inputDef: QueryDefinition<K, V>, key: K, value: V) {
        val queryStorage = getQueryStorage(inputDef)
        when (val currentValue = queryStorage[key]) {
            is InputValue -> {
                currentValue.value = value
                currentValue.changedAt = ++currentRevision
            }

            else -> {
                queryStorage[key] = InputValue(value, ++currentRevision)
            }
        }
    }

    internal fun <K, V> remove(inputDef: QueryDefinition<K, V>, key: K) {
        getQueryStorage(inputDef).remove(key)
    }

    public fun <K, V> query(context: QueryContext, queryDef: QueryDefinition<K, V>, key: K): V {
        val current = QueryKey(queryDef, key)
        val queryStorage = getQueryStorage(queryDef)
        if (currentlyActiveQuery.any { it.queryKey == current }) {
            throw RuntimeException("Cycle detected: $current already in $currentlyActiveQuery")
        }
        recordQuery(current)
        val existingValue = queryStorage[key]
        return if (existingValue is InputValue<V>) {
            trackRevision(existingValue.changedAt)
            existingValue.value
        } else if (existingValue is DerivedValue<V> && verify(existingValue, existingValue.verifiedAt)) {
            existingValue.value
        } else {
            currentlyActiveQuery += QueryFrame(current)
            try {
                val result = queryDef.execute(context, key)
                val frame = currentlyActiveQuery.last()

                queryStorage[key] = DerivedValue(result, frame.dependencies.toMutableList(), currentRevision, frame.maxRevision)
                result
            } finally {
                val frame = currentlyActiveQuery.removeLast()
                recordDependencies(frame.dependencies)
                trackRevision(frame.maxRevision)
            }
        }
    }

    private fun verify(value: Value<*>, asOfRevision: Int): Boolean {
        return when (value) {
            is InputValue<*> -> value.changedAt <= asOfRevision
            is DerivedValue<*> ->
                if (value.changedAt > asOfRevision) {
                    // This value has been updated more recently than the expected revision
                    false
                } else if (value.verifiedAt == currentRevision) {
                    true
                } else {
                    // Recurse through dependencies and verify them
                    value.dependencies.all { dep ->
                        get(dep)?.let { verify(it, value.verifiedAt) } ?: true
                    }.also {
                        if (it) {
                            value.verifiedAt = currentRevision
                        }
                    }
                    // TODO: If dependencies are invalid, recompute the query and check if the result is equivalent.
                    // If so, we can still "verify", preventing dependents from recalculating.
                }
        }
    }

    private fun recordQuery(key: QueryKey<*, *>) {
        if (currentlyActiveQuery.isNotEmpty()) {
            val deps = currentlyActiveQuery.last().dependencies
            if (!deps.contains(key)) deps += key
        }
    }

    private fun trackRevision(revision: Int) {
        if (currentlyActiveQuery.isNotEmpty()) {
            val currentFrame = currentlyActiveQuery.last()
            if (currentFrame.maxRevision < revision) {
                currentFrame.maxRevision = revision
            }
        }
    }

    private fun recordDependencies(dependencies: List<QueryKey<*, *>>) {
        if (currentlyActiveQuery.isNotEmpty()) {
            val deps = currentlyActiveQuery.last().dependencies
            dependencies.forEach { key ->
                if (!deps.contains(key)) deps += key
            }
        }
    }

    public fun print() {
        println("=========================")
        println("Current revision = $currentRevision")
        storage.forEach { (query, store) ->
            println("Query store: $query")
            store.forEach { (key, value) ->
                println("  $key to $value")
            }
        }
        println("=========================")
    }
}
