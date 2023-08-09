package dev.dialector.diagnostic

import dev.dialector.TestNode
import dev.dialector.syntax.Node
import dev.dialector.syntax.given
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DiagnosticRuleTest {

    open class A : TestNode()
    open class B(val data: Int) : A()

    val bFail = "bFail"
    val rule1 = given<B>() check {
        if (it.data < 0) {
            diagnostic(bFail, it)
        }
    }

    val aFail = "aFail"
    val bSubFail = "bSubFail"
    val rule2 = given<A>() check {
        if (it is B) {
            if (it.data > 10) diagnostic(bSubFail, it)
        } else {
            diagnostic(aFail, it)
        }
    }

    class TestContext : DiagnosticContext {
        var lastDiagnostic: Pair<String, Node>? = null
        override fun diagnostic(message: String, node: Node) {
            lastDiagnostic = message to node
        }
    }

    @Test
    fun testDiagnosticRule() {
        with(TestContext()) {
            rule1(B(5), this)
            assertNull(lastDiagnostic)
        }

        with(TestContext()) {
            rule1(B(-1), this)
            val result = assertNotNull(lastDiagnostic)
            assertEquals(bFail, result.first)
        }

        with(TestContext()) {
            rule1(A(), this)
            assertNull(lastDiagnostic)
        }

        with(TestContext()) {
            rule2(B(5), this)
            assertNull(lastDiagnostic)
        }

        with(TestContext()) {
            rule2(B(15), this)
            val result = assertNotNull(lastDiagnostic)
            assertEquals(bSubFail, result.first)
        }

        with(TestContext()) {
            rule2(A(), this)
            val result = assertNotNull(lastDiagnostic)
            assertEquals(aFail, result.first)
        }
    }
}
