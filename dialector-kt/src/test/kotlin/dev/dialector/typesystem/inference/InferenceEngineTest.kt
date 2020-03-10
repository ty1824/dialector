package dev.dialector.typesystem.inference

import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.SimpleTypeLattice
import dev.dialector.typesystem.lattice.TypeLattice
import dev.dialector.typesystem.lattice.hasSupertypes
import dev.dialector.typesystem.type
import dev.dialector.typesystem.typeClass
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class InferenceEngineTest {
    private val booleanType = object : IdentityType("boolean") {}
    private val stringType = object : IdentityType("string") {}
    private val integerType = object : IdentityType("integer") {}
    private val numberType = object : IdentityType("number") {}
    private val anyType = object : IdentityType("any") {}

    @Test
    fun `simple equality inference`() {
        val context = BaseInferenceContext(mockk<TypeLattice>())

        val var1 = context.varTerm()
        val var2 = context.varTerm()
        val var3 = context.varTerm()

        val boolTerm = context.asTerm(booleanType)
        val stringTerm = context.asTerm(stringType)

        context.equals(var1, boolTerm)
        context.equals(var2, var1)
        context.equals(var3, stringTerm)

        val result = DefaultInferenceSolver.solve(context)
        assertAll("variable results",
                { assertEquals(booleanType, (result[var1] as TypeResult.Success).type) },
                { assertEquals(booleanType, (result[var2] as TypeResult.Success).type) },
                { assertEquals(stringType, (result[var3] as TypeResult.Success).type) }
        )
    }

    @Test
    fun `exception with too many bound types`() {
        val lattice = mockk<TypeLattice>()
        every { lattice.isEquivalent(any<Type>(), any<Type>()) } returns false
        val context = BaseInferenceContext(lattice)

        val var1 = context.varTerm()
        val var2 = context.varTerm()
        val var3 = context.varTerm()

        val boolTerm = context.asTerm(booleanType)
        val stringTerm = context.asTerm(stringType)

        assertEquals(context.equals(var1, boolTerm), InferenceResult.Ok)
        assertEquals(context.equals(var2, var1), InferenceResult.Ok)
        assertEquals(context.equals(var2, var3), InferenceResult.Ok)
        val equals = context.equals(var3, stringTerm)
        assertEquals(equals, InferenceResult.UnifyError(var3, stringTerm))
    }

    @Test
    fun `simple inequality inference`() {
        val lattice = SimpleTypeLattice(listOf(
            // integer < number
            type(integerType) hasSupertypes sequenceOf(numberType),
            // ~all types~ < any
            typeClass(Type::class) hasSupertypes sequenceOf(anyType)
        ), listOf())

        val context = BaseInferenceContext(lattice)

        val var1 = context.varTerm()
        val var2 = context.varTerm()

        val integerTerm = context.asTerm(integerType)
    }
}