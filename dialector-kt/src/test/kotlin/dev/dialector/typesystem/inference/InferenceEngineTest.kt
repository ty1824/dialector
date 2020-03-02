package dev.dialector.typesystem.inference

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.reflect.KClass

class InferenceEngineTest {
    private val booleanType = object : IdentityType("boolean") {}
    private val stringType = object : IdentityType("string") {}

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
}