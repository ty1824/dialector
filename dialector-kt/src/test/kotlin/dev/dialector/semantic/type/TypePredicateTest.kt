package dev.dialector.semantic.type

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypePredicateTest {
    open class A : Type {
        override fun equals(other: Any?): Boolean = other === this

        override fun hashCode(): Int = 1

        override fun toString(): String = "A"
    }

    data class B(val bound: Int) : A()
    object C : A()

    data class X(val bound: Int) : Type

    @Test
    fun typePredicates() {
        val instancePredicate = type(C)
        val classPredicate = typeClass<B>()
        val logicalPredicate = typeClause<A, String> {
            this == "hi" || (it is B && it.bound == 0)
        }

        assertTrue(instancePredicate(C, Unit))
        assertFalse(instancePredicate(B(1), Unit))

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
