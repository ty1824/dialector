package dev.dialector.glottony.typesystem

import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.BinaryExpression
import dev.dialector.glottony.ast.BinaryOperators
import dev.dialector.glottony.ast.BlockExpression
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.FunctionType
import dev.dialector.glottony.ast.GType
import dev.dialector.glottony.ast.IntegerLiteral
import dev.dialector.glottony.ast.IntegerType
import dev.dialector.glottony.ast.LambdaLiteral
import dev.dialector.glottony.ast.MemberAccessExpression
import dev.dialector.glottony.ast.NumberLiteral
import dev.dialector.glottony.ast.NumberType
import dev.dialector.glottony.ast.Parameter
import dev.dialector.glottony.ast.ReturnStatement
import dev.dialector.glottony.ast.StringLiteral
import dev.dialector.glottony.ast.StringType
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.model.Node
import dev.dialector.model.getAllDescendants
import dev.dialector.model.given
import dev.dialector.typesystem.IdentityType
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.inference.new.BaseInferenceSystem
import dev.dialector.typesystem.inference.new.InferenceResult
import dev.dialector.typesystem.inference.new.InferredBottomType
import dev.dialector.typesystem.inference.new.InferredLeastUpperBound
import dev.dialector.typesystem.inference.new.InferredGreatestLowerBound
import dev.dialector.typesystem.inference.new.InferredTopType
import dev.dialector.typesystem.inference.new.leftReduction
import dev.dialector.typesystem.inference.new.redundantElimination
import dev.dialector.typesystem.inference.new.rightReduction
import dev.dialector.typesystem.integration.BaseProgramInferenceContext
import dev.dialector.typesystem.integration.InferenceRule
import dev.dialector.typesystem.integration.infer
import dev.dialector.typesystem.lattice.OrType
import dev.dialector.typesystem.lattice.SimpleTypeLattice
import dev.dialector.typesystem.lattice.hasSupertype
import dev.dialector.typesystem.lattice.hasSupertypes
import dev.dialector.typesystem.typeClass
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    val lattice: SimpleTypeLattice = SimpleTypeLattice(listOf(
        typeClass<Type>() hasSupertype AnyType,
        typeClass<IntType>() hasSupertype NumType,
        typeClass<InferredGreatestLowerBound>() hasSupertypes {
            it.getComponents()
        }
    ), listOf())
    val reductionRules = listOf(redundantElimination, leftReduction, rightReduction)
    val inferenceRules: List<InferenceRule<*>> = listOf(
        given<StringLiteral>() infer {
            constraint { typeOf(it) equal StrType }
        },
        given<NumberLiteral>() infer {
            constraint { typeOf(it) equal NumType }
        },
        given<IntegerLiteral>() infer {
            constraint { typeOf(it) equal IntType }
        },
        given<BinaryExpression> { it.operator == BinaryOperators.Plus } infer {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype OrType(setOf(NumType, StrType)) }
            constraint { typeOf(it.right) subtype OrType(setOf(NumType, StrType)) }
        },
        given<BinaryExpression> { it.operator == BinaryOperators.Minus } infer {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        given<BinaryExpression> { it.operator == BinaryOperators.Multiply } infer {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        given<BinaryExpression> { it.operator == BinaryOperators.Divide } infer {
            constraint { typeOf(it) supertype typeOf(it.left) }
            constraint { typeOf(it) supertype typeOf(it.right) }
            constraint { typeOf(it.left) subtype NumType }
            constraint { typeOf(it.right) subtype NumType }
        },
        given<BlockExpression>() infer {
            constraint { typeOf(it) equal typeOf(it.block.statements.last { statement -> statement is ReturnStatement }) }
        },
        given<ValStatement>() infer {
            val type = it.type
            if (type != null) {
                val asType = type.asType()
                constraint { typeOf(it) equal asType }
                constraint { typeOf(it.expression) subtype asType }
            } else {
                constraint { typeOf(it) equal typeOf(it.expression) }
            }
        },
        given<ReturnStatement>() infer {
            constraint { typeOf(it) equal typeOf(it.expression) }
        },
        given<FunctionDeclaration>() infer {
            constraint { typeOf(it.body) subtype it.type.asType() }
        },
        given<Parameter>() infer {
            val type = it.type
            if (type != null) {
                constraint { typeOf(it) equal type.asType() }
            }
        },
        given<LambdaLiteral>() infer {
            val parameterTypes = it.parameters.map {
                parameter -> ParameterType(typeOf(parameter), parameter.name)
            }
            constraint { typeOf(it) equal FunType(parameterTypes, typeOf(it.body)) }
        }
    )

    private val rootInferenceResults: MutableMap<GlottonyRoot, Map<Node, Type>> = mutableMapOf()

    suspend fun requestInferenceResult(root: GlottonyRoot): Map<Node, Type> {
        return rootInferenceResults[root] ?: suspendCoroutine { continuation ->
            continuation.resume(inferTypes(root.rootNode))
        }

    }

    internal fun inferTypes(node: Node): Map<Node, Type> {
        val system = BaseInferenceSystem()
        val context = BaseProgramInferenceContext(system::createVariable, system::registerConstraint)

        context.apply {
            for (currentNode in node.getAllDescendants(true)) {
                for (rule in inferenceRules) {
                    rule(this, currentNode)
                }
            }
        }
        context.nodeVariables.forEach {
            println("Node: ${it.key} bound to ${it.value}")
        }
        println("Starting solver")
        val inferenceSolution = system.solve(reductionRules)
        println("Solver completed")
        return context.nodeVariables.mapValues { (_, value) ->
            // Break intersections down into their components
            lattice.greatestCommonSubtype(inferenceSolution[value]!!.map {
                when (it) {
                    is InferredLeastUpperBound -> lattice.leastCommonSupertype(it.types)
                    is InferredGreatestLowerBound -> lattice.greatestCommonSubtype(it.types)
                    is InferredTopType -> lattice.topType
                    is InferredBottomType -> lattice.bottomType
                    else -> it
                }
            }.toSet())
        }
    }
}

object AnyType : IdentityType("any")
object IntType : IdentityType("integer")
object NumType : IdentityType("number")
object StrType : IdentityType("string")

data class ParameterType(val type: Type, val hint: String? = null)

data class FunType(val parameterTypes: List<ParameterType>, val returnType: Type) : Type {
    override fun getComponents(): Sequence<Type> = sequence {
        yieldAll(parameterTypes.map { it.type })
        yield(returnType)
    }
}

fun GType.asType(): Type = when (this) {
    is IntegerType -> IntType
    is NumberType -> NumType
    is StringType -> StrType
    is FunctionType -> FunType(this.parameterTypes.map { ParameterType(it.type.asType(), it.name)}, this.returnType.asType())
    else -> throw RuntimeException("Could not derive typesystem type for node: $this")
}
