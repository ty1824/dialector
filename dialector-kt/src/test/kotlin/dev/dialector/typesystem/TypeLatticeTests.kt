package dev.dialector.typesystem

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class TypeLatticeTests {

    @Test
    fun isSubtypeOfBasicTest() {
        val a = object : IdentityType() {}
        val b = object : IdentityType() {}
        val c = object : IdentityType() {}

        val lattice = DefaultTypeLattice(listOf(
                type(a) hasSupertypes sequenceOf(b),
                type(b) hasSupertypes sequenceOf(c)
        ), listOf())

        assertAll("basic lattice",
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
}