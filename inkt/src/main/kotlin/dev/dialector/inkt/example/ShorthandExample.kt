package dev.dialector.inkt.example

import dev.dialector.inkt.example.HelloWorldShort.inputs
import dev.dialector.inkt.example.HelloWorldShort.length
import dev.dialector.inkt.example.HelloWorldShort.longest
import dev.dialector.inkt.example.HelloWorldShort.totalLength
import dev.dialector.inkt.next.DatabaseContext
import dev.dialector.inkt.next.QueryDatabaseImpl
import dev.dialector.inkt.next.defineQuery

internal object HelloWorldShort {
    /**
     * Input queries are not required to have an implementation - these will generally depend on
     * externally-provided inputs. This function corresponds to a [String] -> [String] mapping
     *
     * A query database will need to provide means to set these input values.
     */
    val inputs by defineQuery<List<Pair<String, String>>>()

    val inputString by defineQuery<String, String?> { key: String ->
        query(inputs).find { it.first == key }?.second
    }

    /**
     * Derived queries are composed of other queries - in this case, [length] returns the length
     * of the input string for the given key.
     */
    val length by defineQuery { key: String ->
        println("Recomputing length for $key")
        query(inputString, key)?.length
    }

    /**
     * This is an example of a derived query that depends on another derived query.
     */
    val longest by defineQuery { keys: Set<String> ->
        println("Recomputing longest")
        keys.maxByOrNull { query(length, it) ?: 0 }?.let { query(inputString, it) }
    }

    val totalLength by defineQuery<Int> {
        query(inputs).sumOf { query(length, it.first) ?: 0 }
    }
}

internal fun main() {
    val db = QueryDatabaseImpl()
    db.writeTransaction {
        logic()
        logic()
        logic()
    }
}

private fun DatabaseContext.logic() {
    set(inputs, listOf("foo" to "hello"))
//        set(inputString, "foo", "hello")
    println("foo: Length is ${query(length, "foo")}")
    println("foo: Length is ${query(length, "foo")} shouldn't recompute!")

    set(inputs, listOf("foo" to "hello", "bar" to "bye"))
//        set(inputString, "bar", "bye")

    println("foo: Length is ${query(length, "foo")} shouldn't recompute!")
    println("bar: Length is ${query(length, "bar")}")
    println("bar: Length is ${query(length, "bar")} shouldn't recompute!")

    set(inputs, listOf("foo" to "longer", "bar" to "bye"))
//        set(inputString, "foo", "longer")

    println("foo: Length is ${query(length, "foo")}")
    println("bar: Length is ${query(length, "bar")} shouldn't recompute!")

    println("longest {longer, bye} is: ${query(longest, setOf("foo", "bar"))}")
    println("longest {longer, bye} is: ${query(longest, setOf("foo", "bar"))} shouldn't recompute!")

    set(inputs, listOf("foo" to "longer", "bar" to "even longer"))
//        inputString["bar"] = "even longer"
    println("longest {foo, bar} is: ${query(longest, setOf("foo", "bar"))}")
    println("longest {foo, bar} is: ${query(longest, setOf("foo", "bar"))}")
    println("total length: ${query(totalLength)}")

    set(inputs, listOf("foo" to "longer", "bar" to "even longer", "baz" to "definitely the longest"))
//        inputString["baz"] = "definitely the longest"
    println("longest {foo, bar, baz} is ${query(longest, setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${query(longest, setOf("foo", "bar", "baz"))}")
    println("total length: ${query(totalLength)}")

    set(inputs, listOf("foo" to "long", "bar" to "med", "baz" to "s"))
//        inputString["foo"] = "long"
//        inputString["bar"] = "med"
//        inputString["baz"] = "s"
    println("longest {foo, bar, baz} is ${query(longest, setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${query(longest, setOf("foo", "bar", "baz"))}")
    println("total length: ${query(totalLength)}")
}
