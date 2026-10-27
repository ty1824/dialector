package dev.dialector.cst

/**
 * Constant representing an invalid or absent node/token index.
 */
const val NULL_NODE_INDEX: Int = -1

/**
 * Constant representing an invalid or absent token index.
 */
const val NULL_TOKEN_INDEX: Int = -1

/**
 * Constant representing an unassigned or trivia/error child slot.
 */
const val NO_SLOT: Short = -1

/**
 * Built-in node kind for syntax error recovery wrappers (unexpected tokens/nodes).
 */
const val ERROR_NODE_KIND: Int = 0

/**
 * Bitmask applied to negative token kinds to denote trivia (whitespace, comments).
 * Regular tokens: -1 downTo -0x3FFFFFFF (bits 31 and 30 are 11).
 * Trivia tokens:  -0x40000001 and below (bit 31 is 1, bit 30 is 0).
 */
const val TRIVIA_KIND_MASK: Int = 0x4000_0000

/**
 * Returns true if [kind] represents a leaf token (regular or trivia).
 */
@Suppress("NOTHING_TO_INLINE")
inline fun isTokenKind(kind: Int): Boolean = kind < 0

/**
 * Returns true if [kind] represents a trivia token (whitespace or comment).
 */
@Suppress("NOTHING_TO_INLINE")
inline fun isTriviaKind(kind: Int): Boolean = kind < 0 && (kind and TRIVIA_KIND_MASK) == 0

/**
 * Returns true if [kind] represents a composite (non-token) node, including [ERROR_NODE_KIND].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun isCompositeKind(kind: Int): Boolean = kind >= 0

/**
 * Converts a positive 1-based schema token ID into its negative runtime token kind.
 */
fun encodeTokenKind(schemaTokenId: Int, isTrivia: Boolean = false): Int {
    require(schemaTokenId > 0 && schemaTokenId < TRIVIA_KIND_MASK) {
        "Schema token ID must be in 1..<0x40000000 (got $schemaTokenId)"
    }
    return if (isTrivia) {
        (-schemaTokenId) and TRIVIA_KIND_MASK.inv()
    } else {
        -schemaTokenId
    }
}

/**
 * Decodes a negative runtime token kind back into its positive 1-based schema token ID.
 */
fun decodeTokenSchemaId(tokenKind: Int): Int {
    require(tokenKind < 0) { "Expected negative token kind, got $tokenKind" }
    val withBitRestored = tokenKind or TRIVIA_KIND_MASK
    return -withBitRestored
}

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
     * True if this node is an [ERROR_NODE_KIND] recovery wrapper.
     */
    context(tree: ConcreteSyntaxTree)
    val isError: Boolean
        get() = isValid && tree.getNodeKind(nodeIndex) == ERROR_NODE_KIND

    /**
     * True if this node or token was synthesized by the parser during error recovery
     * to stand in for a missing mandatory grammar element.
     */
    context(tree: ConcreteSyntaxTree)
    val isMissing: Boolean
        get() = isValid && tree.isNodeMissing(nodeIndex)

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

    override fun toString(): String = if (isValid) "CstNode($nodeIndex)" else "CstNode.NONE"

    companion object {
        /**
         * Sentinel representing an absent or invalid node.
         */
        val NONE: CstNode = CstNode(NULL_NODE_INDEX)
    }
}

/**
 * Value-class handle representing a CST leaf token.
 * Never use nullable `CstToken?` in hot paths; use [CstToken.NONE] and check [isValid].
 */
@JvmInline
value class CstToken(val tokenIndex: Int) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    val isValid: Boolean get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    val isNone: Boolean get() = tokenIndex < 0

    /**
     * Reinterprets this token handle as a [CstNode] handle without allocation.
     */
    val asNode: CstNode get() = CstNode(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    val parent: CstNode get() = asNode.parent

    context(tree: ConcreteSyntaxTree)
    val kind: Int get() = asNode.kind

    /**
     * Returns the decoded positive 1-based schema token ID, or 0 if this handle does not point to a token.
     */
    context(tree: ConcreteSyntaxTree)
    val schemaId: Int
        get() {
            if (!isValid) return 0
            val rawKind = tree.getNodeKind(tokenIndex)
            return if (isTokenKind(rawKind)) decodeTokenSchemaId(rawKind) else 0
        }

    context(tree: ConcreteSyntaxTree)
    val isTrivia: Boolean get() = asNode.isTrivia

    context(tree: ConcreteSyntaxTree)
    val isMissing: Boolean get() = asNode.isMissing

    context(tree: ConcreteSyntaxTree)
    val text: String get() = asNode.text

    context(tree: ConcreteSyntaxTree)
    val startOffset: Int get() = asNode.startOffset

    context(tree: ConcreteSyntaxTree)
    val length: Int get() = asNode.length

    context(tree: ConcreteSyntaxTree)
    val endOffset: Int get() = asNode.endOffset

    context(tree: ConcreteSyntaxTree)
    val sourceRange: SourceRange get() = asNode.sourceRange

    context(tree: ConcreteSyntaxTree)
    val startPosition: SourcePosition get() = asNode.startPosition

    context(tree: ConcreteSyntaxTree)
    val endPosition: SourcePosition get() = asNode.endPosition

    context(tree: ConcreteSyntaxTree)
    val slotInParent: Int get() = asNode.slotInParent

    context(tree: ConcreteSyntaxTree)
    val prevSibling: CstNode get() = asNode.prevSibling

    context(tree: ConcreteSyntaxTree)
    val nextSibling: CstNode get() = asNode.nextSibling

    override fun toString(): String = if (isValid) "CstToken($tokenIndex)" else "CstToken.NONE"

    companion object {
        /**
         * Sentinel representing an absent or invalid token.
         */
        val NONE: CstToken = CstToken(NULL_TOKEN_INDEX)
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

/**
 * Zero-allocation character range `[startOffset, endOffset)` in source text.
 * Packs `(startOffset: Int, endOffset: Int)` into a single 64-bit [packed] `Long`.
 */
@JvmInline
value class SourceRange(val packed: Long) {
    constructor(startOffset: Int, endOffset: Int) : this(packRaw(startOffset, maxOf(startOffset, endOffset)))

    /**
     * 0-based inclusive start character offset, or -1 if [NONE].
     */
    val startOffset: Int get() = (packed ushr 32).toInt()

    /**
     * 0-based exclusive end character offset, or -1 if [NONE].
     */
    val endOffset: Int get() = packed.toInt()

    /**
     * True if this range represents a valid non-negative span (`startOffset >= 0 && endOffset >= startOffset`).
     */
    val isValid: Boolean get() = startOffset >= 0 && endOffset >= startOffset

    /**
     * True if this range is the absent sentinel ([NONE]).
     */
    val isNone: Boolean get() = !isValid

    /**
     * Character length of this range (`0` if empty or invalid).
     */
    val length: Int get() = if (isValid) maxOf(0, endOffset - startOffset) else 0

    /**
     * True if [length] == 0.
     */
    val isEmpty: Boolean get() = length == 0

    /**
     * Converts this range to a standard Kotlin [IntRange] (`startOffset..<endOffset`).
     */
    val asIntRange: IntRange get() = if (isValid) startOffset..<endOffset else IntRange.EMPTY

    /**
     * Returns true if [offset] falls within `[startOffset, endOffset)` (or `offset == startOffset` when empty and valid).
     */
    operator fun contains(offset: Int): Boolean =
        isValid && (offset in startOffset..<endOffset || (startOffset == endOffset && offset == startOffset))

    /**
     * Returns true if [other] is valid and completely enclosed within `[startOffset, endOffset]`.
     */
    operator fun contains(other: SourceRange): Boolean =
        isValid && other.isValid && other.startOffset >= startOffset && other.endOffset <= endOffset

    /**
     * Returns true if this range and [other] overlap.
     */
    fun intersects(other: SourceRange): Boolean {
        if (!isValid || !other.isValid) return false
        if (isEmpty) return other.contains(startOffset)
        if (other.isEmpty) return contains(other.startOffset)
        return startOffset < other.endOffset && other.startOffset < endOffset
    }

    override fun toString(): String = if (isValid) "$startOffset..$endOffset" else "SourceRange.NONE"

    companion object {
        /**
         * Sentinel representing an absent or invalid source range (`-1..-1`).
         */
        val NONE: SourceRange = SourceRange(-1L)

        /**
         * Empty range at offset `0..0`.
         */
        val EMPTY: SourceRange = SourceRange(0L)

        fun pack(startOffset: Int, endOffset: Int): SourceRange =
            SourceRange(startOffset, endOffset)

        private fun packRaw(startOffset: Int, endOffset: Int): Long =
            if (startOffset < 0) {
                -1L
            } else {
                val clampedEnd = maxOf(startOffset, endOffset)
                (startOffset.toLong() shl 32) or (clampedEnd.toLong() and 0xFFFF_FFFFL)
            }
    }
}

/**
 * Zero-allocation 1-based `(line, column)` source coordinates.
 * Packs `(line: Int, column: Int)` into a single 64-bit [packed] `Long`.
 */
@JvmInline
value class SourcePosition(val packed: Long) : Comparable<SourcePosition> {
    constructor(line: Int, column: Int) : this(packRaw(line, column))

    /**
     * 1-based line number, or -1 if [NONE].
     */
    val line: Int get() = (packed ushr 32).toInt()

    /**
     * 1-based column number (UTF-16 code unit offset from the start of the line + 1), or -1 if [NONE].
     */
    val column: Int get() = packed.toInt()

    /**
     * 0-based line index (`line - 1`), or -1 if [NONE].
     */
    val lineIndex: Int get() = if (isValid) line - 1 else -1

    /**
     * 0-based column index (`column - 1`), or -1 if [NONE].
     */
    val columnIndex: Int get() = if (isValid) column - 1 else -1

    /**
     * True if both [line] >= 1 and [column] >= 1.
     */
    val isValid: Boolean get() = line >= 1 && column >= 1

    /**
     * True if this position is the absent sentinel ([NONE]).
     */
    val isNone: Boolean get() = !isValid

    override fun compareTo(other: SourcePosition): Int {
        val lineCmp = line.compareTo(other.line)
        return if (lineCmp != 0) lineCmp else column.compareTo(other.column)
    }

    override fun toString(): String = if (isValid) "$line:$column" else "SourcePosition.NONE"

    companion object {
        /**
         * Sentinel representing an absent or invalid source position (`-1:-1`).
         */
        val NONE: SourcePosition = SourcePosition(-1L)

        fun pack(line: Int, column: Int): SourcePosition =
            SourcePosition(line, column)

        private fun packRaw(line: Int, column: Int): Long =
            if (line < 1 || column < 1) {
                -1L
            } else {
                (line.toLong() shl 32) or (column.toLong() and 0xFFFF_FFFFL)
            }
    }
}

/**
 * Severity levels for syntax diagnostics recorded in a [ConcreteSyntaxTree].
 */
enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO,
}

/**
 * Syntax error or warning attached to a specific node/token and character range.
 */
data class CstDiagnostic(
    val nodeIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val code: String,
    val message: String,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
) {
    /**
     * Zero-allocation [SourceRange] (`startOffset..endOffset`) of this diagnostic.
     */
    val sourceRange: SourceRange
        get() = SourceRange.pack(startOffset, endOffset)
}

/**
 * A Concrete Syntax Tree (CST) represented with contiguous primitive arrays.
 * - Backing arrays are `internal` to preserve immutability invariants.
 * - `nodeLengths[i] == -1` encodes a synthesized/missing node or token at `nodeStartOffsets[i]`.
 * - `childSlots` runs parallel to `childNodeIndices` (`size == childNodeIndices.size`), storing
 *   the schema field slot (`>= 0`) or [NO_SLOT] (`-1`) for trivia and error nodes.
 * - `nodeOrderInParent` stores each node's edge offset inside `childNodeIndices`, enabling
 *   $O(1)$ `prevSibling`, `nextSibling`, and `slotInParent` lookups.
 */
class ConcreteSyntaxTree internal constructor(
    internal val nodeKinds: IntArray,
    internal val nodeParents: IntArray,
    internal val nodeStartOffsets: IntArray,
    internal val nodeLengths: IntArray,
    internal val nodeChildOffsets: IntArray,
    internal val childNodeIndices: IntArray,
    internal val childSlots: ShortArray,
    internal val nodeOrderInParent: IntArray,
    val sourceText: String = "",
    val diagnostics: List<CstDiagnostic> = emptyList(),
) {
    /**
     * Precomputed flag indicating whether any error diagnostics, [ERROR_NODE_KIND] nodes,
     * or missing/synthesized recovery nodes exist in this tree.
     */
    val hasErrors: Boolean

    init {
        val count = nodeKinds.size
        require(nodeParents.size == count) { "nodeParents size mismatch" }
        require(nodeStartOffsets.size == count) { "nodeStartOffsets size mismatch" }
        require(nodeLengths.size == count) { "nodeLengths size mismatch" }
        require(nodeChildOffsets.size == count + 1) { "nodeChildOffsets must have size nodeCount + 1" }
        require(childSlots.size == childNodeIndices.size) { "childSlots must match childNodeIndices size" }
        require(nodeOrderInParent.size == count) { "nodeOrderInParent size mismatch" }

        var foundError = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
        if (!foundError) {
            for (i in 0..<count) {
                if (nodeKinds[i] == ERROR_NODE_KIND || nodeLengths[i] < 0) {
                    foundError = true
                    break
                }
            }
        }
        hasErrors = foundError
    }

    /**
     * Total number of nodes (both composite nodes and leaf tokens) in the CST.
     */
    val nodeCount: Int get() = nodeKinds.size

    /**
     * Root node handle of this tree, or [CstNode.NONE] if empty.
     */
    val root: CstNode
        get() = CstNode(getRootNodeIndex())

    /**
     * Returns the kind ID of the node at [nodeIndex].
     */
    fun getNodeKind(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        return nodeKinds[nodeIndex]
    }

    /**
     * Returns the parent node index of the node at [nodeIndex], or [NULL_NODE_INDEX] if root.
     */
    fun getNodeParent(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        return nodeParents[nodeIndex]
    }

    /**
     * Returns true if [nodeIndex] was synthesized during error recovery (`rawLength < 0`).
     */
    fun isNodeMissing(nodeIndex: Int): Boolean {
        checkNodeBounds(nodeIndex)
        return nodeLengths[nodeIndex] < 0
    }

    /**
     * Returns the start character offset of the node at [nodeIndex].
     */
    fun getNodeStartOffset(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        return nodeStartOffsets[nodeIndex]
    }

    /**
     * Returns the character length of the node at [nodeIndex] (0 if synthesized/missing).
     */
    fun getNodeLength(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        val raw = nodeLengths[nodeIndex]
        return if (raw < 0) 0 else raw
    }

    /**
     * Returns the end character offset of the node at [nodeIndex].
     */
    fun getNodeEndOffset(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        return nodeStartOffsets[nodeIndex] + getNodeLength(nodeIndex)
    }

    /**
     * Returns the [SourceRange] (`startOffset..endOffset`) of the node at [nodeIndex].
     */
    fun getNodeSourceRange(nodeIndex: Int): SourceRange {
        checkNodeBounds(nodeIndex)
        val start = nodeStartOffsets[nodeIndex]
        return SourceRange(start, start + getNodeLength(nodeIndex))
    }

    /**
     * Returns the source text slice corresponding to the node at [nodeIndex].
     */
    fun getText(nodeIndex: Int): String {
        checkNodeBounds(nodeIndex)
        val length = getNodeLength(nodeIndex)
        if (length == 0) return ""
        val start = nodeStartOffsets[nodeIndex]
        val end = start + length
        if (start < 0 || end > sourceText.length) return ""
        return sourceText.substring(start, end)
    }

    /**
     * Returns the source text slice corresponding to [range].
     */
    fun getText(range: SourceRange): String {
        if (!range.isValid || range.isEmpty) return ""
        val start = range.startOffset
        val end = range.endOffset
        if (start < 0 || end > sourceText.length) return ""
        return sourceText.substring(start, end)
    }

    private val lineStartOffsets: IntArray by lazy(LazyThreadSafetyMode.PUBLICATION) {
        computeLineStartOffsets(sourceText)
    }

    /**
     * Total number of lines in [sourceText] (always >= 1).
     */
    val lineCount: Int
        get() = lineStartOffsets.size

    /**
     * Converts a 0-based character [offset] into a 1-based `(line, column)` [SourcePosition] in $O(\log L)$ time,
     * or returns [SourcePosition.NONE] if [offset] is outside `0..sourceText.length`.
     */
    fun getPosition(offset: Int): SourcePosition {
        if (offset < 0 || offset > sourceText.length) return SourcePosition.NONE
        val starts = lineStartOffsets
        val idx = starts.binarySearch(offset)
        val lineIdx = if (idx >= 0) idx else (-idx - 2)
        val lineStart = starts[lineIdx]
        return SourcePosition(line = lineIdx + 1, column = offset - lineStart + 1)
    }

    /**
     * Alias for [getPosition] returning the 1-based `(line, column)` [SourcePosition] for [offset].
     */
    fun getLineAndColumn(offset: Int): SourcePosition = getPosition(offset)

    /**
     * Converts a 1-based [line] and [column] back into a 0-based character offset in $O(1)$ time,
     * or returns `-1` if [line] or [column] is out of bounds for that line.
     */
    fun getOffset(line: Int, column: Int): Int {
        if (line !in 1..lineCount || column < 1) return -1
        val lineRange = getLineRange(line, includeLineBreak = true)
        val offset = lineRange.startOffset + (column - 1)
        val maxValidOffset = if (line < lineCount) lineRange.endOffset - 1 else lineRange.endOffset
        return if (offset <= maxValidOffset) offset else -1
    }

    /**
     * Converts a 1-based [position] back into a 0-based character offset in $O(1)$ time,
     * or returns `-1` if [position] is invalid or out of bounds.
     */
    fun getOffset(position: SourcePosition): Int =
        if (position.isValid) getOffset(position.line, position.column) else -1

    /**
     * Returns the character [SourceRange] of the 1-based [line], or [SourceRange.NONE] if out of bounds.
     * By default, trailing line break characters (`\n`, `\r\n`, `\r`) are excluded unless [includeLineBreak] is true.
     */
    fun getLineRange(line: Int, includeLineBreak: Boolean = false): SourceRange {
        val starts = lineStartOffsets
        if (line !in 1..starts.size) return SourceRange.NONE
        val lineIdx = line - 1
        val start = starts[lineIdx]
        val nextStart = if (lineIdx + 1 < starts.size) starts[lineIdx + 1] else sourceText.length
        if (includeLineBreak) return SourceRange(start, nextStart)

        var end = nextStart
        if (end > start && sourceText[end - 1] == '\n') {
            end--
            if (end > start && sourceText[end - 1] == '\r') end--
        } else if (end > start && sourceText[end - 1] == '\r') {
            end--
        }
        return SourceRange(start, end)
    }

    /**
     * Returns the text of the 1-based [line], or `""` if out of bounds.
     */
    fun getLineText(line: Int, includeLineBreak: Boolean = false): String {
        val range = getLineRange(line, includeLineBreak)
        return if (range.isValid) getText(range) else ""
    }

    /**
     * Returns the schema slot that [nodeIndex] occupies in its parent, or [NO_SLOT] (-1).
     */
    fun getNodeSlotInParent(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        val edgeIdx = nodeOrderInParent[nodeIndex]
        return if (edgeIdx >= 0) childSlots[edgeIdx].toInt() else NO_SLOT.toInt()
    }

    /**
     * Returns the previous sibling node index in $O(1)$ time, or [NULL_NODE_INDEX] if first/root.
     */
    fun getPrevSibling(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        val parentIdx = nodeParents[nodeIndex]
        if (parentIdx == NULL_NODE_INDEX) return NULL_NODE_INDEX
        val edgeIdx = nodeOrderInParent[nodeIndex]
        val parentFirstEdge = nodeChildOffsets[parentIdx]
        return if (edgeIdx > parentFirstEdge) childNodeIndices[edgeIdx - 1] else NULL_NODE_INDEX
    }

    /**
     * Returns the next sibling node index in $O(1)$ time, or [NULL_NODE_INDEX] if last/root.
     */
    fun getNextSibling(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        val parentIdx = nodeParents[nodeIndex]
        if (parentIdx == NULL_NODE_INDEX) return NULL_NODE_INDEX
        val edgeIdx = nodeOrderInParent[nodeIndex]
        val parentEndEdge = nodeChildOffsets[parentIdx + 1]
        return if (edgeIdx >= 0 && edgeIdx + 1 < parentEndEdge) childNodeIndices[edgeIdx + 1] else NULL_NODE_INDEX
    }

    /**
     * Returns the number of direct child nodes (including tokens and trivia) for [nodeIndex].
     */
    fun getChildNodeCount(nodeIndex: Int): Int {
        checkNodeBounds(nodeIndex)
        return nodeChildOffsets[nodeIndex + 1] - nodeChildOffsets[nodeIndex]
    }

    /**
     * Returns a zero-allocation [CstNodeList] representing all direct children of [nodeIndex].
     */
    fun getChildrenList(nodeIndex: Int): CstNodeList {
        checkNodeBounds(nodeIndex)
        return CstNodeList.pack(nodeChildOffsets[nodeIndex], nodeChildOffsets[nodeIndex + 1])
    }

    /**
     * Returns the child node index at relative [index] (0 until childCount) for [nodeIndex],
     * or [NULL_NODE_INDEX] if out of bounds.
     */
    fun getChildNodeAt(nodeIndex: Int, index: Int): Int {
        checkNodeBounds(nodeIndex)
        val startEdge = nodeChildOffsets[nodeIndex]
        val count = nodeChildOffsets[nodeIndex + 1] - startEdge
        if (index !in 0..<count) return NULL_NODE_INDEX
        return childNodeIndices[startEdge + index]
    }

    /**
     * Returns the first direct child of [nodeIndex] assigned to [slot], or [NULL_NODE_INDEX].
     */
    fun getChildBySlot(nodeIndex: Int, slot: Int): Int {
        checkNodeBounds(nodeIndex)
        if (slot < 0) return NULL_NODE_INDEX
        val targetSlot = slot.toShort()
        val startEdge = nodeChildOffsets[nodeIndex]
        val endEdge = nodeChildOffsets[nodeIndex + 1]
        for (edgeIdx in startEdge..<endEdge) {
            if (childSlots[edgeIdx] == targetSlot) {
                return childNodeIndices[edgeIdx]
            }
        }
        return NULL_NODE_INDEX
    }

    /**
     * Returns the number of direct children of [nodeIndex] assigned to [slot].
     */
    fun getChildCountInSlot(nodeIndex: Int, slot: Int): Int {
        checkNodeBounds(nodeIndex)
        if (slot < 0) return 0
        val targetSlot = slot.toShort()
        val startEdge = nodeChildOffsets[nodeIndex]
        val endEdge = nodeChildOffsets[nodeIndex + 1]
        var count = 0
        for (edgeIdx in startEdge..<endEdge) {
            if (childSlots[edgeIdx] == targetSlot) {
                count++
            }
        }
        return count
    }

    /**
     * Returns the [index]-th (0-based) direct child of [nodeIndex] assigned to [slot],
     * or [NULL_NODE_INDEX] if [index] is out of bounds.
     */
    fun getChildInSlotAt(nodeIndex: Int, slot: Int, index: Int): Int {
        checkNodeBounds(nodeIndex)
        if (slot < 0 || index < 0) return NULL_NODE_INDEX
        val targetSlot = slot.toShort()
        val startEdge = nodeChildOffsets[nodeIndex]
        val endEdge = nodeChildOffsets[nodeIndex + 1]
        var current = 0
        for (edgeIdx in startEdge..<endEdge) {
            if (childSlots[edgeIdx] == targetSlot) {
                if (current == index) return childNodeIndices[edgeIdx]
                current++
            }
        }
        return NULL_NODE_INDEX
    }

    /**
     * Zero-allocation iteration over all direct children of [nodeIndex] matching [slot].
     */
    inline fun forEachChildInSlot(
        nodeIndex: Int,
        slot: Int,
        action: (childNodeIndex: Int) -> Unit,
    ) {
        val list = getChildrenList(nodeIndex)
        for (edgeIdx in list.startEdgeOffset..<list.endEdgeOffset) {
            if (getRawChildEdgeSlot(edgeIdx) == slot.toShort()) {
                action(getRawChildEdgeNode(edgeIdx))
            }
        }
    }

    @PublishedApi
    internal fun getRawChildEdgeNode(edgeIndex: Int): Int = childNodeIndices[edgeIndex]

    @PublishedApi
    internal fun getRawChildEdgeSlot(edgeIndex: Int): Short = childSlots[edgeIndex]

    /**
     * Finds the index of the root node, or [NULL_NODE_INDEX] if tree is empty.
     */
    fun getRootNodeIndex(): Int {
        if (nodeCount == 0) return NULL_NODE_INDEX
        if (nodeParents[0] == NULL_NODE_INDEX) return 0
        for (i in 0..<nodeCount) {
            if (nodeParents[i] == NULL_NODE_INDEX) return i
        }
        return 0
    }

    /**
     * Finds the deepest [CstNode] (or token) covering [offset] in $O(D \log K)$ time using
     * binary search over sorted children, where $D$ is tree depth and $K$ is branching factor.
     *
     * Handles zero-length/missing recovery nodes and EOF boundary offsets gracefully by
     * preferring non-empty real nodes over synthesized zero-length nodes.
     */
    fun nodeAtOffset(offset: Int, includeTrivia: Boolean = true): CstNode {
        var current = getRootNodeIndex()
        if (current == NULL_NODE_INDEX) return CstNode.NONE
        val rootStart = getNodeStartOffset(current)
        val rootEnd = getNodeEndOffset(current)
        if (offset < rootStart || offset > rootEnd) {
            return CstNode.NONE
        }

        while (true) {
            val startEdge = nodeChildOffsets[current]
            val endEdge = nodeChildOffsets[current + 1]
            if (startEdge == endEdge) break

            // Binary search for the first child whose endOffset > offset (or == offset at EOF / zero-length)
            var low = startEdge
            var high = endEdge - 1
            var candidateEdge = -1

            while (low <= high) {
                val mid = (low + high) ushr 1
                val childIdx = childNodeIndices[mid]
                val cStart = nodeStartOffsets[childIdx]
                val cLen = getNodeLength(childIdx)
                val cEnd = cStart + cLen

                when {
                    offset < cStart -> high = mid - 1
                    offset > cEnd -> low = mid + 1
                    offset == cEnd && cLen > 0 -> {
                        // Half-open [cStart, cEnd): look right first, but remember if at EOF
                        low = mid + 1
                    }
                    else -> {
                        candidateEdge = mid
                        break
                    }
                }
            }

            var matchedChild = NULL_NODE_INDEX

            if (candidateEdge >= 0) {
                val candIdx = childNodeIndices[candidateEdge]
                // If we landed on a zero-length/missing node, check adjacent siblings for a real covering node
                if (getNodeLength(candIdx) == 0) {
                    var rightEdge = candidateEdge + 1
                    while (rightEdge < endEdge) {
                        val rightIdx = childNodeIndices[rightEdge]
                        val rStart = nodeStartOffsets[rightIdx]
                        if (rStart > offset) break
                        val rLen = getNodeLength(rightIdx)
                        if (rLen > 0 && offset >= rStart && offset < rStart + rLen) {
                            matchedChild = rightIdx
                            break
                        }
                        rightEdge++
                    }
                    if (matchedChild == NULL_NODE_INDEX) {
                        var leftEdge = candidateEdge - 1
                        while (leftEdge >= startEdge) {
                            val leftIdx = childNodeIndices[leftEdge]
                            val lStart = nodeStartOffsets[leftIdx]
                            val lLen = getNodeLength(leftIdx)
                            val lEnd = lStart + lLen
                            if (lEnd < offset) break
                            if (lLen > 0 && offset >= lStart && offset < lEnd) {
                                matchedChild = leftIdx
                                break
                            }
                            leftEdge--
                        }
                    }
                }
                if (matchedChild == NULL_NODE_INDEX) {
                    matchedChild = candIdx
                }
            } else if (offset == getNodeEndOffset(current)) {
                // At exact end boundary (e.g., EOF): match the last non-missing child touching `offset`
                for (edge in (endEdge - 1) downTo startEdge) {
                    val childIdx = childNodeIndices[edge]
                    if (!includeTrivia && isTriviaKind(nodeKinds[childIdx])) continue
                    if (getNodeEndOffset(childIdx) == offset && !isNodeMissing(childIdx)) {
                        matchedChild = childIdx
                        break
                    }
                }
            }

            if (matchedChild == NULL_NODE_INDEX) break
            if (!includeTrivia && isTriviaKind(nodeKinds[matchedChild])) break
            current = matchedChild
        }
        return CstNode(current)
    }

    /**
     * Finds the leaf [CstToken] covering [offset], or [CstToken.NONE] if none matches.
     */
    fun tokenAtOffset(offset: Int, includeTrivia: Boolean = true): CstToken {
        val node = nodeAtOffset(offset, includeTrivia)
        return if (node.isValid && isTokenKind(nodeKinds[node.nodeIndex])) node.asToken else CstToken.NONE
    }

    /**
     * Zero-allocation Depth-First Pre-Order traversal starting at [nodeIndex].
     * Visits nodes in exact source-text appearance order.
     */
    inline fun walkDepthFirst(
        nodeIndex: Int = getRootNodeIndex(),
        includeTrivia: Boolean = true,
        action: (CstNode) -> Unit,
    ) {
        if (nodeIndex == NULL_NODE_INDEX) return
        val stack = IntArray(64)
        var sp = 0
        stack[sp++] = nodeIndex
        var activeStack = stack
        while (sp > 0) {
            val current = activeStack[--sp]
            val kind = getNodeKind(current)
            if (!includeTrivia && isTriviaKind(kind)) continue
            action(CstNode(current))
            val children = getChildrenList(current)
            val needed = sp + children.size
            if (needed > activeStack.size) {
                activeStack = activeStack.copyOf(maxOf(activeStack.size * 2, needed))
            }
            // Push in reverse so first child is popped first
            for (edgeIdx in (children.endEdgeOffset - 1) downTo children.startEdgeOffset) {
                activeStack[sp++] = getRawChildEdgeNode(edgeIdx)
            }
        }
    }

    /**
     * Returns a sequence of all descendant node indices in depth-first pre-order starting from [nodeIndex].
     */
    fun getAllDescendants(
        nodeIndex: Int,
        inclusive: Boolean = false,
        includeTrivia: Boolean = true,
    ): Sequence<Int> = sequence {
        checkNodeBounds(nodeIndex)
        val stack = ArrayDeque<Int>()
        if (inclusive) {
            stack.addLast(nodeIndex)
        } else {
            val startEdge = nodeChildOffsets[nodeIndex]
            val endEdge = nodeChildOffsets[nodeIndex + 1]
            for (edgeIdx in (endEdge - 1) downTo startEdge) {
                stack.addLast(childNodeIndices[edgeIdx])
            }
        }
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (!includeTrivia && isTriviaKind(nodeKinds[current])) continue
            yield(current)
            val startEdge = nodeChildOffsets[current]
            val endEdge = nodeChildOffsets[current + 1]
            for (edgeIdx in (endEdge - 1) downTo startEdge) {
                stack.addLast(childNodeIndices[edgeIdx])
            }
        }
    }

    /**
     * Returns a sequence of all ancestor node indices starting from [nodeIndex] up to the root.
     */
    fun getAllAncestors(nodeIndex: Int, inclusive: Boolean = false): Sequence<Int> = sequence {
        checkNodeBounds(nodeIndex)
        if (inclusive) yield(nodeIndex)
        var current = nodeParents[nodeIndex]
        while (current != NULL_NODE_INDEX) {
            yield(current)
            current = nodeParents[current]
        }
    }

    /**
     * Formats the tree as a human-readable indented string for debugging.
     */
    fun toDebugTreeString(
        includeTrivia: Boolean = true,
        nodeNameLookup: ((Int) -> String)? = null,
        tokenNameLookup: ((Int) -> String)? = null,
    ): String {
        val rootIdx = getRootNodeIndex()
        if (rootIdx == NULL_NODE_INDEX) return "<empty CST>"
        val sb = StringBuilder()

        fun appendNode(nodeIdx: Int, indent: String, isLast: Boolean) {
            val kind = nodeKinds[nodeIdx]
            val isTok = isTokenKind(kind)
            val isTriv = isTriviaKind(kind)
            val isMiss = isNodeMissing(nodeIdx)
            val slot = getNodeSlotInParent(nodeIdx)
            val slotPrefix = if (slot >= 0) "slot#$slot: " else ""
            val missingTag = if (isMiss) " <MISSING>" else ""
            val textStr = getText(nodeIdx).replace("\n", "\\n").replace("\r", "\\r")

            sb.append(indent)
            sb.append(if (isLast) "└── " else "├── ")

            if (isTok) {
                val decodedId = decodeTokenSchemaId(kind)
                val kindStr = tokenNameLookup?.invoke(decodedId)
                    ?: if (isTriv) "Trivia#$decodedId" else "Token#$decodedId"
                sb.append("$slotPrefix'$textStr' ($kindStr[$nodeIdx])$missingTag (${nodeStartOffsets[nodeIdx]}..${getNodeEndOffset(nodeIdx)})\n")
            } else {
                val kindStr = when {
                    kind == ERROR_NODE_KIND -> "ErrorNode"
                    else -> nodeNameLookup?.invoke(kind) ?: "Node#$kind"
                }
                val snippet = if (textStr.isNotBlank()) " \"${textStr.take(30)}\"" else ""
                sb.append("$slotPrefix$kindStr[$nodeIdx]$snippet$missingTag (${nodeStartOffsets[nodeIdx]}..${getNodeEndOffset(nodeIdx)})\n")
                val childIndent = indent + if (isLast) "    " else "│   "
                val startEdge = nodeChildOffsets[nodeIdx]
                val endEdge = nodeChildOffsets[nodeIdx + 1]
                val visibleChildren = buildList {
                    for (edge in startEdge..<endEdge) {
                        val cIdx = childNodeIndices[edge]
                        if (includeTrivia || !isTriviaKind(nodeKinds[cIdx])) add(cIdx)
                    }
                }
                for (i in visibleChildren.indices) {
                    appendNode(visibleChildren[i], childIndent, i == visibleChildren.lastIndex)
                }
            }
        }

        appendNode(rootIdx, "", true)
        if (diagnostics.isNotEmpty()) {
            sb.append("\nDiagnostics (${diagnostics.size}):\n")
            for (diag in diagnostics) {
                sb.append(" - [${diag.severity}] (${diag.startOffset}..${diag.endOffset}) @ node#${diag.nodeIndex} ${diag.code}: ${diag.message}\n")
            }
        }
        return sb.toString()
    }

    private fun checkNodeBounds(nodeIndex: Int) {
        if (nodeIndex !in 0..<nodeCount) {
            throw IndexOutOfBoundsException("Node index $nodeIndex is out of bounds (nodeCount=$nodeCount)")
        }
    }

    companion object {
        private val EMPTY_INT_ARRAY = IntArray(0)
        private val EMPTY_SHORT_ARRAY = ShortArray(0)
        private val EMPTY_CHILD_OFFSETS = intArrayOf(0)

        /**
         * Creates an empty [ConcreteSyntaxTree] satisfying the `nodeChildOffsets.size == nodeCount + 1` invariant.
         */
        fun empty(): ConcreteSyntaxTree = ConcreteSyntaxTree(
            nodeKinds = EMPTY_INT_ARRAY,
            nodeParents = EMPTY_INT_ARRAY,
            nodeStartOffsets = EMPTY_INT_ARRAY,
            nodeLengths = EMPTY_INT_ARRAY,
            nodeChildOffsets = EMPTY_CHILD_OFFSETS,
            childNodeIndices = EMPTY_INT_ARRAY,
            childSlots = EMPTY_SHORT_ARRAY,
            nodeOrderInParent = EMPTY_INT_ARRAY,
            sourceText = "",
            diagnostics = emptyList(),
        )

        private fun computeLineStartOffsets(text: String): IntArray {
            var count = 1
            var i = 0
            val n = text.length
            while (i < n) {
                val c = text[i]
                if (c == '\r') {
                    count++
                    if (i + 1 < n && text[i + 1] == '\n') i++
                } else if (c == '\n') {
                    count++
                }
                i++
            }
            val starts = IntArray(count)
            starts[0] = 0
            var lineIdx = 1
            i = 0
            while (i < n) {
                val c = text[i]
                if (c == '\r') {
                    if (i + 1 < n && text[i + 1] == '\n') i++
                    starts[lineIdx++] = i + 1
                } else if (c == '\n') {
                    starts[lineIdx++] = i + 1
                }
                i++
            }
            return starts
        }
    }
}
