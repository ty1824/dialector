package dev.dialector.glottony.typesystem

import dev.dialector.glottony.ast.BinaryExpression
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.IntegerLiteral
import dev.dialector.glottony.ast.NumberLiteral
import dev.dialector.glottony.ast.StringLiteral
import dev.dialector.glottony.ast.numberType
import dev.dialector.model.Node
import dev.dialector.model.getAllDescendants
import dev.dialector.model.nodeClassClause
import dev.dialector.model.nodeClause
import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.inference.new.BaseInferenceSystem
import dev.dialector.typesystem.inference.new.InferenceResult
import dev.dialector.typesystem.inference.new.leftReduction
import dev.dialector.typesystem.inference.new.redundantElimination
import dev.dialector.typesystem.inference.new.rightReduction
import dev.dialector.typesystem.integration.BaseProgramInferenceContext
import dev.dialector.typesystem.integration.InferenceRule
import dev.dialector.typesystem.integration.infers
import dev.dialector.typesystem.lattice.OrType
import dev.dialector.typesystem.lattice.SimpleTypeLattice
import dev.dialector.typesystem.lattice.hasSupertype
import dev.dialector.typesystem.typeClass

/**
 * Glottony's type system is hierarchically organized. Given that most of the program will remain constant in normal
 * IDE use cases (i.e. writing code in a single file), we want to ensure that we make minimal changes to types that
 * have already been resolved. To achieve this goal, the typesystem establishes rules about the possible graph of
 * typesystem interdependencies:
 *
 * 1) All typed nodes visible beyond a file boundary are statically typed.
 *      a) Functions
 *      b) Fields
 * 2)
 */
class GlottonyTypesystem {
    private val lastInferenceResult: InferenceResult? = null
}

object AnyType : IdentityType("any")
object IntType : IdentityType("integer")
object NumType : IdentityType("number")
object StrType : IdentityType("string")

class GlottonyTypeInferenceContext {
    val lattice: SimpleTypeLattice = SimpleTypeLattice(listOf(
        typeClass<Type>() hasSupertype AnyType,
        typeClass<IntType>() hasSupertype NumType
    ), listOf())
    val inferenceRules: List<InferenceRule<*>> = listOf(
        nodeClassClause(StringLiteral::class) infers {
            constraint { typeOf(it) equal StrType }
        },
        nodeClassClause(NumberLiteral::class) infers {
            constraint { typeOf(it) equal NumType }
        },
        nodeClassClause(IntegerLiteral::class) infers {
            constraint { typeOf(it) equal IntType }
        },
        nodeClause<BinaryExpression> { it.operator == BinaryOperators.Plus } infers {
            constraint { typeOf(it) subtype NumType }
            constraint { typeOf(it.left) subtype OrType(setOf(NumType, StrType)) }
            constraint { typeOf(it.right) subtype OrType(setOf(NumType, StrType)) }
        },
        nodeClause<BinaryExpression> { it.operator == BinaryOperators.Minus } infers {
            constraint { typeOf(it) subtype NumType }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        nodeClause<BinaryExpression> { it.operator == BinaryOperators.Multiply } infers {
            constraint { typeOf(it) subtype NumType }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        nodeClause<BinaryExpression> { it.operator == BinaryOperators.Divide } infers {
            constraint { typeOf(it) subtype NumType }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        }
    )

    fun inferTypes(program: Node): Map<Node, Type> {
        val system = BaseInferenceSystem()
        val context = BaseProgramInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            for (node in program.getAllDescendants(true)) {
                for (rule in inferenceRules) {
                    rule(this, node)
                }
            }
        }

        val inferenceSolution = system.solve(listOf(redundantElimination, leftReduction, rightReduction))

        return context.nodeVariables.mapValues { (_, value) ->
            lattice.leastCommonSupertype(inferenceSolution[value]!!.toSet())
        }
    }
}

