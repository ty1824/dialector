package dev.dialector.query

internal interface HelloWorld {
    fun inputString(key: String): String?

    fun length(key: String): Int? {
        println("Recomputing length for $key")
        return inputString(key)?.length
    }

    fun longest(keys: Set<String>): String? {
        println("recomputing longest")
        return keys.maxByOrNull { length(it) ?: -1 }?.let { inputString(it) }
    }
}

public class NoInputDefinedException(message: String) : RuntimeException(message)

internal class DefinedQueryExample : HelloWorld {
    private val inputString: InputQuery<String, String?> = inputQuery("inputString") { throw NoInputDefinedException("Input not provided for inputString($it)") }
    private val length: DerivedQuery<String, Int?> = derivedQuery("length") { super.length(it) }
    private val longest: DerivedQuery<Set<String>, String?> = derivedQuery("longest") { super.longest(it) }
    private val database = QueryDatabase(listOf(inputString, length, longest))

    fun setInputString(key: String, value: String?) = database.setInput(inputString, key, value)

    override fun inputString(key: String): String? = database.inputQuery(inputString, key)

    override fun length(key: String): Int? = database.derivedQuery(length, key)

    override fun longest(keys: Set<String>): String? = database.derivedQuery(longest, keys)
}

internal fun main() {
    val db = DefinedQueryExample()
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
