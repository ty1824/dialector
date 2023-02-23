package dev.dialector.query

@QueryGroup
internal interface HelloWorldGen  {
    @Input
    fun inputString(key: String): String?

    @Tracked
    fun length(key: String): Int? {
        println("Recomputing length for $key")
        return inputString(key)?.length
    }

    fun longest(keys: Set<String>): String? {
        println("recomputing longest")
        return keys.maxByOrNull { length(it) ?: -1 }?.let { inputString(it) }
    }

}

@DatabaseDef(HelloWorldGen::class)
internal interface MyDatabase : HelloWorldGen

internal class GeneratedQueryExample : MyDatabase {
    private companion object : DatabaseDefinition {
        override val queryDefinitions = arrayOf<DatabaseQuery<*, *>>(
            InputString,
            Length,
            Longest
        )
    }
    private object HelloWorldDef : QueryGroupDef<HelloWorldGen>(HelloWorldGen::class)
    private object InputString : InputQuery<String, String?>(HelloWorldDef)
    private object Length : DerivedQuery<String, Int?>(HelloWorldDef)
    private object Longest : DerivedQuery<Set<String>, String?>(HelloWorldDef)

    private var currentRevision = 0

    private val storage: Map<DatabaseQuery<*, *>, MutableMap<*, *>> = queryDefinitions.associateWith { it.createMap() }

    private val currentlyActiveQuery: MutableList<QueryFrame<*>> = mutableListOf()

    private fun <K, V> getQueryStorage(query: DatabaseQuery<K, V>): MutableMap<K, Value<V>> = storage[query] as MutableMap<K, Value<V>>
    private fun <K, V> get(queryKey: QueryKey<K, V>): Value<V>? = getQueryStorage(queryKey.queryDef)[queryKey.key]

    internal fun setInputString(key: String, value: String) = setInput(InputString, key, value)

    override fun inputString(key: String): String? = inputQuery(InputString, key)

    override fun length(key: String): Int? = derivedQuery(Length, key) { super.length(it) }

    override fun longest(keys: Set<String>): String? = derivedQuery(Longest, keys) { super.longest(it) }

    private fun <K, V> setInput(inputDef: InputQuery<K, V>, key: K, value: V) {
        val inputStorage = getQueryStorage(inputDef)
        val inputValue = inputStorage[key]
        if (inputValue == null) {
            inputStorage[key] = InputValue(value, ++currentRevision)
        } else {
            inputValue.value = value
            inputValue.changedAt = ++currentRevision
        }
    }

    private fun <K, V> inputQuery(queryDef: DatabaseQuery<K, V>, key: K): V {
        val current = QueryKey(queryDef, key)
        recordQuery(current)
        return get(current)?.let {
            trackRevision(it.changedAt)
            it.value
        } ?: throw RuntimeException("No value when running query $queryDef for input $key")
    }

    private fun <K, V> derivedQuery(queryDef: DatabaseQuery<K, V>, key: K, queryLogic: (K) -> V): V {
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
                val result = queryLogic(key)
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
                if (value.verifiedAt == currentRevision) {
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

    fun print() {
        println("=========================")
        println("Current revision = $currentRevision")
        storage.forEach { (query, store) ->
            println("Query store: $query")
            store.forEach {
                println("  ${it.key} to ${it.value}")
            }
        }
        println("=========================")
    }
}

internal fun main() {
    val db = GeneratedQueryExample()
    db.setInputString("foo", "hello world")

    println("foo: Length is ${db.length("foo")}")
    println("foo: Length is ${db.length("foo")} shouldn't recompute!")

    db.setInputString("bar", "bai")

    println("foo: Length is ${db.length("foo")} shouldn't recompute!")
    println("bar: Length is ${db.length("bar")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

    db.setInputString("foo", "oh wow this is very long")

    println("foo: Length is ${db.length("foo")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")
//    db.print()
    db.setInputString("bar", "super long to verify some stuff hereeeeeeeeee")
//    db.print()
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")
//    db.print()
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")

    db.setInputString("baz", "the longest there ever was, because it's criticalllll")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")

    db.setInputString("foo", "long")
    db.setInputString("bar", "med")
    db.setInputString("baz", "s")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")

}