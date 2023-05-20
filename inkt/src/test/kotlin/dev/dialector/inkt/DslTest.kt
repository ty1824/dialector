package dev.dialector.inkt

import dev.dialector.inkt.next.QueryContext
import dev.dialector.inkt.next.QueryDefinition
import dev.dialector.inkt.next.defineQuery
import dev.dialector.inkt.next.notImplementedMessage
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class DslTest {
    private val unimplemented by defineQuery<String, String>()
    private val argumentDependent by defineQuery<Int, Int> { it + it }
    private val unimplementedUnit by defineQuery<Int>("daInput")
    private val derivedUnit by defineQuery<Int>("derived2") { query(unimplementedUnit) * 2 }
    private val transitive by defineQuery<String, Int> {
        val first = query(unimplementedUnit)
        val second = query(argumentDependent, first)
        second * second + first
    }

    @Test
    fun testQueryDefinitionName() {
        assertEquals("argumentDependent", argumentDependent.name)
        assertEquals("derived2", derivedUnit.name)
        assertEquals("unimplemented", unimplemented.name)
        assertEquals("daInput", unimplementedUnit.name)
    }

    @Test
    fun testBaseQueryExecution() {
        val context = TestContext()
        assertEquals(4, context.query(argumentDependent, 2))
    }

    @Test
    fun testQueryFailures() {
        val context = TestContext()

        // No-argument query with no implementation throws exception with overridden query name & Unit as key
        val e1 = assertThrows<NotImplementedError> { context.query(unimplementedUnit) }
        assertEquals(notImplementedMessage("daInput", Unit), e1.message)

        // Query with no implementation throws exception with the query name & key
        val e2 = assertThrows<NotImplementedError> { context.query(unimplemented, "name") }
        assertEquals(notImplementedMessage("unimplemented", "name"), e2.message)

        // Transitive query failure
        val e3 = assertThrows<NotImplementedError> { context.query(transitive, "aha") }
        assertEquals(notImplementedMessage("daInput", Unit), e3.message)
    }

    class TestContext : QueryContext {
        override fun <K : Any, V> query(definition: QueryDefinition<K, V>, key: K): V =
            definition.execute(this, key)
    }
}
