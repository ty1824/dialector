package dev.dialector.typesystem.lattice

import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.type
import dev.dialector.typesystem.typeClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class TypeLatticeTests {
    private val any = object : IdentityType("Any") {}
    private val a = object : IdentityType("A") {}
    private val b = object : IdentityType("B") {}
    private val c = object : IdentityType("C") {}
    private val d = object : IdentityType("D") {}
    private val e = object : IdentityType("E") {}
    private val f = object : IdentityType("F") {}
    private val g = object : IdentityType("G") {}

    @Test
    fun isSubtypeOfBasicTest() {
        val lattice = SimpleTypeLattice(listOf(
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
        val lattice = SimpleTypeLattice(listOf(
                typeClass(Type::class) hasSupertypes sequenceOf(any),
                type(a) hasSupertypes sequenceOf(c),
                type(b) hasSupertypes sequenceOf(c),
                type(d) hasSupertypes sequenceOf(c, e),
                type(f) hasSupertypes sequenceOf(a, e)
        ), listOf())

        assertAll("leastCommonSupertypes test",
                { assertEquals(setOf(c), lattice.leastCommonSupertypes(listOf(a, b)), "Type c is the common supertype of a and d") },
                { assertEquals(setOf(c), lattice.leastCommonSupertypes(listOf(a, c)), "Degenerate input case") },
                { assertEquals(setOf(any), lattice.leastCommonSupertypes(listOf(a, b, d, e)), "All inputs only share the any supertype") }
        )
    }

    @Test
    fun `multiple valid common supertypes`() {
        val lattice = SimpleTypeLattice(listOf(
                typeClass(Type::class) hasSupertypes sequenceOf(any),
                type(a) hasSupertypes sequenceOf(g, f),
                type(b) hasSupertypes sequenceOf(g, f),
                type(c) hasSupertypes sequenceOf(g, f)
        ), listOf())

        assertEquals(setOf(g, f), lattice.leastCommonSupertypes(listOf(a, b, c)))
    }
}