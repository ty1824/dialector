package dev.dialector.semantic.type.inference.new

import assertAll
import dev.dialector.semantic.type.IdentityType
import kotlin.test.Test
import kotlin.test.assertEquals

class InferenceEngineTest {
    private val booleanType = object : IdentityType("boolean") {}
    private val stringType = object : IdentityType("string") {}
    private val integerType = object : IdentityType("integer") {}
    private val numberType = object : IdentityType("number") {}
    private val anyType = object : IdentityType("any") {}

    // private infix fun Map<VariableTerm, TypeResult>.typeFor(term: VariableTerm): Type = (this[term] as TypeResult.Success).type

    object FromTest : InferenceOrigin

    @Test
    fun `simple equality inference`() {
        val system = BaseInferenceConstraintSystem()
        val context = BaseInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            val var1 = typeVar()
            val var2 = typeVar()
            val var3 = typeVar()

            constraint { var1 equal booleanType }
            constraint { var2 equal var1 }
            constraint { var3 equal stringType }

            val result = system.solve(listOf(redundantElimination, leftReduction, rightReduction))
            assertAll(
                "variable results",
                { assertEquals(listOf(booleanType), result[var1]) },
                { assertEquals(listOf(booleanType), result[var2]) },
                { assertEquals(listOf(stringType), result[var3]) },
            )
        }
    }

//    @Test
//    fun `exception with too many bound types`() {
//        val lattice = mockk<TypeLattice>()
//        every { lattice.isEquivalent(any<Type>(), any<Type>()) } returns false
//        val system = BaseInferenceSystem(lattice)
//
//        val var1 = system.varTerm()
//        val var2 = system.varTerm()
//        val var3 = system.varTerm()
//
//        val boolTerm = system.asTerm(booleanType)
//        val stringTerm = system.asTerm(stringType)
//
//        assertEquals(system.equals(var1, boolTerm), InferenceResult.Ok)
//        assertEquals(system.equals(var2, var1), InferenceResult.Ok)
//        assertEquals(system.equals(var2, var3), InferenceResult.Ok)
//        val equals = system.equals(var3, stringTerm)
//        assertEquals(equals, InferenceResult.UnifyError(var3, stringTerm))
//    }

    // TODO: @Test
    fun `simple inequality inference`() {
        val system = BaseInferenceConstraintSystem()
        val context = BaseInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            val var1 = typeVar()
            val var2 = typeVar()

            constraint { var1 equal integerType }
            constraint { var2 subtype var1 }

            val result = system.solve(listOf(redundantElimination, leftReduction, rightReduction))

            assertAll(
                "variable results",
                { assertEquals(listOf(integerType), result[var1]) },
                { assertEquals(listOf(integerType), result[var2]) },
            )
        }
    }

    // TODO: @Test
    fun `upper and lower bounded inference`() {
        val system = BaseInferenceConstraintSystem()
        val context = BaseInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            val integerVar = typeVar()
            val inferredNumberVar = typeVar()
            val numberVar = typeVar()
            val inferredAnyVar = typeVar()
            val anyVar = typeVar()

            // integer :< X :< number :< Y :< any
            constraint { integerVar equal integerType }
            constraint { numberVar equal numberType }
            constraint { anyVar equal anyType }
            constraint { inferredNumberVar supertype integerVar }
            constraint { inferredNumberVar subtype numberVar }
            constraint { inferredAnyVar supertype numberVar }
            constraint { inferredAnyVar subtype anyVar }

            val result = system.solve(listOf(redundantElimination, leftReduction, rightReduction))

            assertAll(
                "variable results",
                { assertEquals(listOf(integerType), result[integerVar]) },
                { assertEquals(listOf(numberType), result[numberVar]) },
                { assertEquals(listOf(anyType), result[anyVar]) },
                { assertEquals(listOf(numberType), result[inferredNumberVar]) },
                { assertEquals(listOf(anyType), result[inferredAnyVar]) },
            )
        }
    }

    //  TODO: @Test
    fun `codependent bounded inference`() {
        val system = BaseInferenceConstraintSystem()
        val context = BaseInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            val integerVar = typeVar()
            val inferredNumberVar = typeVar() // X
            val numberVar = typeVar()
            val inferredAnyVar = typeVar() // Y
            val anyVar = typeVar()

            constraint { integerVar equal integerType }
            constraint { numberVar equal numberType }
            constraint { anyVar equal anyType }

            // integer < X < number
            // X < Y < any
            constraint { inferredNumberVar supertype integerVar }
            constraint { inferredNumberVar subtype numberVar }
            constraint { inferredAnyVar supertype inferredNumberVar }
            constraint { inferredAnyVar subtype anyVar }

            println(system.toString())

            val result = system.solve(listOf(redundantElimination, leftReduction, rightReduction))

            assertAll(
                "variable results",
                { assertEquals(listOf(integerType), result[integerVar]) },
                { assertEquals(listOf(numberType), result[numberVar]) },
                { assertEquals(listOf(anyType), result[anyVar]) },
                { assertEquals(listOf(numberType), result[inferredNumberVar]) },
                { assertEquals(listOf(anyType), result[inferredAnyVar]) },
            )
        }
    }
}
