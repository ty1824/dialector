package dev.dialector.typesystem.inference

import dev.dialector.typesystem.lattice.SimpleTypeLattice
import org.junit.jupiter.api.Test

class BoundSetTest {
    @Test
    fun testResolution() {
        val lattice = SimpleTypeLattice(listOf(), listOf())
        val state = InferenceState()
        val boundSet = BoundSet()
    }
}