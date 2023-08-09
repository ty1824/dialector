package dev.dialector.syntax

import dev.dialector.TestNode
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NodePredicateTest {
    open class A : TestNode()

    data class B(val bound: Int) : A()
    object C : A()

    data class X(val bound: Int) : TestNode()

    @Test
    fun typePredicates() {
        val instancePredicate = given(B(2))
        val classPredicate = given<B>()
        val logicalPredicate = given<A, String> {
            this == "hi" || (it is B && it.bound == 0)
        }

        assertTrue(instancePredicate(B(2), Unit))
        assertFalse(instancePredicate(B(1), Unit))
        assertFalse(instancePredicate(C, Unit))

        assertTrue(classPredicate(B(1), Unit))
        assertTrue(classPredicate(B(-1), Unit))
        assertFalse(classPredicate(C, Unit))

        assertTrue(logicalPredicate(B(0), ""))
        assertTrue(logicalPredicate(C, "hi"))
        assertFalse(logicalPredicate(C, ""))
        assertFalse(logicalPredicate(X(1), ""))
        assertFalse(logicalPredicate(X(1), "hi"))
    }
}
