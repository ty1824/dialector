package dev.dialector.glottony.typesystem

import dev.dialector.glottony.ast.BinaryExpression
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.GType
import dev.dialector.glottony.ast.IntegerLiteral
import dev.dialector.glottony.ast.IntegerType
import dev.dialector.glottony.ast.NumberLiteral
import dev.dialector.glottony.ast.NumberType
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.StringLiteral
import dev.dialector.glottony.ast.StringType
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.model.Node
import dev.dialector.model.getAllDescendants
import dev.dialector.model.givenNode
import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.inference.new.BaseInferenceSystem
import dev.dialector.typesystem.inference.new.InferenceResult
import dev.dialector.typesystem.inference.new.InferredLeastUpperBound
import dev.dialector.typesystem.inference.new.InferredGreatestLowerBound
import dev.dialector.typesystem.inference.new.leftReduction
import dev.dialector.typesystem.inference.new.redundantElimination
import dev.dialector.typesystem.inference.new.rightReduction
import dev.dialector.typesystem.integration.BaseProgramInferenceContext
import dev.dialector.typesystem.integration.InferenceRule
import dev.dialector.typesystem.integration.infers
import dev.dialector.typesystem.lattice.OrType
import dev.dialector.typesystem.lattice.SimpleTypeLattice
import dev.dialector.typesystem.lattice.hasSupertype
import dev.dialector.typesystem.lattice.hasSupertypes
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

fun GType.asType(): Type = when (this) {
    is IntegerType -> IntType
    is NumberType -> NumType
    is StringType -> StrType
    else -> throw RuntimeException("Could not derive typesystem type for node: $this")
}

class GlottonyTypesystemContext {
    val lattice: SimpleTypeLattice = SimpleTypeLattice(listOf(
        typeClass<Type>() hasSupertype AnyType,
        typeClass<IntType>() hasSupertype NumType,
        typeClass<InferredGreatestLowerBound>() hasSupertypes {
            it.getComponents()
        }
    ), listOf())
    val reductionRules = listOf(redundantElimination, leftReduction, rightReduction)
    val inferenceRules: List<InferenceRule<*>> = listOf(
        givenNode<StringLiteral>() infers {
            constraint { typeOf(it) equal StrType }
        },
        givenNode<NumberLiteral>() infers {
            constraint { typeOf(it) equal NumType }
        },
        givenNode<IntegerLiteral>() infers {
            constraint { typeOf(it) equal IntType }
        },
        givenNode<BinaryExpression> { it.operator == BinaryOperators.Plus } infers {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype OrType(setOf(NumType, StrType)) }
            constraint { typeOf(it.right) subtype OrType(setOf(NumType, StrType)) }
        },
        givenNode<BinaryExpression> { it.operator == BinaryOperators.Minus } infers {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        givenNode<BinaryExpression> { it.operator == BinaryOperators.Multiply } infers {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        givenNode<BinaryExpression> { it.operator == BinaryOperators.Divide } infers {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        givenNode<BlockExpression>() infers {
            constraint { typeOf(it) equal typeOf(it.block.statements.last { statement -> statement is ReturnStatement }) }
        },
        givenNode<ValStatement>() infers {
            val type = it.type
            if (type != null) {
                val asType = type.asType()
                constraint { typeOf(it) equal asType }
                constraint { typeOf(it.expression) subtype asType }
            } else {
                constraint { typeOf(it) equal typeOf(it.expression) }
            }
        },
        givenNode<ReturnStatement>() infers {
            constraint { typeOf(it) equal typeOf(it.expression) }
        },
        givenNode<FunctionDeclaration>() infers {
            constraint { typeOf(it.body) subtype it.type.asType() }
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
        context.nodeVariables.forEach {
            println("Node: ${it.key} bound to ${it.value}")
        }

        val inferenceSolution = system.solve(reductionRules)

        return context.nodeVariables.mapValues { (_, value) ->
            // Break intersections down into their components
            lattice.greatestCommonSubtype(inferenceSolution[value]!!.map {
                when (it) {
                    is InferredLeastUpperBound -> lattice.leastCommonSupertype(it.types)
                    is InferredGreatestLowerBound -> lattice.greatestCommonSubtype(it.types)
                    else -> it
                }
            }.toSet())
        }
    }
}

