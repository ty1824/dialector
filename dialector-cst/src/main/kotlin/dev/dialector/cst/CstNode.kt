package dev.dialector.cst

/**
 * Value-class handle representing a CST node.
 * Never use nullable `CstNode?` in hot paths; use [CstNode.NONE] and check [isValid]
 * to guarantee zero heap boxing.
 */
@JvmInline
value class CstNode(val nodeIndex: Int) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    val isValid: Boolean get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    val isNone: Boolean get() = nodeIndex < 0

    /**
     * Reinterprets this node handle as a [CstToken] handle without allocation.
     */
    val asToken: CstToken get() = CstToken(nodeIndex)

    /**
     * Returns the parent node handle, or [CstNode.NONE] if this is a root or invalid node.
     */
    context(tree: ConcreteSyntaxTree)
    val parent: CstNode
        get() = if (isValid) CstNode(tree.getNodeParent(nodeIndex)) else NONE

    /**
     * Returns the kind integer of this node.
     */
    context(tree: ConcreteSyntaxTree)
    val kind: Int
        get() = tree.getNodeKind(nodeIndex)

    /**
     * True if this node's kind is a token (leaf), false if it is a composite node.
     */
    context(tree: ConcreteSyntaxTree)
    val isToken: Boolean
        get() = isValid && isTokenKind(tree.getNodeKind(nodeIndex))

    /**
     * True if this node is a trivia token (whitespace or comment).
     */
    context(tree: ConcreteSyntaxTree)
    val isTrivia: Boolean
        get() = isValid && isTriviaKind(tree.getNodeKind(nodeIndex))

    /**
     * True if this node is an [ERROR_NODE_KIND] recovery wrapper, an unavailable slot placeholder,
     * or a leaf token marked as an error/recovery token.
     */
    context(tree: ConcreteSyntaxTree)
    val isError: Boolean
        get() = isValid && isErrorKind(tree.getNodeKind(nodeIndex))

    /**
     * True if this node or token was synthesized by the parser during error recovery
     * to stand in for a missing mandatory grammar element.
     */
    context(tree: ConcreteSyntaxTree)
    val isMissing: Boolean
        get() = isValid && tree.isNodeMissing(nodeIndex)

    /**
     * True if this node or token is present and usable in the syntax tree
     * (`isValid && !isMissing && !isError`).
     *
     * Together with [isNone] (Absent), [isMissing] (Missing), and [isError] (Unavailable / Recovery),
     * this distinguishes all four states of a CST slot binding.
     */
    context(tree: ConcreteSyntaxTree)
    val isPresent: Boolean
        get() = isValid && !isMissing && !isError

    /**
     * Returns the [Document] backing this syntax tree.
     */
    context(tree: ConcreteSyntaxTree)
    val document: Document
        get() = tree.document

    /**
     * Returns the source text slice of this node.
     */
    context(tree: ConcreteSyntaxTree)
    val text: String
        get() = if (isValid) tree.getText(nodeIndex) else ""

    /**
     * Returns the start character offset of this node.
     */
    context(tree: ConcreteSyntaxTree)
    val startOffset: Int
        get() = tree.getNodeStartOffset(nodeIndex)

    /**
     * Returns the character length of this node (0 for missing/synthetic nodes).
     */
    context(tree: ConcreteSyntaxTree)
    val length: Int
        get() = tree.getNodeLength(nodeIndex)

    /**
     * Returns the end character offset of this node.
     */
    context(tree: ConcreteSyntaxTree)
    val endOffset: Int
        get() = tree.getNodeEndOffset(nodeIndex)

    /**
     * Returns the zero-allocation [SourceRange] (`startOffset..endOffset`) of this node, or [SourceRange.NONE] if invalid.
     */
    context(tree: ConcreteSyntaxTree)
    val sourceRange: SourceRange
        get() = if (isValid) tree.getNodeSourceRange(nodeIndex) else SourceRange.NONE

    /**
     * Returns the 1-based `(line, column)` [SourcePosition] at the start of this node, or [SourcePosition.NONE] if invalid.
     */
    context(tree: ConcreteSyntaxTree)
    val startPosition: SourcePosition
        get() = if (isValid) tree.getPosition(startOffset) else SourcePosition.NONE

    /**
     * Returns the 1-based `(line, column)` [SourcePosition] at the end of this node, or [SourcePosition.NONE] if invalid.
     */
    context(tree: ConcreteSyntaxTree)
    val endPosition: SourcePosition
        get() = if (isValid) tree.getPosition(endOffset) else SourcePosition.NONE

    /**
     * Returns the schema slot index this node occupies in its parent, or [NO_SLOT] (-1).
     */
    context(tree: ConcreteSyntaxTree)
    val slotInParent: Int
        get() = if (isValid) tree.getNodeSlotInParent(nodeIndex) else NO_SLOT.toInt()

    /**
     * Returns the previous sibling node in the parent's children, or [CstNode.NONE].
     */
    context(tree: ConcreteSyntaxTree)
    val prevSibling: CstNode
        get() = if (isValid) CstNode(tree.getPrevSibling(nodeIndex)) else NONE

    /**
     * Returns the next sibling node in the parent's children, or [CstNode.NONE].
     */
    context(tree: ConcreteSyntaxTree)
    val nextSibling: CstNode
        get() = if (isValid) CstNode(tree.getNextSibling(nodeIndex)) else NONE

    /**
     * Returns the total number of direct children (including tokens and trivia).
     */
    context(tree: ConcreteSyntaxTree)
    val childCount: Int
        get() = if (isValid) tree.getChildNodeCount(nodeIndex) else 0

    /**
     * Returns the direct child at relative [index], or [CstNode.NONE] if out of bounds.
     */
    context(tree: ConcreteSyntaxTree)
    fun childAt(index: Int): CstNode =
        if (isValid) CstNode(tree.getChildNodeAt(nodeIndex, index)) else NONE

    /**
     * Returns the first child node assigned to [slot], or [CstNode.NONE] if absent.
     */
    context(tree: ConcreteSyntaxTree)
    fun childBySlot(slot: Int): CstNode =
        if (isValid) CstNode(tree.getChildBySlot(nodeIndex, slot)) else NONE

    /**
     * Returns the first child token assigned to [slot], or [CstToken.NONE] if absent.
     */
    context(tree: ConcreteSyntaxTree)
    fun tokenBySlot(slot: Int): CstToken =
        if (isValid) CstToken(tree.getChildBySlot(nodeIndex, slot)) else CstToken.NONE

    /**
     * Returns a zero-allocation view over all direct children of this node.
     */
    context(tree: ConcreteSyntaxTree)
    val children: CstNodeList
        get() = if (isValid) tree.getChildrenList(nodeIndex) else CstNodeList.EMPTY

    /**
     * Iterates over all direct children allocated to [slot] without heap allocation.
     */
    context(tree: ConcreteSyntaxTree)
    inline fun forEachChildInSlot(slot: Int, action: (CstNode) -> Unit) {
        if (!isValid) return
        tree.forEachChildInSlot(nodeIndex, slot) { childIdx ->
            action(CstNode(childIdx))
        }
    }

    /**
     * Stackless, zero-allocation depth-first pre-order iteration over all descendants of this node.
     */
    context(tree: ConcreteSyntaxTree)
    inline fun forEachDescendant(
        inclusive: Boolean = false,
        includeTrivia: Boolean = true,
        action: (CstNode) -> Unit,
    ) {
        if (!isValid) return
        tree.forEachDescendant(nodeIndex, inclusive = inclusive, includeTrivia = includeTrivia, action = action)
    }

    /**
     * Zero-allocation iteration over all ancestors of this node up to the root.
     */
    context(tree: ConcreteSyntaxTree)
    inline fun forEachAncestor(
        inclusive: Boolean = false,
        action: (CstNode) -> Unit,
    ) {
        if (!isValid) return
        tree.forEachAncestor(nodeIndex, inclusive = inclusive, action = action)
    }

    override fun toString(): String = if (isValid) "CstNode($nodeIndex)" else "CstNode.NONE"

    companion object {
        /**
         * Sentinel representing an absent or invalid node.
         */
        val NONE: CstNode = CstNode(NULL_NODE_INDEX)
    }
}

/**
 * Zero-allocation slice view over a contiguous range in [ConcreteSyntaxTree.childNodeIndices].
 * Packs `(startEdgeOffset: Int, endEdgeOffset: Int)` into a single 64-bit [packedRange].
 */
@JvmInline
value class CstNodeList(val packedRange: Long) {
    /**
     * Starting index into the flattened child edge arrays.
     */
    val startEdgeOffset: Int get() = (packedRange ushr 32).toInt()

    /**
     * Ending index (exclusive) into the flattened child edge arrays.
     */
    val endEdgeOffset: Int get() = packedRange.toInt()

    /**
     * Total number of child elements in this slice (clamped to >= 0).
     */
    val size: Int get() = maxOf(0, endEdgeOffset - startEdgeOffset)

    /**
     * True if this slice contains no elements.
     */
    val isEmpty: Boolean get() = size == 0

    /**
     * Returns the [CstNode] at [index] (0..<size), or [CstNode.NONE] if out of bounds.
     */
    context(tree: ConcreteSyntaxTree)
    operator fun get(index: Int): CstNode {
        if (index !in 0..<size) return CstNode.NONE
        return CstNode(tree.getRawChildEdgeNode(startEdgeOffset + index))
    }

    /**
     * Zero-allocation inline iteration over all children in this slice.
     */
    context(tree: ConcreteSyntaxTree)
    inline fun forEach(
        includeTrivia: Boolean = false,
        action: (CstNode) -> Unit,
    ) {
        for (edgeIdx in startEdgeOffset..<endEdgeOffset) {
            val childIdx = tree.getRawChildEdgeNode(edgeIdx)
            if (!includeTrivia && isTriviaKind(tree.getNodeKind(childIdx))) continue
            action(CstNode(childIdx))
        }
    }

    /**
     * Zero-allocation inline iteration over children matching a specific schema [slot].
     */
    context(tree: ConcreteSyntaxTree)
    inline fun forEachInSlot(
        slot: Int,
        action: (CstNode) -> Unit,
    ) {
        val targetSlot = slot.toShort()
        for (edgeIdx in startEdgeOffset..<endEdgeOffset) {
            if (tree.getRawChildEdgeSlot(edgeIdx) == targetSlot) {
                action(CstNode(tree.getRawChildEdgeNode(edgeIdx)))
            }
        }
    }

    companion object {
        val EMPTY: CstNodeList = CstNodeList(0L)

        fun pack(startEdgeOffset: Int, endEdgeOffset: Int): CstNodeList {
            val clampedEnd = maxOf(startEdgeOffset, endEdgeOffset)
            return CstNodeList((startEdgeOffset.toLong() shl 32) or (clampedEnd.toLong() and 0xFFFF_FFFFL))
        }
    }
}