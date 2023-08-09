package dev.dialector.semantic

import dev.dialector.semantic.type.Type
import dev.dialector.syntax.Node

private class BaseTypeVariable(override val id: String) : TypeVariable {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "tv.$id"
}

private class BaseScopeVariable(override val name: String) : ScopeVariable {
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "sv.$name"
}

class SampleSemanticSystem {
    var typeVarId = 'a'
    var typeVarNum: Int? = null

    private val typeVariables: MutableSet<TypeVariable> = mutableSetOf()
    private val nodeTypeVariables: MutableMap<Node, TypeVariable> = mutableMapOf()

    private val scopeVariables: MutableSet<ScopeVariable> = mutableSetOf()
    private val nodeScopeVariables: MutableMap<Node, MutableMap<String, ScopeVariable>> = mutableMapOf()

    private val constraints: MutableSet<SemanticConstraint> = mutableSetOf()

    private fun getNextTypeVarId(): String {
        val id = typeVarId++
        if (typeVarId > 'z') {
            (typeVarNum?.inc()) ?: 1
            typeVarId = 'a'
        }
        return id + (typeVarNum?.toString() ?: "")
    }

    private fun createTypeVariable(node: Node? = null): TypeVariable {
        return if (node != null) {
            nodeTypeVariables.computeIfAbsent(node) {
                val variable = BaseTypeVariable(getNextTypeVarId())
                typeVariables += variable
                variable
            }
        } else {
            val variable = BaseTypeVariable(getNextTypeVarId())
            typeVariables += variable
            variable
        }
    }

    private fun createScopeVariable(node: Node, name: String): ScopeVariable =
        nodeScopeVariables.computeIfAbsent(node) { mutableMapOf() }.computeIfAbsent(name) {
            val variable = BaseScopeVariable(name)
            scopeVariables += variable
            variable
        }

    public fun getRuleContext(): SemanticRuleContext = object : SemanticRuleContext {
        override var node: Node? = null

        override fun scope(name: String): ScopeVariable {
            return if (node != null) {
                createScopeVariable(node!!, name)
            } else {
                throw RuntimeException("Can not create scope unattached to node")
            }
        }

        override fun propagateScope(scope: ScopeVariable, node: Node, type: PropagationType) {
            TODO("Not yet implemented")
        }

        override fun receiveScope(type: PropagationType): ScopeVariable {
            TODO("Not yet implemented")
        }

        override fun typeVar(): TypeVariable = createTypeVariable()

        override fun typeOf(node: Node): TypeVariable = createTypeVariable(node)

        override fun propagateTypeScope(scope: ScopeVariable, type: Type) {
            TODO("Not yet implemented")
        }

        override fun typeScope(type: Type): ScopeVariable {
            TODO("Not yet implemented")
        }

        override fun <T : ConstraintCreator> constraint(creator: T, routine: T.() -> SemanticConstraint) {
            constraints += creator.routine()
        }
    }
}
