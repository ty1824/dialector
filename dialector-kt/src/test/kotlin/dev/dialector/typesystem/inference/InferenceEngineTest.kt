package dev.dialector.typesystem.inference

import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.lattice.TypeLattice
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class InferenceEngineTest {
    private val booleanType = object : IdentityType("boolean") {}
    private val stringType = object : IdentityType("string") {}

    @Test
    fun `simple equality inference`() {
        val context = InferenceContext(mockk<TypeLattice>())

        val var1 = context.varTerm()
        val var2 = context.varTerm()
        val var3 = context.varTerm()

        val boolTerm = context.typeTerm(booleanType)
        val stringTerm = context.typeTerm(stringType)

        context.equality(var1, boolTerm)
        context.equality(var2, var1)
        context.equality(var3, stringTerm)

        val result = DefaultInferenceSolver.solve(context)
        assertAll("variable results",
                { assertEquals(booleanType, (result[var1] as TypeResult).type) },
                { assertEquals(booleanType, (result[var2] as TypeResult).type) },
                { assertEquals(stringType, (result[var3] as TypeResult).type) }
        )
    }

    @Test
    fun `exception with too many bound types`() {
        val context = InferenceContext(mockk<TypeLattice>())

        val var1 = context.varTerm()
        val var2 = context.varTerm()
        val var3 = context.varTerm()

        val boolTerm = context.typeTerm(booleanType)
        val stringTerm = context.typeTerm(stringType)

        context.equality(var1, boolTerm)
        context.equality(var2, var1)
        context.equality(var2, var3)
        context.equality(var3, stringTerm)

        val result = DefaultInferenceSolver.solve(context)
        println((result[var1] as ErrorResult).reason)
        assertAll("variable results",
                { assertNotNull((result[var1] as ErrorResult)) },
                { assertNotNull((result[var2] as ErrorResult)) },
                { assertNotNull((result[var3] as ErrorResult)) }
        )
    }
}