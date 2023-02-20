package dev.dialector.semantic.type.inference

import assertAll
import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.SimpleTypeLattice
import dev.dialector.semantic.type.lattice.TypeLattice
import dev.dialector.semantic.type.lattice.hasSupertypes
import dev.dialector.semantic.type.type
import dev.dialector.semantic.type.typeClass
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class InferenceEngineTest {
    private val booleanType = object : IdentityType("boolean") {}
    private val stringType = object : IdentityType("string") {}
    private val integerType = object : IdentityType("integer") {}
    private val numberType = object : IdentityType("number") {}
    private val anyType = object : IdentityType("any") {}

    private infix fun Map<VariableTerm, TypeResult>.typeFor(term: VariableTerm): Type = (this[term] as TypeResult.Success).type

    // TODO: @Test
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
        assertAll(
            "variable results",
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

    // TODO: @Test
    fun `simple inequality inference`() {
        val lattice = SimpleTypeLattice(
            listOf(
                // integer < number
                type(integerType) hasSupertypes sequenceOf(numberType),
                // ~all types~ < any
                typeClass<Type>() hasSupertypes sequenceOf(anyType)
            ),
            listOf()
        )

        val system = BaseInferenceSystem(lattice)

        val var1 = system.varTerm()
        val var2 = system.varTerm()

        val integerTerm = system.asTerm(integerType)

        system.equals(var1, integerTerm)
        system.subtype(var2, var1)

        val result = DefaultInferenceSolver.solve(system)

        assertAll(
            "variable results",
            { assertEquals(integerType, result typeFor var1) },
            { assertEquals(integerType, result typeFor var2) }
        )
    }

    // TODO: @Test
    fun `upper and lower bounded inference`() {
        val lattice = SimpleTypeLattice(
            listOf(
                // integer < number
                type(integerType) hasSupertypes sequenceOf(numberType),
                // ~all types~ < any
                typeClass<Type>() hasSupertypes sequenceOf(anyType)
            ),
            listOf()
        )

        val system = BaseInferenceSystem(lattice)

        val integerVar = system.varTerm()
        val inferredNumberVar = system.varTerm()
        val numberVar = system.varTerm()
        val inferredAnyVar = system.varTerm()
        val anyVar = system.varTerm()

        val integerTerm = system.asTerm(integerType)
        val numberTerm = system.asTerm(numberType)
        val anyTerm = system.asTerm(anyType)

        // integer :< X :< number :< Y :< any
        system.equals(integerVar, integerTerm)
        system.equals(numberVar, numberTerm)
        system.equals(anyVar, anyTerm)
        system.supertype(inferredNumberVar, integerVar)
        system.subtype(inferredNumberVar, numberVar)
        system.supertype(inferredAnyVar, numberVar)
        system.subtype(inferredAnyVar, anyVar)

        val result = DefaultInferenceSolver.solve(system)

        assertAll(
            "variable results",
            { assertEquals(integerType, result typeFor integerVar) },
            { assertEquals(numberType, result typeFor numberVar) },
            { assertEquals(anyType, result typeFor anyVar) },
            { assertEquals(numberType, result typeFor inferredNumberVar) },
            { assertEquals(anyType, result typeFor inferredAnyVar) }
        )
    }

    // TODO: @Test
    fun `codependent bounded inference`() {
        val lattice = SimpleTypeLattice(
            listOf(
                // integer < number
                type(integerType) hasSupertypes sequenceOf(numberType),
                // ~all types~ < any
                typeClass<Type>() hasSupertypes sequenceOf(anyType)
            ),
            listOf()
        )

        val system = BaseInferenceSystem(lattice)

        val integerVar = system.varTerm()
        val inferredNumberVar = system.varTerm() // X
        val numberVar = system.varTerm()
        val inferredAnyVar = system.varTerm() // Y
        val anyVar = system.varTerm()

        val integerTerm = system.asTerm(integerType)
        val numberTerm = system.asTerm(numberType)
        val anyTerm = system.asTerm(anyType)

        system.equals(integerVar, integerTerm)
        system.equals(numberVar, numberTerm)
        system.equals(anyVar, anyTerm)

        // integer < X < number
        // X < Y < any
        system.supertype(inferredNumberVar, integerVar)
        system.subtype(inferredNumberVar, numberVar)
        system.supertype(inferredAnyVar, inferredNumberVar)
        system.subtype(inferredAnyVar, anyVar)

        println(system.toString())

        val result = DefaultInferenceSolver.solve(system)

        assertAll(
            "variable results",
            { assertEquals(integerType, result typeFor integerVar) },
            { assertEquals(numberType, result typeFor numberVar) },
            { assertEquals(anyType, result typeFor anyVar) },
            { assertEquals(numberType, result typeFor inferredNumberVar) },
            { assertEquals(anyType, result typeFor inferredAnyVar) }
        )
    }
}
