package dev.dialector.inkt.example

import dev.dialector.inkt.better.QueryDatabase
import dev.dialector.inkt.better.defineQuery
import dev.dialector.inkt.example.HelloWorldShort.inputString
import dev.dialector.inkt.example.HelloWorldShort.length
import dev.dialector.inkt.example.HelloWorldShort.longest

internal object HelloWorldShort {
    /**
     * Input queries are not required to have an implementation - these will generally depend on
     * externally-provided inputs. This function corresponds to a [String] -> [String] mapping
     *
     * A query database will need to provide means to set these input values.
     */
    val inputString by defineQuery<String, String>()

    /**
     * Derived queries are composed of other queries - in this case, [length] returns the length
     * of the input string for the given key.
     */
    val length by defineQuery { key: String ->
        println("Recomputing length for $key")
        inputString(key)?.length
    }

    /**
     * This is an example of a derived query that depends on another derived query.
     */
    val longest by defineQuery { keys: Set<String> ->
        println("Recomputing longest")
        keys.maxByOrNull { length(it) ?: -1 }?.let { inputString(it) }
    }
}

internal fun main() {
    val db = QueryDatabase()
    db.apply {
        inputString["foo"] = "hello"

        println("foo: Length is ${length("foo")}")
        println("foo: Length is ${length("foo")} shouldn't recompute!")

        inputString["bar"] = "bye"

        println("foo: Length is ${length("foo")} shouldn't recompute!")
        println("bar: Length is ${length("bar")}")
        println("bar: Length is ${length("bar")} shouldn't recompute!")

        inputString["foo"] = "longer"

        println("foo: Length is ${length("foo")}")
        println("bar: Length is ${length("bar")} shouldn't recompute!")

        println("longest {foo, bar} is: ${longest(setOf("foo", "bar"))}")
        println("longest {foo, bar} is: ${longest(setOf("foo", "bar"))}")

        inputString["bar"] = "even longer"
        println("longest {foo, bar} is: ${longest(setOf("foo", "bar"))}")
        println("longest {foo, bar} is: ${longest(setOf("foo", "bar"))}")

        inputString["baz"] = "definitely the longest"
        println("longest {foo, bar, baz} is ${longest(setOf("foo", "bar", "baz"))}")
        println("longest {foo, bar, baz} is ${longest(setOf("foo", "bar", "baz"))}")

        inputString["foo"] = "long"
        inputString["bar"] = "med"
        inputString["baz"] = "s"
        println("longest {foo, bar, baz} is ${longest(setOf("foo", "bar", "baz"))}")
        println("longest {foo, bar, baz} is ${longest(setOf("foo", "bar", "baz"))}")
    }
}
