package dev.dialector.semantic

import dev.dialector.util.DataGraph
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference

public sealed class ScopeGraphNode
public class ScopeNode(public val scope: ScopeVariable) : ScopeGraphNode()
public class ReferenceNode(public val reference: NodeReference<*>): ScopeGraphNode()
public class ElementNode(public val node: Node): ScopeGraphNode()

public sealed class ScopeGraphEdge

public class Declaring(public val name: String, public val namespace: Namespace?): ScopeGraphEdge()
public class Inheriting(public val label: String): ScopeGraphEdge()
public class Referencing(public val namespace: Namespace?): ScopeGraphEdge()
public class ReferencingType(public val namespace: Namespace, public val target: NodeReference<out Node>): ScopeGraphEdge()

public class ScopeGraph {
    private val graph: DataGraph<ScopeGraphNode, ScopeGraphEdge> = DataGraph()
    private val scopeNodes: MutableMap<ScopeVariable, ScopeNode> = mutableMapOf()
    private val elementNodes: MutableMap<Node, ElementNode> = mutableMapOf()
    private val referenceNodes: MutableMap<NodeReference<*>, ReferenceNode> = mutableMapOf()

    private fun ScopeVariable.scopeNode(): ScopeNode = scopeNodes.computeIfAbsent(this, ::ScopeNode)
    private fun Node.elementNode(): ElementNode = elementNodes.computeIfAbsent(this, ::ElementNode)
    private fun NodeReference<*>.referenceNode(): ReferenceNode = referenceNodes.computeIfAbsent(this, ::ReferenceNode)

    public fun inherit(scope: ScopeVariable, inheritFrom: ScopeVariable, label: String) {
        graph.addEdge(Inheriting(label), scope.scopeNode(), inheritFrom.scopeNode())
    }

    public fun declare(scope: ScopeVariable, element: Node, name: String, namespace: Namespace?) {
        graph.addEdge(Declaring(name, namespace), scope.scopeNode(), element.elementNode())
    }

    public fun reference(reference: NodeReference<*>, scope: ScopeVariable, namespace: Namespace?) {
        graph.addEdge(Referencing(namespace), reference.referenceNode(), scope.scopeNode())
    }

    public fun getDeclarations(scope: ScopeVariable, namespace: Namespace? = null): Sequence<Pair<String, Node>> {
        val scopeNode = graph.getNode(scope.scopeNode())
        return if (scopeNode == null) {
            sequenceOf()
        } else {
            scopeNode.getEdgesOfType<Declaring>().filter {
                namespace == null || it.data.namespace == namespace
            }.map { it.data.name to (it.target.data as ElementNode).node }
        }
    }

    public fun getAllDeclarations(scope: ScopeVariable, namespace: Namespace? = null): Sequence<Pair<String, Node>> = sequence {
        val immediateDeclarations = getDeclarations(scope, namespace)
        yieldAll(immediateDeclarations)

        graph.getNode(scope.scopeNode())?.getEdgesOfType<Inheriting>()?.forEach {
            yieldAll(getAllDeclarations((it.target.data as ScopeNode).scope, namespace))
        }
    }

    public fun getTargetNode(reference: NodeReference<*>): Node? {
        val refNode = graph.getNode(reference.referenceNode())
        return if (refNode == null) {
            null
        } else {
            val referencingEdge = refNode.getEdgesOfType<Referencing>().first()
            getAllDeclarations((referencingEdge.target.data as ScopeNode).scope, referencingEdge.data.namespace)
                .first { (name, element) -> reference.targetIdentifier == name}
                .second
        }
    }

    public inline fun <reified T : Node> getTarget(reference: NodeReference<T>): T? =
        getTargetNode(reference) as? T
}