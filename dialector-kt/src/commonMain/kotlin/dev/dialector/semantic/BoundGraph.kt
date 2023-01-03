package dev.dialector.semantic

import dev.dialector.semantic.type.IdentityType
import dev.dialector.semantic.type.Type
import dev.dialector.util.DataGraph
import dev.dialector.semantic.type.inference.new.InferenceOrigin

public object InferredTopType : IdentityType("InferredTop")

public object InferredBottomType : IdentityType("InferredBottom")

public data class InferredGreatestLowerBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredUnion<${types.joinToString(" | ")}>"
}

public data class InferredLeastUpperBound(val types: List<Type>) : Type {
    override fun getComponents(): Sequence<Type> {
        return types.asSequence()
    }

    override fun toString(): String = "InferredIntersection<${types.joinToString(" & ")}>"
}

public interface BoundCreator {
    public infix fun TypeVariable.exactBound(type: Type)
    public infix fun TypeVariable.upperBound(type: Type)
    public infix fun TypeVariable.lowerBound(type: Type)
}

public object SimpleBoundCreator : BoundCreator {
    override fun TypeVariable.exactBound(type: Type) {
        BaseBound(TypeRelation.EQUIVALENT, this, type)
    }

    override fun TypeVariable.upperBound(type: Type) {
        BaseBound(TypeRelation.SUPERTYPE, this, type)
    }

    override fun TypeVariable.lowerBound(type: Type) {
        BaseBound(TypeRelation.SUBTYPE, this, type)
    }
}

public sealed class BoundGraphNode {
    public abstract val type: Type
    public abstract fun isProper(): Boolean
}

public class TypeNode(override val type: Type) : BoundGraphNode() {
    override fun isProper(): Boolean = true
}

public class VariableNode(public val variable: TypeVariable) : BoundGraphNode() {
    public val equivalentTo: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    public val upperBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    public val lowerBounds: MutableSet<Pair<BoundGraphNode, Bound>> = mutableSetOf()
    /**
     * Determines whether the solution for this variable should be the greatest lower bound rather than the least upper bound.
     */
    public var pushDown: Boolean = false

    override val type: Type
        get() = variable

    public fun getDependencies(): Sequence<VariableNode> =
        if (pushDown) {
            lowerBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } else {
            upperBounds.asSequence().filter {
                !it.first.isProper()
            }.map {
                it.first as VariableNode
            }
        } + equivalentTo.asSequence().filter {
            !it.first.isProper()
        }.map {
            it.first as VariableNode
        }

    override fun isProper(): Boolean = equivalentTo.any { it.first is TypeNode }
}

public class BoundGraph {
    private val graph: DataGraph<BoundGraphNode, Bound> = DataGraph()
    private val typeNodes: MutableMap<Type, TypeNode> = mutableMapOf()
    public val variableNodes: MutableMap<TypeVariable, VariableNode> = mutableMapOf()

    public fun addVariable(variable: TypeVariable, pushDown: Boolean = false) {
        val data = nodeFor(variable)
        data.pushDown = pushDown
        graph.addNode(data)
    }

    public fun addBound(bound: Bound, origin: InferenceOrigin) {
        val lower = nodeFor(bound.lowerType())
        val upper = nodeFor(bound.upperType())
        graph.addEdge(bound, lower, upper, true)
        if (bound.relation != TypeRelation.EQUIVALENT) {
            if (lower is VariableNode) lower.upperBounds += upper to bound
            if (upper is VariableNode) upper.lowerBounds += lower to bound
        } else {
            if (lower is VariableNode) lower.equivalentTo += upper to bound
            if (upper is VariableNode) upper.equivalentTo += lower to bound
        }
    }

    public fun setPushDown(variable: TypeVariable, pushDown: Boolean) {
        nodeFor(variable).pushDown = pushDown
    }

    public fun getAllVariables(): Sequence<VariableNode> = variableNodes.values.asSequence()

    public fun isResolved(variable: TypeVariable): Boolean = nodeFor(variable).isProper()

    public fun getUnresolvedVariables(): Sequence<VariableNode> =
        variableNodes.values.asSequence().filter { !it.isProper() }

    public fun getBounds(variable: TypeVariable): Sequence<Bound> = graph.getEdges(nodeFor(variable)).map { it.data }

    public fun getDependencyGroups(): Sequence<Set<VariableNode>> = sequence {
        val simpleNodes = getUnresolvedVariables().filter {
            it.getDependencies().count() == 0
        }.toList()

        // Return nodes with no dependencies first
        if (simpleNodes.isNotEmpty()) {
            for (node in simpleNodes) {
                yield(setOf(node))
            }
        }

        // Form dependency groups
    }

    private fun nodeFor(type: Type): BoundGraphNode =
        if (type is TypeVariable) {
            variableNodes.computeIfAbsent(type) { VariableNode(type) }
        } else {
            typeNodes.computeIfAbsent(type) { TypeNode(type) }
        }

    private fun nodeFor(variable: TypeVariable): VariableNode =
        variableNodes.computeIfAbsent(variable) { VariableNode(variable) }

    override fun toString(): String {
        val builder = StringBuilder("Bounds:\n")

        this.graph.getAllEdges().groupBy { it.data.lowerType() }.forEach { (_, value) ->
            value.forEach {
                if (it.data.variable == it.data.lowerType()) {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation} ${it.data.upperType()}\n")
                } else {
                    builder.append("\t${it.data.lowerType()} ${it.data.relation.opposite()} ${it.data.upperType()}\n")
                }
            }
        }

        return builder.toString()
    }
}