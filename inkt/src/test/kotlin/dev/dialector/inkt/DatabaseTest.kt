package dev.dialector.inkt

import dev.dialector.inkt.next.QueryDatabase
import dev.dialector.inkt.next.QueryDatabaseImpl
import dev.dialector.inkt.next.QueryDefinition
import dev.dialector.inkt.next.defineQuery
import dev.dialector.inkt.next.query
import dev.dialector.inkt.next.remove
import dev.dialector.inkt.next.set
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DatabaseTest {
    private val someInput by defineQuery<Int>("daInput")
    private val someInputTimesTwo by defineQuery<Int>("derived2") { query(someInput) * 2 }
    private val doubleArgument by defineQuery<Int, Int> { it + it }

    private var transitiveInvokeCount = 0
    private val transitive by defineQuery<String, Int> { arg ->
        transitiveInvokeCount++
        val doubledSomeInput = query(doubleArgument, query(someInput))
        doubledSomeInput + arg.length
    }

    private lateinit var database: QueryDatabase

    @BeforeTest
    fun init() {
        database = QueryDatabaseImpl()
        transitiveInvokeCount = 0
    }

    @Test
    fun basicExecution() {
        // Run each query twice to ensure consistency
        database.set(someInput, 5)
        assertEquals(5, database.query(someInput))
        assertEquals(5, database.query(someInput))
        assertEquals(10, database.query(someInputTimesTwo))
        assertEquals(10, database.query(someInputTimesTwo))
        assertEquals(12, database.query(transitive, "hi"))
        assertEquals(12, database.query(transitive, "hi"))
        assertEquals(13, database.query(transitive, "hi!"))

        // Verify that the `transitive` query was only invoked twice, once for each unique argument
        assertEquals(2, transitiveInvokeCount)
        transitiveInvokeCount = 0

        // Change someInput and repeat
        database.set(someInput, 100)
        assertEquals(100, database.query(someInput))
        assertEquals(100, database.query(someInput))
        assertEquals(200, database.query(someInputTimesTwo))
        assertEquals(200, database.query(someInputTimesTwo))
        assertEquals(202, database.query(transitive, "hi"))
        assertEquals(202, database.query(transitive, "hi"))
        assertEquals(203, database.query(transitive, "hi!"))
        assertEquals(2, transitiveInvokeCount)
        transitiveInvokeCount = 0

        // All calls should fail after removing dependency
        database.remove(someInput)
        assertFails { database.query(someInput) }
        assertFails { database.query(someInputTimesTwo) }
        assertFails { database.query(transitive, "hi") }
        assertFails { database.query(transitive, "hi!") }
    }

    @Test
    fun implementationWithExplicitValue() {
        assertEquals(4, database.query(doubleArgument, 2))
        assertEquals(6, database.query(doubleArgument, 3))

        // 2 + 2 = 5
        database.set(doubleArgument, 2, 5)
        assertEquals(5, database.query(doubleArgument, 2))
        // Result for 3 should be unchanged
        assertEquals(6, database.query(doubleArgument, 3))

        database.remove(doubleArgument, 2)
        assertEquals(4, database.query(doubleArgument, 2))
        assertEquals(6, database.query(doubleArgument, 3))
    }

    val cyclicQuery: QueryDefinition<Int, Int> by defineQuery { arg ->
        arg + query(cyclicQuery, arg)
    }
    val possiblyCyclic: QueryDefinition<Int, Int> by defineQuery { arg ->
        if (arg < 2 && arg % 2 == 1) {
            arg
        } else {
            query(possiblyCyclic, arg % 2)
        }
    }

    @Test
    fun cyclicQueryDetection() {
        assertFails { database.query(cyclicQuery, 2) }

        assertEquals(1, database.query(possiblyCyclic, 1))
        assertEquals(1, database.query(possiblyCyclic, 3))
        assertEquals(1, database.query(possiblyCyclic, 5))
        assertFails { database.query(possiblyCyclic, 2) }
        assertFails { database.query(possiblyCyclic, 4) }
        assertFails { database.query(possiblyCyclic, 8) }
    }
}
