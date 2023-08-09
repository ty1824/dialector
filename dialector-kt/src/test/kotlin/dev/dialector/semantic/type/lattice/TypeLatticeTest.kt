package dev.dialector.semantic.type.lattice

import dev.dialector.assertAll
import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.type
import dev.dialector.semantic.type.typeClass
import dev.dialector.semantic.type.typeClause
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeLatticeTest {
    private object Any : IdentityType("Any")
    private object A : IdentityType("A")
    private object B : IdentityType("B")
    private object C : IdentityType("C")
    private object D : IdentityType("D")
    private object E : IdentityType("E")
    private object F : IdentityType("F")
    private object G : IdentityType("G")
    private data class X(val bound: Int) : Type

    @Test
    fun isSubtypeOfBasicTest() {
        val lattice = StandardTypeLattice(
            listOf(
                type(A) hasSupertype B,
                type(B) hasSupertype C,
            ),
            listOf(),
        )

        assertAll(
            "isSubtypeOf tests",
            { assertTrue(lattice.isSubtypeOf(A, A, Unit), "Type a should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(B, B, Unit), "Type b should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(C, C, Unit), "Type c should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(A, B, Unit), "Type b should be direct supertype of a") },
            { assertTrue(lattice.isSubtypeOf(B, C, Unit), "Type c should be direct supertype of b") },
            { assertTrue(lattice.isSubtypeOf(A, C, Unit), "Type c should be transitive supertype of a") },
            { assertFalse(lattice.isSubtypeOf(C, A, Unit), "Type c should not be subtype of a") },
            { assertFalse(lattice.isSubtypeOf(C, B, Unit), "Type c should not be subtype of a") },
            { assertFalse(lattice.isSubtypeOf(B, A, Unit), "Type c should not be subtype of a") },
        )
    }

    @Test
    fun commonSupertypeTest() {
        val lattice = StandardTypeLattice(
            listOf(
                typeClass<Type>() hasSupertype Any,
                type(A) hasSupertype C,
                type(B) hasSupertype C,
                type(D) hasSupertypes listOf(C, E),
                type(F) hasSupertypes listOf(A, E),
            ),
            listOf(),
        )

        assertAll(
            "leastCommonSupertypes test",
            { assertEquals(setOf(C), lattice.leastCommonSupertypes(listOf(A, B), Unit), "Type c is the common supertype of a and d") },
            { assertEquals(setOf(C), lattice.leastCommonSupertypes(listOf(A, C), Unit), "Degenerate input case") },
            { assertEquals(setOf(Any), lattice.leastCommonSupertypes(listOf(A, B, D, E), Unit), "All inputs only share the any supertype") },
        )
    }

    @Test
    fun `multiple valid common supertypes`() {
        val lattice = StandardTypeLattice(
            listOf(
                typeClass<Type>() hasSupertype Any,
                type(A) hasSupertypes listOf(G, F),
                type(B) hasSupertypes listOf(G, F),
                type(C) hasSupertypes listOf(G, F),
            ),
            listOf(),
        )
        assertEquals(setOf(G, F), lattice.leastCommonSupertypes(listOf(A, B, C), Unit))
    }

    @Test
    fun supertypeRuleTest() {
        val rule = typeClause<X, Unit> { it.bound > 0 } hasSupertypes { _ -> listOf(A, B) }
        assertEquals(setOf(A, B), rule.evaluate(X(1), Unit).toSet())
        assertEquals(emptySequence(), rule.evaluate(X(-1), Unit))
    }
}
