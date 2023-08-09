package dev.dialector.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

open class A
open class B : A()
data class C(val data: Int) : B()
class D : B()

class X

data class Ctx(val arg: String)

class TypesafePredicateTests {

    @Test
    fun instancePredicate() {
        val predicate = object : InstancePredicate<C, Ctx>(C(1)) {}
        val context = Ctx("hi")

        assertTrue(predicate(C(1), context))
        assertFalse(predicate(C(2), context))
        assertFalse(predicate(D(), context))
    }

    @Test
    fun classifierPredicate() {
        val predicate = object : ClassifierPredicate<B, Ctx>(B::class) {}
        val context = Ctx("hi")

        assertTrue(predicate(B(), context))
        assertTrue(predicate(C(1), context))
        assertFalse(predicate(A(), context))
        assertFalse(predicate(X(), context))
    }

    @Test
    fun logicalPredicate() {
        val predicate = object : LogicalPredicate<C, String>(
            C::class,
            { this == "hi" || it.data > 0 },
        ) {}

        assertTrue(predicate(C(1), "hi"))
        assertTrue(predicate(C(1), "bye"))
        assertTrue(predicate(C(-1), "hi"))
        assertFalse(predicate(C(-1), "bye"))

        assertFalse(predicate(D(), "hi"))
        assertFalse(predicate(D(), "bye"))
    }

    @Test
    fun runIfValid() {
        val predicate = object : LogicalPredicate<C, String>(
            C::class,
            { this == "hi" || it.data > 0 },
        ) {}

        assertEquals(1, predicate.runIfValid(C(1), "hi") { 1 })
        assertEquals(1, predicate.runIfValid(C(-1), "hi") { 1 })

        assertNull(predicate.runIfValid(D(), "hi") { 1 })
        assertNull(predicate.runIfValid(C(-1), "bye") { 1 })
        assertNull(predicate.runIfValid(D(), "bye") { 1 })
    }
}
