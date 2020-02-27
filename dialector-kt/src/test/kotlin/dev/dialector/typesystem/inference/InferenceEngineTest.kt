package dev.dialector.typesystem.inference

import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.lattice.DefaultTypeLattice
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class InferenceEngineTest {

    @Test
    fun inferenceSolverBasicTest() {
        val context = InferenceContext(DefaultTypeLattice(listOf(), listOf()))

        var id = 0

        val booleanType = object : IdentityType() {}
        val stringType = object : IdentityType() {}

        val var1 = VariableTerm(id++)
        val var2 = VariableTerm(id++)
        val var3 = VariableTerm(id++)

        val boolTerm = TypeTerm(id++, booleanType)
        val stringTerm = TypeTerm(id++, stringType)

        context.equality(var1, boolTerm)
        context.equality(var2, var1)
        context.equality(var3, stringTerm)

        val result = DefaultInferenceSolver.solve(context)
        assertAll("basic inference",
                { assertEquals(booleanType, (result[var1] as TypeResult).type) },
                { assertEquals(booleanType, (result[var2] as TypeResult).type) },
                { assertEquals(stringType, (result[var3] as TypeResult).type) }
        )
    }
}