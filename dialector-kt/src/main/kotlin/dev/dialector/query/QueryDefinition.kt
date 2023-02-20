package dev.dialector.query

//interface Query<I, O>

//public interface QueryGroup
//
//fun queryGroup(block: () -> Unit): QueryGroup {
//
//}
//
//private fun testDsl(): QueryGroup =
//    queryGroup {
//
//    }
//
//
//object HelloWorld : QueryGroup {
//
//}

interface QueryDefinition<I, O>

annotation class QueryGroup

annotation class Query

annotation class Input

@QueryGroup
interface HelloWorld  {
    @Input
    fun inputString(key: String): String?

    fun length(key: String): Int? {
        println("Recomputing for $key")
        return inputString(key)?.length
    }

}

internal sealed interface QueryDef<K, V : Value<*>>

internal data class QueryKey<K, V : Value<*>>(val queryDef: QueryDef<K, V>, val key: K)

internal sealed interface Value<V> {
    var value: V
    var changedAt: Int
}

internal class InputValue<V>(override var value: V, override var changedAt: Int): Value<V>

internal class DerivedValue<V>(
    override var value: V,
    val dependencies: MutableList<QueryKey<*, *>>,
    var verifiedAt: Int,
    override var changedAt: Int
): Value<V>

internal sealed interface Storage<K, V>
//internal class InputStorage<K, V> : Storage<K, V> {
//
//    operator fun set(key: K, value: InputValue<V>)
//    operator fun get(key: K): V =
//}

//internal class Storage<K, V>(val data: MutableMap<K, V>)

internal class QueryFrame<K>(
    val queryKey: QueryKey<K, *>,
    var maxRevision: Int = 0,
    val dependencies: MutableList<QueryKey<*, *>> = mutableListOf()
)


class Database : HelloWorld {
    private object InputString : QueryDef<String, InputValue<String>>
    private object Length : QueryDef<String, DerivedValue<Int>>

    private var currentRevision = 0

    private val storage: MutableMap<QueryKey<*, *>, Value<*>> = mutableMapOf()
    private val currentlyActiveQuery: MutableList<QueryFrame<*>> = mutableListOf()

    private fun <K, V : Value<*>> get(key: QueryKey<K, V>): V? = storage[key] as V?

    internal fun setInputString(key: String, value: String) {
        storage[QueryKey(InputString, key)] = InputValue(value, ++currentRevision)
    }

    override fun inputString(key: String): String? {
        val current = QueryKey(InputString, key)
        recordQuery(current)
        return get(current)?.let {
            trackRevision(it.changedAt)
            it.value
        }
    }

    override fun length(key: String): Int? {
        val current = QueryKey(Length, key)
        if (currentlyActiveQuery.any { it.queryKey == current }) {
            throw RuntimeException("Cycle detected: $current already in $currentlyActiveQuery")
        }
        recordQuery(current)
        return get(current)?.let { value ->
            if (value.verifiedAt == currentRevision) {
                // Memoized and verified
                value.value
            } else if (value.dependencies.all { get(it)!!.changedAt <= value.verifiedAt }) {
                // Memoized and is now verified
                value.verifiedAt = currentRevision
                value.value
            } else {
                null
            }
        } ?: run {
            currentlyActiveQuery += QueryFrame(current)
            try {
                val result = super.length(key)
                val frame = currentlyActiveQuery.last()
                storage[current] = DerivedValue(result, frame.dependencies.toMutableList(), frame.maxRevision, frame.maxRevision)
                result
            } finally {
                val frame = currentlyActiveQuery.removeLast()
                trackRevision(frame.maxRevision)
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
}

fun main() {
    val db = Database()
    db.setInputString("foo", "hello world")

    println("foo: Length is ${db.length("foo")}")
    println("foo: Length is ${db.length("foo")} shouldn't recompute!")

    db.setInputString("bar", "bai")

    println("foo: Length is ${db.length("foo")} shouldn't recompute!")
    println("bar: Length is ${db.length("bar")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

    db.setInputString("foo", "oh wow this is verrrrry long")

    println("foo: Length is ${db.length("foo")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

}

object HelloWorldQueries {

}