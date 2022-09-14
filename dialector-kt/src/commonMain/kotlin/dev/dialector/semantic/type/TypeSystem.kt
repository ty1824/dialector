package dev.dialector.semantic.type

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import dev.dialector.semantic.Query
import dev.dialector.semantic.SemanticAnalysisContext
import dev.dialector.semantic.SemanticDataDefinition
import dev.dialector.semantic.SemanticSystem
import dev.dialector.semantic.SemanticSystemDefinition
import dev.dialector.semantic.type.inference.IncorporationRule
import dev.dialector.semantic.type.inference.new.InferenceConstraint
import dev.dialector.semantic.type.inference.new.InferenceVariable
import dev.dialector.semantic.type.inference.new.ReductionRule
import dev.dialector.semantic.type.inference.new.RelationalConstraint
import dev.dialector.semantic.type.inference.new.VariableConstraint
import dev.dialector.semantic.type.integration.InferenceRule

object TypeSystemDefinition : SemanticSystemDefinition<TypeSystem>() {
    val ReferencedNode = SemanticDataDef<NodeReference<*>, Node> { system, argument -> TODO("") }
}

object NodeType : SemanticDataDefinition<TypeSystem, Node, List<Type>?>(TypeSystemDefinition) {
    override fun query(system: TypeSystem, argument: Node): Query<Node, List<Type>?> {
        TODO("Not yet implemented")
    }
}

sealed class TypeResult

interface TypeSystem : SemanticSystem {

    val inferenceRules: List<InferenceRule<*>>
    val reductionRules: List<ReductionRule>
    val incorporationRules: List<IncorporationRule>

    suspend fun getTypeOfNode(node: Node): TypeResult
}

/*
Type system that tracks state and can respond to changes.

On change:
    Invoke all

 */
class InferringTypeSystem(override val semantics: SemanticAnalysisContext) : TypeSystem {
    private val nodeTypes: MutableMap<Node, TypeResult> = mutableMapOf()

    override val inferenceRules: List<InferenceRule<*>> = listOf()
    override val reductionRules: List<ReductionRule> = listOf()
    override val incorporationRules: List<IncorporationRule> = listOf()


    private val variables: MutableList<InferenceVariable> = mutableListOf()
    private val constraints: MutableList<InferenceConstraint> = mutableListOf()

    private val nodeVariables: MutableMap<Node, InferenceVariable> = mutableMapOf()
    private val nodeConstraints: MutableMap<Node, MutableList<InferenceConstraint>> = mutableMapOf()

//    fun getConstraintSystemForNode(node: Node): InferenceConstraintSystem {
//        val resultVariables = nodeVariables
//        val resultConstraints = mutableListOf<InferenceConstraint>()
//
//
//    }

    override suspend fun getTypeOfNode(node: Node): TypeResult {
        TODO("Not yet implemented")
    }
}

interface TypeConstraintCreator {
    /**
     * Indicates that the variable's optimal solution should resolve using its upper bounds.
     */
    fun pullUp(variable: InferenceVariable): VariableConstraint

    /**
     * Indicates that the variable's optimal solution should resolve using its lower bounds.
     */
    fun pushDown(variable: InferenceVariable): VariableConstraint

    /**
     * Indicates that two types should be considered equivalent.
     *
     * There is a unidirectional dependency from the left-hand-side argument and the right-hand-side argument.
     */
    infix fun Type.equal(type: Type): RelationalConstraint

    /**
     * Indicates that two types should be considered equivalent.
     *
     * There is a bidirectional dependency between the two arguments.
     */
    infix fun Type.mutualEqual(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a subtype of the right-hand-side type.
     *
     * There is a unidirectional dependency from the left-hand-side argument and the right-hand-side argument.
     */
    infix fun Type.subtype(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a subtype of the right-hand-side type.
     *
     * There is a bidirectional dependency between the two arguments.
     */
    infix fun Type.mutualSubtype(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a supertype of the right-hand-side type.
     *
     * There is a unidirectional dependency from the left-hand-side argument and the right-hand-side argument.
     */
    infix fun Type.supertype(type: Type): RelationalConstraint

    /**
     * Indicates that the left-hand-side type should be a supertype of the right-hand-side type.
     *
     * There is a bidirectional dependency between the two arguments.
     */
    infix fun Type.mutualSupertype(type: Type): RelationalConstraint
}