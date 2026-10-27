package dev.dialector.cst

/**
 * Sentinel rawLength indicating a missing/synthesized recovery node or token.
 */
internal const val MISSING_RAW_LENGTH: Int = -1

/**
 * Sentinel rawLength indicating a composite node that has been started via [ConcreteSyntaxBuilder.startNode]
 * but not yet finalized via [ConcreteSyntaxBuilder.finishNode].
 */
internal const val UNFINISHED_RAW_LENGTH: Int = -2

/**
 * A builder for constructing [ConcreteSyntaxTree] instances.
 */
class ConcreteSyntaxBuilder {
    private val nodeKinds = mutableListOf<Int>()
    private val nodeParents = mutableListOf<Int>()
    private val nodeStartOffsets = mutableListOf<Int>()
    // -1 = missing, -2 = unfinished
    private val nodeRawLengths = mutableListOf<Int>()
    private val nodeChildren = mutableListOf<MutableList<Int>>()
    private val nodeChildSlots = mutableListOf<MutableList<Short>>()
    private val diagnostics = mutableListOf<CstDiagnostic>()

    /**
     * Allocates a new composite node with the given [kind] and optional initial [startOffset].
     * If [startOffset] is left as -1, [finishNode] will automatically derive it from the first child.
     */
    fun startNode(kind: Int, startOffset: Int = -1): Int {
        require(kind >= 0) { "Composite node kind must be >= 0 (got $kind). Use addToken for leaf tokens." }
        return allocateRawNode(kind = kind, startOffset = startOffset, rawLength = UNFINISHED_RAW_LENGTH)
    }

    /**
     * Allocates an [ERROR_NODE_KIND] composite node for wrapping unexpected tokens or broken syntax.
     */
    fun startErrorNode(startOffset: Int = -1): Int =
        startNode(ERROR_NODE_KIND, startOffset)

    /**
     * Finishes a composite node. If [endOffset] is -1 (or `startOffset` was -1),
     * automatically computes the node's span from its children (supporting bottom-up
     * parsing where `parentIdx > childIdx` as well as trailing missing recovery nodes).
     */
    fun finishNode(nodeIndex: Int, endOffset: Int = -1) {
        require(nodeIndex in 0..<nodeKinds.size) { "Node index $nodeIndex out of bounds" }
        if (nodeRawLengths[nodeIndex] == MISSING_RAW_LENGTH) return // Already marked missing
        val children = nodeChildren[nodeIndex]

        for (i in children.indices) {
            val childIdx = children[i]
            if (nodeRawLengths[childIdx] == UNFINISHED_RAW_LENGTH || nodeStartOffsets[childIdx] < 0) {
                finishNode(childIdx)
            }
        }

        var start = nodeStartOffsets[nodeIndex]
        if (start < 0) {
            val firstAnchor = children.firstOrNull { nodeRawLengths[it] > 0 }
                ?: children.firstOrNull { nodeRawLengths[it] != MISSING_RAW_LENGTH }
                ?: children.firstOrNull()
            start = if (firstAnchor != null) maxOf(0, nodeStartOffsets[firstAnchor]) else 0
            nodeStartOffsets[nodeIndex] = start
        }
        val resolvedEnd = when {
            endOffset >= 0 -> endOffset
            children.isNotEmpty() -> {
                val lastAnchor = children.lastOrNull { nodeRawLengths[it] > 0 }
                    ?: children.lastOrNull { nodeRawLengths[it] != MISSING_RAW_LENGTH }
                    ?: children.last()
                val targetLen = maxOf(0, nodeRawLengths[lastAnchor])
                maxOf(start, maxOf(0, nodeStartOffsets[lastAnchor]) + targetLen)
            }
            else -> start
        }
        nodeRawLengths[nodeIndex] = maxOf(0, resolvedEnd - start)
    }

    /**
     * Convenience helper to create a composite node, attach it to [parentNode] at [slot],
     * populate its children in [block], and automatically compute its span.
     */
    inline fun node(
        kind: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        startOffset: Int = -1,
        endOffset: Int = -1,
        block: (nodeIndex: Int) -> Unit = {},
    ): Int {
        val idx = startNode(kind, startOffset)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, idx, slot)
        }
        block(idx)
        finishNode(idx, endOffset)
        return idx
    }

    /**
     * Allocates a leaf token node.
     *
     * @param schemaTokenId Either the positive schema token ID (which will be encoded as negative)
     *                      or an already-encoded negative runtime token kind.
     */
    fun addToken(
        schemaTokenId: Int,
        startOffset: Int,
        endOffset: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        isTrivia: Boolean = false,
    ): Int {
        val runtimeKind = if (schemaTokenId < 0) schemaTokenId else encodeTokenKind(schemaTokenId, isTrivia)
        val length = maxOf(0, endOffset - startOffset)
        val tokenIndex = allocateRawNode(kind = runtimeKind, startOffset = startOffset, rawLength = length)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, tokenIndex, if (isTrivia) NO_SLOT.toInt() else slot)
        }
        return tokenIndex
    }

    /**
     * Allocates a trivia leaf token (whitespace or comment) with `slot = NO_SLOT`.
     */
    fun addTrivia(
        schemaTokenId: Int,
        startOffset: Int,
        endOffset: Int,
        parentNode: Int = NULL_NODE_INDEX,
    ): Int = addToken(
        schemaTokenId = schemaTokenId,
        startOffset = startOffset,
        endOffset = endOffset,
        parentNode = parentNode,
        slot = NO_SLOT.toInt(),
        isTrivia = true,
    )

    /**
     * Synthesizes a missing leaf token (`isMissing == true`, `length == 0`) for error recovery.
     */
    fun addMissingToken(
        schemaTokenId: Int,
        offset: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        diagnosticCode: String? = null,
        diagnosticMessage: String? = null,
    ): Int {
        val runtimeKind = if (schemaTokenId < 0) schemaTokenId else encodeTokenKind(schemaTokenId, isTrivia = false)
        val tokenIndex = allocateRawNode(kind = runtimeKind, startOffset = offset, rawLength = MISSING_RAW_LENGTH)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, tokenIndex, slot)
        }
        if (diagnosticMessage != null) {
            reportDiagnostic(
                nodeIndex = tokenIndex,
                startOffset = offset,
                endOffset = offset,
                code = diagnosticCode ?: "MISSING_TOKEN",
                message = diagnosticMessage,
            )
        }
        return tokenIndex
    }

    /**
     * Synthesizes a missing composite node (`isMissing == true`, `length == 0`) for error recovery.
     */
    fun addMissingNode(
        kind: Int,
        offset: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        diagnosticCode: String? = null,
        diagnosticMessage: String? = null,
    ): Int {
        require(kind > 0) { "Composite node kind must be > 0 (got $kind)" }
        val nodeIndex = allocateRawNode(kind = kind, startOffset = offset, rawLength = MISSING_RAW_LENGTH)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, nodeIndex, slot)
        }
        if (diagnosticMessage != null) {
            reportDiagnostic(
                nodeIndex = nodeIndex,
                startOffset = offset,
                endOffset = offset,
                code = diagnosticCode ?: "MISSING_NODE",
                message = diagnosticMessage,
            )
        }
        return nodeIndex
    }

    /**
     * Attaches [childNodeIndex] as a child of [parentNodeIndex] at optional schema [slot].
     */
    fun addChild(
        parentNodeIndex: Int,
        childNodeIndex: Int,
        slot: Int = NO_SLOT.toInt(),
    ) {
        require(parentNodeIndex in 0..<nodeKinds.size) { "Parent node index $parentNodeIndex is out of range" }
        require(childNodeIndex in 0..<nodeKinds.size) { "Child node index $childNodeIndex is out of range" }
        require(parentNodeIndex != childNodeIndex) { "Node $childNodeIndex cannot be a child of itself" }
        require(nodeParents[childNodeIndex] == NULL_NODE_INDEX) {
            "Child node $childNodeIndex is already attached to parent ${nodeParents[childNodeIndex]}"
        }
        require(slot in Short.MIN_VALUE..Short.MAX_VALUE) { "Slot $slot does not fit in a 16-bit Short" }
        nodeParents[childNodeIndex] = parentNodeIndex
        nodeChildren[parentNodeIndex].add(childNodeIndex)
        nodeChildSlots[parentNodeIndex].add(slot.toShort())
    }

    /**
     * Records a syntax diagnostic attached to [nodeIndex].
     */
    fun reportDiagnostic(
        nodeIndex: Int,
        startOffset: Int,
        endOffset: Int = startOffset,
        code: String = "SYNTAX_ERROR",
        message: String,
        severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
    ) {
        diagnostics.add(
            CstDiagnostic(
                nodeIndex = nodeIndex,
                startOffset = startOffset,
                endOffset = endOffset,
                code = code,
                message = message,
                severity = severity,
            )
        )
    }

    private fun allocateRawNode(kind: Int, startOffset: Int, rawLength: Int): Int {
        val index = nodeKinds.size
        nodeKinds.add(kind)
        nodeParents.add(NULL_NODE_INDEX)
        nodeStartOffsets.add(startOffset)
        nodeRawLengths.add(rawLength)
        nodeChildren.add(mutableListOf())
        nodeChildSlots.add(mutableListOf())
        return index
    }

    /**
     * Compacts all accumulated structural data into an immutable [ConcreteSyntaxTree].
     */
    fun build(sourceText: String = ""): ConcreteSyntaxTree {
        val totalNodes = nodeKinds.size
        if (totalNodes == 0) return ConcreteSyntaxTree.empty()

        // Ensure any unfinished nodes have valid start offsets and computed lengths (bottom-up)
        for (i in (totalNodes - 1) downTo 0) {
            if (nodeRawLengths[i] == UNFINISHED_RAW_LENGTH || nodeStartOffsets[i] < 0) {
                finishNode(i)
            }
        }

        val nodeKindsArr = nodeKinds.toIntArray()
        val nodeParentsArr = nodeParents.toIntArray()
        val nodeStartOffsetsArr = nodeStartOffsets.toIntArray()
        val nodeLengthsArr = nodeRawLengths.toIntArray()
        val nodeChildOffsetsArr = IntArray(totalNodes + 1)

        var totalEdges = 0
        for (i in 0..<totalNodes) {
            totalEdges += nodeChildren[i].size
        }

        val childNodeIndicesArr = IntArray(totalEdges)
        val childSlotsArr = ShortArray(totalEdges)
        val nodeOrderInParentArr = IntArray(totalNodes) { -1 }

        var edgeCursor = 0
        for (parentIdx in 0..<totalNodes) {
            nodeChildOffsetsArr[parentIdx] = edgeCursor
            val children = nodeChildren[parentIdx]
            val slots = nodeChildSlots[parentIdx]
            for (j in children.indices) {
                val childIdx = children[j]
                val edgeIdx = edgeCursor + j
                childNodeIndicesArr[edgeIdx] = childIdx
                childSlotsArr[edgeIdx] = slots[j]
                nodeOrderInParentArr[childIdx] = edgeIdx
            }
            edgeCursor += children.size
        }
        nodeChildOffsetsArr[totalNodes] = edgeCursor

        return ConcreteSyntaxTree(
            nodeKinds = nodeKindsArr,
            nodeParents = nodeParentsArr,
            nodeStartOffsets = nodeStartOffsetsArr,
            nodeLengths = nodeLengthsArr,
            nodeChildOffsets = nodeChildOffsetsArr,
            childNodeIndices = childNodeIndicesArr,
            childSlots = childSlotsArr,
            nodeOrderInParent = nodeOrderInParentArr,
            sourceText = sourceText,
            diagnostics = diagnostics.toList(),
        )
    }
}

/**
 * Builds a [ConcreteSyntaxTree] with the provided [ConcreteSyntaxBuilder] DSL block.
 */
inline fun buildCst(sourceText: String = "", block: ConcreteSyntaxBuilder.() -> Unit): ConcreteSyntaxTree {
    val builder = ConcreteSyntaxBuilder()
    builder.block()
    return builder.build(sourceText)
}
