package dev.dialector.inkt.next

internal data class QueryKey<K : Any, V>(val queryDef: QueryDefinition<K, V>, val key: K)

internal sealed interface Value<V> {
    var value: V
    var changedAt: Int
}

internal data class InputValue<V>(override var value: V, override var changedAt: Int) : Value<V>

internal data class DerivedValue<V>(
    override var value: V,
    var dependencies: List<QueryKey<*, *>>,
    var verifiedAt: Int,
    override var changedAt: Int
) : Value<V>

internal class QueryFrame<K : Any>(
    val queryKey: QueryKey<K, *>,
    var maxRevision: Int = 0,
    val dependencies: MutableList<QueryKey<*, *>> = mutableListOf()
)

internal interface QueryExecutionContext : QueryContext {
    fun pushFrame(key: QueryKey<*, *>)
    fun popFrame(): QueryFrame<*>

    /**
     * Adds a dependency and adds its changedAt revision to the current frame.
     */
    fun addDependency(key: QueryKey<*, *>, revision: Int)
}

internal inline fun <T> QueryExecutionContext.withFrame(key: QueryKey<*, *>, block: () -> T): Pair<T, QueryFrame<*>> {
    pushFrame(key)
    return block() to popFrame()
}

public class QueryDatabaseImpl : QueryDatabase {
    private val storage: MutableMap<QueryDefinition<*, *>, MutableMap<*, out Value<*>>> = mutableMapOf()
    private var currentRevision = 0

    private val lock = Object()

    public override fun <T> readTransaction(body: QueryContext.() -> T): T {
        synchronized(lock) {
            return object : QueryContext {
                override fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V =
                    this@QueryDatabaseImpl.query(definition, key)
            }.body()
        }
    }

    public override fun <T> writeTransaction(body: DatabaseContext.() -> T): T {
        synchronized(lock) {
            return object : DatabaseContext {
                override fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V =
                    this@QueryDatabaseImpl.query(definition, key)

                override fun <K : Any, V> set(definition: QueryDefinition<K, V>, key: K, value: V) =
                    this@QueryDatabaseImpl.set(definition, key, value)

                override fun <K : Any, V> remove(definition: QueryDefinition<K, V>, key: K) =
                    this@QueryDatabaseImpl.remove(definition, key)
            }.body()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K : Any, V> getQueryStorage(query: QueryDefinition<K, V>): MutableMap<K, Value<V>> =
        storage.computeIfAbsent(query) { mutableMapOf<K, Value<V>>() } as MutableMap<K, Value<V>>

    private fun <K : Any, V> get(queryKey: QueryKey<K, V>): Value<V>? = getQueryStorage(queryKey.queryDef)[queryKey.key]

    private fun <K : Any, V> set(inputDef: QueryDefinition<K, V>, key: K, value: V) {
        val queryStorage = getQueryStorage(inputDef)
        when (val currentValue = queryStorage[key]) {
            is InputValue -> {
                currentValue.value = value
                currentValue.changedAt = ++currentRevision
            }
            else -> queryStorage[key] = InputValue(value, ++currentRevision)
        }
    }

    private fun <K : Any, V> remove(inputDef: QueryDefinition<K, V>, key: K) {
        getQueryStorage(inputDef).remove(key)
        ++currentRevision
    }

    /**
     * Obtains a value for a given query def and key
     */
    private fun <K : Any, V> query(queryDef: QueryDefinition<K, V>, key: K): V = synchronized(lock) {
        fetch(QueryExecutionContextImpl(this), queryDef, key)
    }

    /**
     * Obtains a value for a given query def and key using an existing QueryExecutionContext
     */
    private fun <K : Any, V> fetch(context: QueryExecutionContext, queryDef: QueryDefinition<K, V>, key: K): V {
        val queryKey = QueryKey(queryDef, key)
        val queryStorage = getQueryStorage(queryDef)

        return when (val existingValue = queryStorage[key]) {
            is InputValue<V> -> {
                context.addDependency(queryKey, existingValue.changedAt)
                existingValue.value
            }

            is DerivedValue<V> -> {
                if (deepVerify(context, existingValue)) {
                    context.addDependency(queryKey, existingValue.changedAt)
                    existingValue.value
                } else {
                    execute(context, queryKey, existingValue)
                }
            }

            null -> execute(context, queryKey)
        }
    }

    private fun <K : Any, V> execute(
        context: QueryExecutionContext,
        queryKey: QueryKey<K, V>,
        storage: DerivedValue<V>? = null
    ): V {
        val queryRevision = currentRevision
        val (definition, key) = queryKey
        val (value, frame) = context.withFrame(queryKey) {
            definition.execute(context, key)
        }
        // Sanity check - a query must not modify the database
        assert(queryRevision == currentRevision) { "Database revision was modified during query execution." }

        val queryStorage = getQueryStorage(definition)
        val changedAt = if (storage?.value != null && storage.value == value) {
            // If the new value is equivalent to the previous value, backdate to the previous value's changedAt
            storage.changedAt
        } else {
            frame.maxRevision
        }
        queryStorage[key] = DerivedValue(value, frame.dependencies.toList(), queryRevision, changedAt)

        return value
    }

    /**
     * Checks whether a value is guaranteed to be up-to-date as of this revision. Does not check dependencies.
     */
    private fun shallowVerify(value: Value<*>): Boolean {
        return when (value) {
            is InputValue<*> -> value.changedAt <= currentRevision
            is DerivedValue<*> -> value.verifiedAt == currentRevision
        }
    }

    /**
     * Checks whether a value is up-to-date based on its dependencies.
     *
     * Returns true if the value is considered up-to-date, false if it must be recomputed.
     */
    private fun deepVerify(context: QueryExecutionContext, value: Value<*>): Boolean {
        return when (value) {
            is InputValue<*> -> shallowVerify(value)
            is DerivedValue<*> -> {
                if (shallowVerify(value)) {
                    return true
                }

                val noDepsChanged = value.dependencies.none { dep ->
                    // If the dependency exists, check if it may have changed.
                    // If it does not exist, it has "changed" (likely removed) and thus must be recomputed.
                    get(dep)?.let {
                        maybeChangedAfter(context, dep, it, value.verifiedAt)
                    } ?: true
                }

                if (noDepsChanged) {
                    value.verifiedAt = currentRevision
                }

                return false
            }
        }
    }

    /**
     * Checks if a value may have changed as of the given revision.
     */
    private fun maybeChangedAfter(
        context: QueryExecutionContext,
        key: QueryKey<*, *>,
        value: Value<*>,
        asOfRevision: Int
    ): Boolean {
        if (value is InputValue<*>) {
            return shallowVerify(value)
        }

        if (shallowVerify(value)) {
            return value.changedAt > asOfRevision
        }

        if (deepVerify(context, value)) {
            return value.changedAt > asOfRevision
        }

        val newValue = execute(context, key)
        if (value == newValue) {
            return false
        }

        return true
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

    internal class QueryExecutionContextImpl(private val database: QueryDatabaseImpl) : QueryExecutionContext {
        private val queryStack: MutableList<QueryFrame<*>> = mutableListOf()

        override fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V = database.fetch(this, definition, key)

        private fun checkCanceled() {}

        override fun pushFrame(key: QueryKey<*, *>) {
            checkCanceled()
            if (queryStack.any { it.queryKey == key }) {
                throw IllegalStateException(
                    "Cycle detected: $key already in ${queryStack.joinToString { it.queryKey.queryDef.name }}"
                )
            }
            queryStack.add(QueryFrame(key))
        }

        override fun popFrame(): QueryFrame<*> {
            val endedFrame = queryStack.removeLast()
            queryStack.lastOrNull()?.let { priorFrame ->
                // the completed query is a dependency of the one above it
                priorFrame.dependencies += endedFrame.queryKey
                if (priorFrame.maxRevision < endedFrame.maxRevision) {
                    priorFrame.maxRevision = endedFrame.maxRevision
                }
            }
            return endedFrame
        }

        override fun addDependency(key: QueryKey<*, *>, revision: Int) {
            queryStack.lastOrNull()?.let { frame ->
                frame.dependencies += key
                if (frame.maxRevision < revision) {
                    frame.maxRevision = revision
                }
            }
        }
    }
}
