package dev.dialector.util

/**
 * Represents a graph with data stored both on nodes and edges
 *
 * @param N The type of data stored in the graph nodes
 * @param E The type of data stored in the graph edges
 */
public class DataGraph<N, E> {
    private val nodes: MutableMap<N, Node<N, E>> = mutableMapOf()
//    private val edges: MutableMap<N, Set<Edge<N, E>>> = mutableMapOf()

    public fun getEdges(data: N): Sequence<Edge<N, E>> = getOrCreateNode(data).getEdges()

    public fun getAllNodes(): Sequence<Node<N, E>> = nodes.values.asSequence()

    public fun getAllEdges(): Sequence<Edge<N, E>> = getAllNodes().flatMap { it.getEdges() }.distinct()

    public fun addNode(data: N): Node<N, E> = getOrCreateNode(data)

    public fun getNode(data: N): Node<N, E>? = nodes[data]

    public fun addEdge(data: E, source: N, target: N, bidirectional: Boolean = false): Edge<N, E> {
        val sourceNode = getOrCreateNode(source)
        val targetNode = getOrCreateNode(target)
        val edge = Edge(data, sourceNode, targetNode, bidirectional)

        sourceNode.edges += edge
        if (bidirectional) targetNode.edges += edge

        return edge
    }

    public fun copy(): DataGraph<N, E> {
        val newGraph = DataGraph<N, E>()
        this.nodes.forEach { newGraph.addNode(it.key) }
//        this.edges.forEach { (_, edges) -> edges.forEach { newGraph.addEdge(it.data, it.source.data, it.target.data, it.bidirectional) }}
        return newGraph
    }

    private fun getOrCreateNode(data: N): Node<N, E> = nodes.computeIfAbsent(data) { Node(it) }

    public class Node<N, E>(public val data: N) {
        internal val edges: MutableSet<Edge<N, E>> = mutableSetOf()

        public fun getEdges(): Sequence<Edge<N, E>> = edges.asSequence()

        public inline fun <reified D : E> getEdgesOfType(): Sequence<Edge<N, D>> =
            getEdges().filter { it.data is D }.filterIsInstance<Edge<N, D>>()
    }

    /**
     * Represents an optionally-bidirectional edge between two nodes.
     */
    public data class Edge<N, E>(
        public val data: E,
        public val source: Node<N, E>,
        public val target: Node<N, E>,
        public val bidirectional: Boolean = false,
    )
}
