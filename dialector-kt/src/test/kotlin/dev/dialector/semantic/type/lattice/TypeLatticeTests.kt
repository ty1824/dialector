package dev.dialector.semantic.type.lattice

import assertAll
import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.type
import dev.dialector.semantic.type.typeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypeLatticeTests {
    private object Any : IdentityType("Any")
    private object A : IdentityType("A")
    private object B : IdentityType("B")
    private object C : IdentityType("C")
    private object D : IdentityType("D")
    private object E : IdentityType("E")
    private object F : IdentityType("F")
    private object G : IdentityType("G")

    @Test
    fun isSubtypeOfBasicTest() {
        val lattice = SimpleTypeLattice(
            listOf(
                type(A) hasSupertypes sequenceOf(B),
                type(B) hasSupertypes sequenceOf(C)
            ),
            listOf()
        )

        assertAll(
            "isSubtypeOf tests",
            { assertTrue(lattice.isSubtypeOf(A, A), "Type a should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(B, B), "Type b should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(C, C), "Type c should be subtype of itself") },
            { assertTrue(lattice.isSubtypeOf(A, B), "Type b should be direct supertype of a") },
            { assertTrue(lattice.isSubtypeOf(B, C), "Type c should be direct supertype of b") },
            { assertTrue(lattice.isSubtypeOf(A, C), "Type c should be transitive supertype of a") },
            { assertFalse(lattice.isSubtypeOf(C, A), "Type c should not be subtype of a") },
            { assertFalse(lattice.isSubtypeOf(C, B), "Type c should not be subtype of a") },
            { assertFalse(lattice.isSubtypeOf(B, A), "Type c should not be subtype of a") }
        )
    }

    @Test
    fun commonSupertypeTest() {
        val lattice = SimpleTypeLattice(
            listOf(
                typeClass<Type>() hasSupertypes sequenceOf(Any),
                type(A) hasSupertypes sequenceOf(C),
                type(B) hasSupertypes sequenceOf(C),
                type(D) hasSupertypes sequenceOf(C, E),
                type(F) hasSupertypes sequenceOf(A, E)
            ),
            listOf()
        )

        assertAll(
            "leastCommonSupertypes test",
            { assertEquals(setOf(C), lattice.leastCommonSupertypes(listOf(A, B)), "Type c is the common supertype of a and d") },
            { assertEquals(setOf(C), lattice.leastCommonSupertypes(listOf(A, C)), "Degenerate input case") },
            { assertEquals(setOf(Any), lattice.leastCommonSupertypes(listOf(A, B, D, E)), "All inputs only share the any supertype") }
        )
    }

    @Test
    fun `multiple valid common supertypes`() {
        val lattice = SimpleTypeLattice(
            listOf(
                typeClass<Type>() hasSupertypes sequenceOf(Any),
                type(A) hasSupertypes sequenceOf(G, F),
                type(B) hasSupertypes sequenceOf(G, F),
                type(C) hasSupertypes sequenceOf(G, F)
            ),
            listOf()
        )
        assertEquals(setOf(G, F), lattice.leastCommonSupertypes(listOf(A, B, C)))
    }

    @Test
    fun supertypeRuleTest() {
    }
}
