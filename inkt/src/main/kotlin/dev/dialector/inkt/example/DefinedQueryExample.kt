package dev.dialector.inkt.example

import dev.dialector.inkt.DerivedQuery
import dev.dialector.inkt.InputQuery
import dev.dialector.inkt.NoInputDefinedException
import dev.dialector.inkt.QueryDatabase
import dev.dialector.inkt.derivedQuery
import dev.dialector.inkt.inputQuery

/**
 * Inkt queries begin as interface definitions. It is easiest to provide default implementations
 * as the query database can then invoke the super method, though this approach is not required.
 */
internal interface HelloWorld {
    /**
     * Input queries are not required to have an implementation - these will generally depend on
     * externally-provided inputs. This function corresponds to a [String] -> [String] mapping
     *
     * A query database will need to provide means to set these input values.
     */
    fun inputString(key: String): String?

    /**
     * Derived queries are composed of other queries - in this case, [length] returns the length
     * of the input string for the given key.
     */
    fun length(key: String): Int? {
        println("Recomputing length for $key")
        return inputString(key)?.length
    }

    /**
     * This is an example of a derived query that depends on another derived query.
     */
    fun longest(keys: Set<String>): String? {
        println("recomputing longest")
        return keys.maxByOrNull { length(it) ?: -1 }?.let { inputString(it) }
    }
}

/**
 * A query database implementation is an implementation of one or more query interfaces, a series of
 * [DatabaseQuery] definitions that represent the implemented queries, along with an internal
 * [QueryDatabase] to provide incremental behavior.
 *
 * The [DatabaseQuery] definitions are typesafe handles for the query functionality and must be passed
 * to the [QueryDatabase] constructor. This is to allow for internal optimization of query storage.
 *
 * The [QueryDatabase.inputQuery] and [QueryDatabase.derivedQuery] methods handle fetching different types of data
 * from the database and ensuring the queries are incrementalized.
 *
 * The [QueryDatabase.setInput] method handles assigning input data for input queries.
 */
internal class DefinedQueryExampleDatabase : HelloWorld {
    private val inputString: InputQuery<String, String?> = inputQuery("inputString") {
        throw NoInputDefinedException("Input not provided for inputString($it)")
    }
    private val length: DerivedQuery<String, Int?> = derivedQuery("length") { super.length(it) }
    private val longest: DerivedQuery<Set<String>, String?> = derivedQuery("longest") { super.longest(it) }
    private val database = QueryDatabase(listOf(inputString, length, longest))

    fun setInputString(key: String, value: String?) = database.setInput(inputString, key, value)

    override fun inputString(key: String): String? = database.inputQuery(inputString, key)

    override fun length(key: String): Int? = database.derivedQuery(length, key)

    override fun longest(keys: Set<String>): String? = database.derivedQuery(longest, keys)
}

internal fun main() {
    val db = DefinedQueryExampleDatabase()
    db.setInputString("foo", "hello")

    println("foo: Length is ${db.length("foo")}")
    println("foo: Length is ${db.length("foo")} shouldn't recompute!")

    db.setInputString("bar", "bye")

    println("foo: Length is ${db.length("foo")} shouldn't recompute!")
    println("bar: Length is ${db.length("bar")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

    db.setInputString("foo", "longer")

    println("foo: Length is ${db.length("foo")}")
    println("bar: Length is ${db.length("bar")} shouldn't recompute!")

    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")

    db.setInputString("bar", "even longer")
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")
    println("longest {foo, bar} is: ${db.longest(setOf("foo", "bar"))}")

    db.setInputString("baz", "definitely the longest")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")

    db.setInputString("foo", "long")
    db.setInputString("bar", "med")
    db.setInputString("baz", "s")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")
    println("longest {foo, bar, baz} is ${db.longest(setOf("foo", "bar", "baz"))}")
}
