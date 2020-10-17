package dev.dialector.typesystem.inference

/**
 * Represents a graph with data stored both on nodes and edges
 *
 * @param N The type of data stored in the graph nodes
 * @param E The type of data stored in the graph edges
 */
class DataGraph<N, E> {
    private val nodes: MutableMap<N, Node<N, E>> = mutableMapOf()
//    private val edges: MutableMap<N, Set<Edge<N, E>>> = mutableMapOf()

    fun getEdges(data: N): Sequence<Edge<N, E>> = getOrCreateNode(data).getEdges()

    fun getAllNodes(): Sequence<Node<N, E>> = nodes.values.asSequence()

    fun getAllEdges(): Sequence<Edge<N, E>> = getAllNodes().flatMap { it.getEdges() }.distinct()

    fun addNode(data: N): Node<N, E> = getOrCreateNode(data)

    fun addEdge(data: E, source: N, target: N, bidirectional: Boolean = false): Edge<N, E> {
        val sourceNode = getOrCreateNode(source)
        val targetNode = getOrCreateNode(target)
        val edge = Edge(data, sourceNode, targetNode, bidirectional)

        sourceNode.edges += edge
        if (bidirectional) targetNode.edges += edge

        return edge
    }

    fun copy(): DataGraph<N, E> {
        val newGraph = DataGraph<N, E>()
        this.nodes.forEach { newGraph.addNode(it.key) }
//        this.edges.forEach { (_, edges) -> edges.forEach { newGraph.addEdge(it.data, it.source.data, it.target.data, it.bidirectional) }}
        return newGraph
    }

    private fun getOrCreateNode(data: N): Node<N, E> = nodes.computeIfAbsent(data, { Node(it) })
}

class Node<N, E>(val data: N) {
    internal val edges: MutableSet<Edge<N, E>> = mutableSetOf()

    fun getEdges(): Sequence<Edge<N, E>> = edges.asSequence()
}

/**
 * Represents an optionally-bidirectional edge between two nodes.
 */
class Edge<N, E>(
    val data: E,
    val source: Node<N, E>,
    val target: Node<N, E>,
    val bidirectional: Boolean = false
)
