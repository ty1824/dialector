package dev.dialector.inkt

import dev.dialector.inkt.next.QueryDatabaseImpl
import dev.dialector.inkt.next.QueryDefinition
import dev.dialector.inkt.next.defineQuery
import dev.dialector.inkt.next.query
import dev.dialector.inkt.next.remove
import dev.dialector.inkt.next.set
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFails

class InvocationCounter {
    private var counter = 0
    fun increment() {
        counter++
    }

    fun checkAndReset(): Int {
        val ret = counter
        counter = 0
        return ret
    }

    fun reset() {
        counter = 0
    }
}

class DatabaseTest {
    private val someInput by defineQuery<Int>("daInput")
    private val otherInput by defineQuery<Int>()
    private val timesTwoCounter = InvocationCounter()
    private val someInputTimesTwo by defineQuery<Int>("derived2") {
        timesTwoCounter.increment()
        query(someInput) * 2
    }
    private val doubleArgument by defineQuery<Int, Int> { it + it }

    private var transitiveInvokeCount = InvocationCounter()
    private val transitive by defineQuery<String, Int> { arg ->
        transitiveInvokeCount.increment()
        val doubledSomeInput = query(doubleArgument, query(someInput))
        doubledSomeInput + arg.length
    }

    private val otherTransitiveCounter = InvocationCounter()
    private val otherTransitive by defineQuery<Int> {
        otherTransitiveCounter.increment()
        query(someInputTimesTwo) + query(otherInput)
    }

    private lateinit var database: QueryDatabaseImpl

    @BeforeTest
    fun init() {
        database = QueryDatabaseImpl()
        timesTwoCounter.reset()
        transitiveInvokeCount.reset()
        otherTransitiveCounter.reset()
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
        assertEquals(2, transitiveInvokeCount.checkAndReset())

        // Change someInput and repeat
        database.set(someInput, 100)
        assertEquals(100, database.query(someInput))
        assertEquals(100, database.query(someInput))
        assertEquals(200, database.query(someInputTimesTwo))
        assertEquals(200, database.query(someInputTimesTwo))
        assertEquals(202, database.query(transitive, "hi"))
        assertEquals(202, database.query(transitive, "hi"))
        assertEquals(203, database.query(transitive, "hi!"))
        assertEquals(2, transitiveInvokeCount.checkAndReset())

        // All calls should fail after removing dependency
        database.remove(someInput)
        assertFails { database.query(someInput) }
        assertFails { database.query(someInputTimesTwo) }
        assertFails { database.query(transitive, "hi") }
        assertFails { database.query(transitive, "hi!") }
    }

    @Test
    fun `caching and invalidation of queries upon change`() {
        database.writeTransaction {
            set(someInput, 5)
            set(otherInput, 100)
            query(otherTransitive) // Should run fully here
            set(otherInput, 100)
            query(otherTransitive) // Should not recompute, setting to the same value
            set(someInput, 6)
            query(otherTransitive) // Should recompute fully
            set(otherInput, 5)
            query(otherTransitive) // Should not re-run times two
            set(someInputTimesTwo, 12)
            query(otherTransitive) // Should not recompute, derived query was explicitly assigned the same value
            remove(someInputTimesTwo)
            query(otherTransitive) // Should recompute fully
            set(someInput, 5)
            set(someInput, 6)
            query(otherTransitive) // Should only recompute intermediate value, result should be the same.
        }

        assertEquals(4, timesTwoCounter.checkAndReset())
        assertEquals(4, otherTransitiveCounter.checkAndReset())
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

        // 2 + 2 = 4, whew
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

    @Test
    fun `print database`() {
        database.writeTransaction {
            set(someInput, 5)
            set(otherInput, 10)
            query(otherTransitive)
            set(someInput, 100)
        }

        val expected = """
            |=========================
            |Current revision = 3
            |Query store: QueryDefinition(daInput)
            |  kotlin.Unit to InputValue(value=100, changedAt=3)
            |Query store: QueryDefinition(otherInput)
            |  kotlin.Unit to InputValue(value=10, changedAt=2)
            |Query store: QueryDefinition(otherTransitive)
            |  kotlin.Unit to DerivedValue(value=20, dependencies=[(derived2, kotlin.Unit), (otherInput, kotlin.Unit)], verifiedAt=2, changedAt=2)
            |Query store: QueryDefinition(derived2)
            |  kotlin.Unit to DerivedValue(value=10, dependencies=[(daInput, kotlin.Unit)], verifiedAt=2, changedAt=1)
            |=========================
            |
        """.trimMargin()

        val os = ByteArrayOutputStream(1024)
        val originalOut = System.out
        try {
            System.setOut(PrintStream(os))
            database.print()
            assertEquals(expected, os.toString())
        } finally {
            System.setOut(originalOut)
        }
    }
}
