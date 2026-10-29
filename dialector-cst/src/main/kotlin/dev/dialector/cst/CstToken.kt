package dev.dialector.cst

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
    val isError: Boolean get() = asNode.isError

    context(tree: ConcreteSyntaxTree)
    val isMissing: Boolean get() = asNode.isMissing

    context(tree: ConcreteSyntaxTree)
    val isPresent: Boolean get() = asNode.isPresent

    context(tree: ConcreteSyntaxTree)
    val document: Document get() = asNode.document

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