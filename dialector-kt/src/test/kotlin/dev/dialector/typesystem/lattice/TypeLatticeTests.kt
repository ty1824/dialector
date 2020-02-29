package dev.dialector.typesystem.lattice

import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.type
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class TypeLatticeTests {
    private val a = object : IdentityType("A") {}
    private val b = object : IdentityType("B") {}
    private val c = object : IdentityType("C") {}
    private val d = object : IdentityType("D") {}
    private val e = object : IdentityType("E") {}
    private val f = object : IdentityType("F") {}

    @Test
    fun isSubtypeOfBasicTest() {
        val lattice = DefaultTypeLattice(listOf(
                type(a) hasSupertypes sequenceOf(b),
                type(b) hasSupertypes sequenceOf(c)
        ), listOf())

        assertAll("isSubtypeOf tests",
                { assertTrue(lattice.isSubtypeOf(a, a), "Type a should be subtype of itself") },
                { assertTrue(lattice.isSubtypeOf(b, b), "Type b should be subtype of itself") },
                { assertTrue(lattice.isSubtypeOf(c, c), "Type c should be subtype of itself") },
                { assertTrue(lattice.isSubtypeOf(a, b), "Type b should be direct supertype of a") },
                { assertTrue(lattice.isSubtypeOf(b, c), "Type c should be direct supertype of b") },
                { assertTrue(lattice.isSubtypeOf(a, c), "Type c should be transitive supertype of a") },
                { assertFalse(lattice.isSubtypeOf(c, a), "Type c should not be subtype of a") },
                { assertFalse(lattice.isSubtypeOf(c, b), "Type c should not be subtype of a") },
                { assertFalse(lattice.isSubtypeOf(b, a), "Type c should not be subtype of a") }
        )
    }

    @Test
    fun commonSupertypeTest() {
        val lattice = DefaultTypeLattice(listOf(
                type(a) hasSupertypes sequenceOf(b),
                type(b) hasSupertypes sequenceOf(c),
                type(d) hasSupertypes sequenceOf(e, b),
                type(f) hasSupertypes sequenceOf(a, e)
        ), listOf())

        assertAll("leastCommonSupertypes test",
                { assertEquals(setOf(b), lattice.leastCommonSupertypes(listOf(a, b)), "Degenerate input case") },
                { assertEquals(setOf(b), lattice.leastCommonSupertypes(listOf(a, d)), "Type b is the common supertype of a and d") },
                { assertEquals(setOf(b, e), lattice.leastCommonSupertypes(listOf(d, f)), "Both types b and e are common supertypes") }
        )
    }
}