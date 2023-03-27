package dev.dialector.inkt

public class QueryDatabase(public val definitions: List<DatabaseQuery<*, *>>) {
    private val storage: Map<DatabaseQuery<*, *>, MutableMap<*, *>> = definitions.associateWith { it.createMap() }

    private var currentRevision = 0

    private val currentlyActiveQuery: MutableList<QueryFrame<*>> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    private fun <K, V> getQueryStorage(query: DatabaseQuery<K, V>): MutableMap<K, Value<V>> =
        storage[query] as MutableMap<K, Value<V>>

    private fun <K, V> get(queryKey: QueryKey<K, V>): Value<V>? = getQueryStorage(queryKey.queryDef)[queryKey.key]

    public fun <K, V> setInput(inputDef: InputQuery<K, V>, key: K, value: V) {
        val inputStorage = getQueryStorage(inputDef)
        val inputValue = inputStorage[key]
        if (inputValue == null) {
            inputStorage[key] = InputValue(value, ++currentRevision)
        } else {
            inputValue.value = value
            inputValue.changedAt = ++currentRevision
        }
    }

    public fun <K, V> inputQuery(queryDef: DatabaseQuery<K, V>, key: K): V {
        val current = QueryKey(queryDef, key)
        recordQuery(current)
        return get(current)?.let {
            trackRevision(it.changedAt)
            it.value
        } ?: throw RuntimeException("No value when running query $queryDef for input $key")
    }

    public fun <K, V> derivedQuery(queryDef: DatabaseQuery<K, V>, key: K): V {
        val current = QueryKey(queryDef, key)
        val derivedStorage = getQueryStorage(queryDef)
        if (currentlyActiveQuery.any { it.queryKey == current }) {
            throw RuntimeException("Cycle detected: $current already in $currentlyActiveQuery")
        }
        recordQuery(current)
        val existingValue = derivedStorage[key]
        return if (existingValue is DerivedValue<V> && verify(existingValue, existingValue.verifiedAt)) {
            existingValue.value
        } else {
            currentlyActiveQuery += QueryFrame(current)
            try {
                val result = queryDef.get(key)
                val frame = currentlyActiveQuery.last()

                derivedStorage[key] = DerivedValue(result, frame.dependencies.toMutableList(), currentRevision, frame.maxRevision)
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
                    //   If so, we can still "verify", preventing dependents from recalculating.
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
