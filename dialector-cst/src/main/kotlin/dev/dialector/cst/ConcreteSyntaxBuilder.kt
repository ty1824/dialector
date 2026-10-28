package dev.dialector.cst

import java.util.Collections

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
 * Sentinel rawLength indicating a composite node whose span is currently being computed by [ConcreteSyntaxBuilder.finishNode].
 */
internal const val IN_PROGRESS_RAW_LENGTH: Int = -3

/**
 * A builder for constructing [ConcreteSyntaxTree] instances using flat primitive arrays
 * (zero boxing and zero per-node list allocations).
 *
 * @param initialCapacity Initial capacity for node and edge primitive arrays.
 * @param checkCycles Whether to check for ancestor cycles on [addChild] and [build].
 */
class ConcreteSyntaxBuilder(
    initialCapacity: Int = 64,
    val checkCycles: Boolean = false,
    var schema: CstSchemaDescriptor? = null,
) {
    private var nodeCapacity: Int = maxOf(8, initialCapacity)
    private var nodeCount: Int = 0
    private var nodeKinds = IntArray(nodeCapacity)
    private var nodeParents = IntArray(nodeCapacity)
    private var nodeStartOffsets = IntArray(nodeCapacity)
    // -1 = missing, -2 = unfinished, -3 = in-progress
    private var nodeRawLengths = IntArray(nodeCapacity)
    private var nodeFirstEdge = IntArray(nodeCapacity)
    private var nodeLastEdge = IntArray(nodeCapacity)
    private var nodeChildCounts = IntArray(nodeCapacity)

    private var edgeCapacity: Int = maxOf(8, initialCapacity)
    private var edgeCount: Int = 0
    private var edgeChildren = IntArray(edgeCapacity)
    private var edgeSlots = ShortArray(edgeCapacity)
    private var edgeNext = IntArray(edgeCapacity)

    private val diagnostics = mutableListOf<CstDiagnostic>()

    /**
     * Allocates a new composite node with the given [kind] and optional initial [startOffset].
     * If [startOffset] is left as -1, [finishNode] will automatically derive it from its children.
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
        require(nodeIndex in 0..<nodeCount) { "Node index $nodeIndex out of bounds" }
        val currentRawLen = nodeRawLengths[nodeIndex]
        if (currentRawLen == MISSING_RAW_LENGTH) return // Already marked missing
        check(currentRawLen != IN_PROGRESS_RAW_LENGTH) {
            "Cycle detected in CST while finishing node $nodeIndex"
        }

        nodeRawLengths[nodeIndex] = IN_PROGRESS_RAW_LENGTH

        var e = nodeFirstEdge[nodeIndex]
        while (e >= 0) {
            val childIdx = edgeChildren[e]
            val childRawLen = nodeRawLengths[childIdx]
            check(childRawLen != IN_PROGRESS_RAW_LENGTH) {
                "Cycle detected in CST involving node $childIdx"
            }
            if (childRawLen == UNFINISHED_RAW_LENGTH || nodeStartOffsets[childIdx] < 0) {
                finishNode(childIdx)
            }
            e = edgeNext[e]
        }

        var minNonEmptyStart = Int.MAX_VALUE
        var maxNonEmptyEnd = Int.MIN_VALUE
        var minZeroLenStart = Int.MAX_VALUE
        var maxZeroLenEnd = Int.MIN_VALUE
        var minMissingStart = Int.MAX_VALUE
        var maxMissingEnd = Int.MIN_VALUE

        e = nodeFirstEdge[nodeIndex]
        while (e >= 0) {
            val childIdx = edgeChildren[e]
            val cStart = maxOf(0, nodeStartOffsets[childIdx])
            val cRawLen = nodeRawLengths[childIdx]
            if (cRawLen > 0) {
                if (cStart < minNonEmptyStart) minNonEmptyStart = cStart
                val cEnd = cStart + cRawLen
                if (cEnd > maxNonEmptyEnd) maxNonEmptyEnd = cEnd
            } else if (cRawLen != MISSING_RAW_LENGTH) {
                if (cStart < minZeroLenStart) minZeroLenStart = cStart
                if (cStart > maxZeroLenEnd) maxZeroLenEnd = cStart
            } else {
                if (cStart < minMissingStart) minMissingStart = cStart
                if (cStart > maxMissingEnd) maxMissingEnd = cStart
            }
            e = edgeNext[e]
        }

        var start = nodeStartOffsets[nodeIndex]
        if (start < 0) {
            start = when {
                minNonEmptyStart != Int.MAX_VALUE -> minNonEmptyStart
                minZeroLenStart != Int.MAX_VALUE -> minZeroLenStart
                minMissingStart != Int.MAX_VALUE -> minMissingStart
                else -> 0
            }
            nodeStartOffsets[nodeIndex] = start
        }
        val resolvedEnd = when {
            endOffset >= 0 -> endOffset
            maxNonEmptyEnd != Int.MIN_VALUE -> maxOf(start, maxNonEmptyEnd)
            maxZeroLenEnd != Int.MIN_VALUE -> maxOf(start, maxZeroLenEnd)
            maxMissingEnd != Int.MIN_VALUE -> maxOf(start, maxMissingEnd)
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
     * Convenience helper to create an [ERROR_NODE_KIND] composite node, attach it to [parentNode]
     * at [slot], populate any skipped recovery tokens/children in [block], and compute its span.
     */
    inline fun errorNode(
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        startOffset: Int = -1,
        endOffset: Int = -1,
        diagnosticCode: String? = null,
        diagnosticMessage: String? = null,
        block: (nodeIndex: Int) -> Unit = {},
    ): Int {
        val idx = node(
            kind = ERROR_NODE_KIND,
            parentNode = parentNode,
            slot = slot,
            startOffset = startOffset,
            endOffset = endOffset,
            block = block,
        )
        if (diagnosticMessage != null) {
            val s = getFinishedStartOffset(idx)
            val e = s + getFinishedLength(idx)
            reportDiagnostic(
                nodeIndex = idx,
                startOffset = s,
                endOffset = e,
                code = diagnosticCode ?: "SYNTAX_ERROR",
                message = diagnosticMessage,
            )
        }
        return idx
    }

    /**
     * Marks a [slot] on [parentNode] as unavailable (`isError == true`) when error recovery
     * could not establish a usable binding (for example, on a category slot where no specific
     * concrete node could be constructed).
     */
    fun addUnavailableNode(
        offset: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        endOffset: Int = offset,
        diagnosticCode: String? = null,
        diagnosticMessage: String? = null,
    ): Int {
        val length = maxOf(0, endOffset - offset)
        val nodeIndex = allocateRawNode(kind = ERROR_NODE_KIND, startOffset = offset, rawLength = length)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, nodeIndex, slot)
        }
        if (diagnosticMessage != null) {
            reportDiagnostic(
                nodeIndex = nodeIndex,
                startOffset = offset,
                endOffset = offset + length,
                code = diagnosticCode ?: "UNAVAILABLE_NODE",
                message = diagnosticMessage,
            )
        }
        return nodeIndex
    }

    @PublishedApi
    internal fun getFinishedStartOffset(nodeIndex: Int): Int = maxOf(0, nodeStartOffsets[nodeIndex])

    @PublishedApi
    internal fun getFinishedLength(nodeIndex: Int): Int = maxOf(0, nodeRawLengths[nodeIndex])

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
        isError: Boolean = false,
    ): Int {
        val runtimeKind = if (schemaTokenId < 0) {
            schemaTokenId
        } else {
            encodeTokenKind(schemaTokenId, isTrivia = isTrivia, isError = isError)
        }
        val length = maxOf(0, endOffset - startOffset)
        val tokenIndex = allocateRawNode(kind = runtimeKind, startOffset = startOffset, rawLength = length)
        if (parentNode != NULL_NODE_INDEX) {
            addChild(parentNode, tokenIndex, if (isTrivia) NO_SLOT.toInt() else slot)
        }
        return tokenIndex
    }

    /**
     * Allocates a leaf token flagged as an error/recovery token (`isError == true`),
     * preserving its [schemaTokenId] and source span (`startOffset..endOffset`) without requiring
     * a wrapper [ERROR_NODE_KIND] node.
     */
    fun addErrorToken(
        schemaTokenId: Int,
        startOffset: Int,
        endOffset: Int,
        parentNode: Int = NULL_NODE_INDEX,
        slot: Int = NO_SLOT.toInt(),
        diagnosticCode: String? = null,
        diagnosticMessage: String? = null,
    ): Int {
        val tokenIndex = addToken(
            schemaTokenId = schemaTokenId,
            startOffset = startOffset,
            endOffset = endOffset,
            parentNode = parentNode,
            slot = slot,
            isTrivia = false,
            isError = true,
        )
        if (diagnosticMessage != null) {
            reportDiagnostic(
                nodeIndex = tokenIndex,
                startOffset = startOffset,
                endOffset = endOffset,
                code = diagnosticCode ?: "UNEXPECTED_TOKEN",
                message = diagnosticMessage,
            )
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
        require(parentNodeIndex in 0..<nodeCount) { "Parent node index $parentNodeIndex is out of range" }
        require(childNodeIndex in 0..<nodeCount) { "Child node index $childNodeIndex is out of range" }
        require(parentNodeIndex != childNodeIndex) { "Node $childNodeIndex cannot be a child of itself" }
        require(nodeParents[childNodeIndex] == NULL_NODE_INDEX) {
            "Child node $childNodeIndex is already attached to parent ${nodeParents[childNodeIndex]}"
        }
        require(slot in Short.MIN_VALUE..Short.MAX_VALUE) { "Slot $slot does not fit in a 16-bit Short" }
        if (checkCycles) {
            var anc = nodeParents[parentNodeIndex]
            while (anc != NULL_NODE_INDEX) {
                require(anc != childNodeIndex) {
                    "Cycle detected: node $childNodeIndex is an ancestor of $parentNodeIndex"
                }
                anc = nodeParents[anc]
            }
        }
        nodeParents[childNodeIndex] = parentNodeIndex
        appendEdge(parentNodeIndex, childNodeIndex, slot.toShort())
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
        val index = nodeCount
        if (index == nodeCapacity) {
            val newCap = nodeCapacity * 2
            nodeKinds = nodeKinds.copyOf(newCap)
            nodeParents = nodeParents.copyOf(newCap)
            nodeStartOffsets = nodeStartOffsets.copyOf(newCap)
            nodeRawLengths = nodeRawLengths.copyOf(newCap)
            nodeFirstEdge = nodeFirstEdge.copyOf(newCap)
            nodeLastEdge = nodeLastEdge.copyOf(newCap)
            nodeChildCounts = nodeChildCounts.copyOf(newCap)
            nodeCapacity = newCap
        }
        nodeKinds[index] = kind
        nodeParents[index] = NULL_NODE_INDEX
        nodeStartOffsets[index] = startOffset
        nodeRawLengths[index] = rawLength
        nodeFirstEdge[index] = -1
        nodeLastEdge[index] = -1
        nodeChildCounts[index] = 0
        nodeCount = index + 1
        return index
    }

    private fun appendEdge(parentNodeIndex: Int, childNodeIndex: Int, slot: Short) {
        val e = edgeCount
        if (e == edgeCapacity) {
            val newCap = edgeCapacity * 2
            edgeChildren = edgeChildren.copyOf(newCap)
            edgeSlots = edgeSlots.copyOf(newCap)
            edgeNext = edgeNext.copyOf(newCap)
            edgeCapacity = newCap
        }
        edgeChildren[e] = childNodeIndex
        edgeSlots[e] = slot
        edgeNext[e] = -1
        val prevLast = nodeLastEdge[parentNodeIndex]
        if (prevLast < 0) {
            nodeFirstEdge[parentNodeIndex] = e
        } else {
            edgeNext[prevLast] = e
        }
        nodeLastEdge[parentNodeIndex] = e
        nodeChildCounts[parentNodeIndex]++
        edgeCount = e + 1
    }

    /**
     * Compacts all accumulated structural data into an immutable [ConcreteSyntaxTree].
     */
    fun build(
        sourceText: String = "",
        checkCycles: Boolean = this.checkCycles,
        schema: CstSchemaDescriptor? = this.schema,
    ): ConcreteSyntaxTree {
        val totalNodes = nodeCount
        if (totalNodes == 0) return ConcreteSyntaxTree.empty(schema = schema)

        if (checkCycles) {
            validateNoCycles(totalNodes)
        }

        // Ensure any unfinished nodes have valid start offsets and computed lengths (bottom-up)
        for (i in (totalNodes - 1) downTo 0) {
            if (nodeRawLengths[i] == UNFINISHED_RAW_LENGTH || nodeStartOffsets[i] < 0) {
                finishNode(i)
            }
        }

        val nodeKindsArr = nodeKinds.copyOf(totalNodes)
        val nodeParentsArr = nodeParents.copyOf(totalNodes)
        val nodeStartOffsetsArr = nodeStartOffsets.copyOf(totalNodes)
        val nodeLengthsArr = nodeRawLengths.copyOf(totalNodes)
        val nodeChildOffsetsArr = IntArray(totalNodes + 1)

        val totalEdges = edgeCount
        val childNodeIndicesArr = IntArray(totalEdges)
        val childSlotsArr = ShortArray(totalEdges)
        val nodeOrderInParentArr = IntArray(totalNodes) { -1 }

        var edgeCursor = 0
        for (parentIdx in 0..<totalNodes) {
            nodeChildOffsetsArr[parentIdx] = edgeCursor
            val count = nodeChildCounts[parentIdx]
            if (count > 0) {
                var e = nodeFirstEdge[parentIdx]
                var j = 0
                var prevNonMissingStart = Int.MIN_VALUE
                var needsSort = false
                while (e >= 0) {
                    val childIdx = edgeChildren[e]
                    val edgeIdx = edgeCursor + j
                    childNodeIndicesArr[edgeIdx] = childIdx
                    childSlotsArr[edgeIdx] = edgeSlots[e]
                    if (nodeLengthsArr[childIdx] != MISSING_RAW_LENGTH) {
                        val cStart = nodeStartOffsetsArr[childIdx]
                        if (cStart < prevNonMissingStart) {
                            needsSort = true
                        } else {
                            prevNonMissingStart = cStart
                        }
                    }
                    j++
                    e = edgeNext[e]
                }

                if (needsSort) {
                    sortChildEdgesByStartOffset(
                        childNodeIndicesArr = childNodeIndicesArr,
                        childSlotsArr = childSlotsArr,
                        nodeStartOffsetsArr = nodeStartOffsetsArr,
                        nodeLengthsArr = nodeLengthsArr,
                        startEdge = edgeCursor,
                        endEdge = edgeCursor + count,
                    )
                }

                for (k in 0..<count) {
                    val edgeIdx = edgeCursor + k
                    nodeOrderInParentArr[childNodeIndicesArr[edgeIdx]] = edgeIdx
                }
                edgeCursor += count
            }
        }
        nodeChildOffsetsArr[totalNodes] = edgeCursor

        val immutableDiagnostics: List<CstDiagnostic> = if (diagnostics.isEmpty()) {
            emptyList()
        } else {
            Collections.unmodifiableList(ArrayList(diagnostics))
        }

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
            diagnostics = immutableDiagnostics,
            schema = schema,
        )
    }

    private fun validateNoCycles(totalNodes: Int) {
        // 0 = unvisited, 1 = visiting, 2 = visited
        val state = ByteArray(totalNodes)
        for (i in 0..<totalNodes) {
            if (state[i].toInt() != 0) continue
            var curr = i
            while (curr != NULL_NODE_INDEX) {
                val s = state[curr].toInt()
                if (s == 1) {
                    throw IllegalStateException("Cycle detected in CST parent chain involving node $curr")
                }
                if (s == 2) break
                state[curr] = 1
                curr = nodeParents[curr]
            }
            curr = i
            while (curr != NULL_NODE_INDEX && state[curr].toInt() == 1) {
                state[curr] = 2
                curr = nodeParents[curr]
            }
        }
    }

    private fun sortChildEdgesByStartOffset(
        childNodeIndicesArr: IntArray,
        childSlotsArr: ShortArray,
        nodeStartOffsetsArr: IntArray,
        nodeLengthsArr: IntArray,
        startEdge: Int,
        endEdge: Int,
    ) {
        // Stable insertion sort on startEdge..<endEdge.
        // Missing children with default offset 0 inherit the preceding sibling's end offset so they stay in relative position.
        val count = endEdge - startEdge
        val sortKeys = IntArray(count)
        var runningOffset = 0
        for (i in 0..<count) {
            val childIdx = childNodeIndicesArr[startEdge + i]
            val cStart = nodeStartOffsetsArr[childIdx]
            val cLen = nodeLengthsArr[childIdx]
            if (cLen == MISSING_RAW_LENGTH && cStart == 0) {
                sortKeys[i] = runningOffset
            } else {
                sortKeys[i] = cStart
                runningOffset = cStart + maxOf(0, cLen)
            }
        }

        for (i in 1..<count) {
            val key = sortKeys[i]
            val childIdx = childNodeIndicesArr[startEdge + i]
            val slot = childSlotsArr[startEdge + i]
            var j = i - 1
            while (j >= 0 && sortKeys[j] > key) {
                sortKeys[j + 1] = sortKeys[j]
                childNodeIndicesArr[startEdge + j + 1] = childNodeIndicesArr[startEdge + j]
                childSlotsArr[startEdge + j + 1] = childSlotsArr[startEdge + j]
                j--
            }
            sortKeys[j + 1] = key
            childNodeIndicesArr[startEdge + j + 1] = childIdx
            childSlotsArr[startEdge + j + 1] = slot
        }
    }
}

/**
 * Builds a [ConcreteSyntaxTree] with the provided [ConcreteSyntaxBuilder] DSL block.
 */
inline fun buildCst(
    sourceText: String = "",
    checkCycles: Boolean = false,
    schema: CstSchemaDescriptor? = null,
    block: ConcreteSyntaxBuilder.() -> Unit,
): ConcreteSyntaxTree {
    val builder = ConcreteSyntaxBuilder(checkCycles = checkCycles, schema = schema)
    builder.block()
    return builder.build(sourceText, checkCycles = checkCycles, schema = schema)
}
