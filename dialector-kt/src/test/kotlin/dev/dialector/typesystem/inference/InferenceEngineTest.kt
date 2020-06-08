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
        val system = BaseInferenceSystem(mockk<TypeLattice>())

        val var1 = system.varTerm()
        val var2 = system.varTerm()
        val var3 = system.varTerm()

        val boolTerm = system.asTerm(booleanType)
        val stringTerm = system.asTerm(stringType)

        system.equals(var1, boolTerm)
        system.equals(var2, var1)
        system.equals(var3, stringTerm)

        val result = DefaultInferenceSolver.solve(system)
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
        val system = BaseInferenceSystem(lattice)

        val var1 = system.varTerm()
        val var2 = system.varTerm()
        val var3 = system.varTerm()

        val boolTerm = system.asTerm(booleanType)
        val stringTerm = system.asTerm(stringType)

        assertEquals(system.equals(var1, boolTerm), InferenceResult.Ok)
        assertEquals(system.equals(var2, var1), InferenceResult.Ok)
        assertEquals(system.equals(var2, var3), InferenceResult.Ok)
        val equals = system.equals(var3, stringTerm)
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

        val system = BaseInferenceSystem(lattice)

        val var1 = system.varTerm()
        val var2 = system.varTerm()

        val integerTerm = system.asTerm(integerType)

        system.equals(var1, integerTerm)
        system.subtype(var2, var1)

        val result = DefaultInferenceSolver.solve(system)

        assertAll("variable results",
            { assertEquals(integerType, (result[var1] as TypeResult.Success).type) },
            { assertEquals(integerType, (result[var2] as TypeResult.Success).type) })
    }
}