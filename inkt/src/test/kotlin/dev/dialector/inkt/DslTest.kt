package dev.dialector.inkt

import dev.dialector.inkt.next.QueryContext
import dev.dialector.inkt.next.QueryDefinition
import dev.dialector.inkt.next.defineQuery
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class DslTest {
    @Test
    fun testQueryDefinition() {
        val input by defineQuery<String, String>()
        val derived by defineQuery<Int, Int> { it + it }
        val inputUnit by defineQuery<Int>("daInput")
        val derivedUnit by defineQuery<Int>("derived2") { query(inputUnit) * 2 }
        val transitive by defineQuery<String, Int> {
            val first = query(inputUnit)
            val second = query(derived, first)
            second * second
        }

        assertEquals("derived", derived.name)
        assertEquals("derived2", derivedUnit.name)
        assertEquals("input", input.name)
        assertEquals("daInput", inputUnit.name)

        val context = TestContext()
        assertEquals(4, context.query(derived, 2))
        assertThrows<NotImplementedError> { context.query(inputUnit) }
        assertThrows<NotImplementedError> { context.query(transitive, "aha") }
    }

    class TestContext : QueryContext {
        override fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V =
            definition.execute(this, key)
    }
}
