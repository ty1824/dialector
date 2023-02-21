package dev.dialector.query

import kotlin.reflect.KClass

public annotation class QueryGroup

public annotation class Query

public annotation class Input

public annotation class Tracked

public annotation class DatabaseDef(vararg val groups: KClass<*>)

@QueryGroup
internal interface HelloWorld  {
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

@DatabaseDef(HelloWorld::class)
internal interface MyDatabase : HelloWorld

internal interface DatabaseDefinition {
    val queryDefinitions: Array<DatabaseQuery<*, *>>
}
internal open class QueryGroupDef<I : Any>(val definition: KClass<I>)
internal interface DatabaseQuery<K, V : Value<*>> {
    val group: QueryGroupDef<*>
    val queryIndex: Int
}
internal open class InputQuery<K, V : InputValue<*>>(override val group: QueryGroupDef<*>, override val queryIndex: Int) : DatabaseQuery<K, V>
internal open class DerivedQuery<K, V : DerivedValue<*>>(
    override val group: QueryGroupDef<*>,
    override val queryIndex: Int,
) : DatabaseQuery<K, V>


internal data class QueryKey<K, V : Value<*>>(val queryDef: DatabaseQuery<K, V>, val key: K)

internal sealed interface Value<V> {
    var value: V
    var changedAt: Int
}

internal data class InputValue<V>(override var value: V, override var changedAt: Int): Value<V>

internal data class DerivedValue<V>(
    override var value: V,
    val dependencies: MutableList<QueryKey<*, *>>,
    var verifiedAt: Int,
    override var changedAt: Int
): Value<V>


internal class QueryFrame<K>(
    val queryKey: QueryKey<K, *>,
    var maxRevision: Int = 0,
    val dependencies: MutableList<QueryKey<*, *>> = mutableListOf()
)

internal class MyDatabaseImpl : MyDatabase {
    private companion object : DatabaseDefinition {
        override val queryDefinitions = arrayOf<DatabaseQuery<*, *>>(
            InputString,
            Length,
            Longest
        )
    }
    private object HelloWorldDef : QueryGroupDef<HelloWorld>(HelloWorld::class)
    private object InputString : InputQuery<String, InputValue<String?>>(HelloWorldDef, 0)
    private object Length : DerivedQuery<String, DerivedValue<Int?>>(HelloWorldDef, 1)
    private object Longest : DerivedQuery<Set<String>, DerivedValue<String?>>(HelloWorldDef, 2)

    private var currentRevision = 0

    private val storage: Array<MutableMap<*, *>> = arrayOf(
        mutableMapOf<String, InputValue<String>>(),
        mutableMapOf<String, DerivedValue<Int>>(),
        mutableMapOf<Set<String>, DerivedValue<String>>()
    )

    private val currentlyActiveQuery: MutableList<QueryFrame<*>> = mutableListOf()

    private fun <K, V : Value<*>> getQueryStorage(query: DatabaseQuery<K, V>): MutableMap<K, V> = storage[query.queryIndex] as MutableMap<K, V>
    private fun <K, V : Value<*>> get(queryKey: QueryKey<K, V>): V? = getQueryStorage(queryKey.queryDef)[queryKey.key]

    internal fun setInputString(key: String, value: String) {
        println("Setting inputString: $key to $value")
        val inputStorage: MutableMap<String, InputValue<String>> = storage[InputString.queryIndex] as MutableMap<String, InputValue<String>>
        val inputValue = inputStorage[key]
        if (inputValue == null) {
            inputStorage[key] = InputValue(value, ++currentRevision)
        } else {
            inputValue.value = value
            inputValue.changedAt = ++currentRevision
        }
    }

    override fun inputString(key: String): String? = inputQuery(InputString, key)

    override fun length(key: String): Int? = derivedQuery(Length, key) { super.length(it) }

    override fun longest(keys: Set<String>): String? = derivedQuery(Longest, keys) { super.longest(it) }

    private fun <K, V> inputQuery(queryDef: DatabaseQuery<K, InputValue<V>>, key: K): V? {
        val current = QueryKey(queryDef, key)
        recordQuery(current)
        return get(current)?.let {
            trackRevision(it.changedAt)
            it.value
        }
    }

    private fun <K, V> derivedQuery(queryDef: DatabaseQuery<K, DerivedValue<V>>, key: K, queryLogic: (K) -> V): V {
        val current = QueryKey(queryDef, key)
        val derivedStorage = getQueryStorage(queryDef)
        if (currentlyActiveQuery.any { it.queryKey == current }) {
            throw RuntimeException("Cycle detected: $current already in $currentlyActiveQuery")
        }
        recordQuery(current)
        val existingValue = derivedStorage[key]
        return if (existingValue != null && verify(existingValue, existingValue.verifiedAt)) {
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
        println("Current revision = ${currentRevision}")
        storage.forEachIndexed { index, store ->
            println("Query store: ${queryDefinitions[index]}")
            store.forEach {
                println("  ${it.key} to ${it.value}")
            }
        }
        println("=========================")
    }
}

fun main() {
    val db = MyDatabaseImpl()
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
