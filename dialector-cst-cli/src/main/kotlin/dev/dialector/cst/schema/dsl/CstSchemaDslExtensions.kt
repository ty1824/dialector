package dev.dialector.cst.schema.dsl

import dev.dialector.cst.ConcreteSyntaxBuilder
import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.CstNode
import dev.dialector.cst.CstToken
import dev.dialector.cst.NO_SLOT
import dev.dialector.cst.NULL_NODE_INDEX
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

/**
 * Formats this [ConcreteSyntaxTree] using [CstSchemaDslNodeKinds] and [CstSchemaDslTokenKinds] names.
 */
public fun ConcreteSyntaxTree.toCstSchemaDslDebugString(includeTrivia: Boolean = true): String = toDebugTreeString(
    includeTrivia = includeTrivia,
    nodeNameLookup = CstSchemaDslNodeKinds::getKindName,
    tokenNameLookup = CstSchemaDslTokenKinds::getKindName,
)

/**
 * Wraps the node index in a generic [CstNode].
 */
public fun ConcreteSyntaxTree.asCstSchemaDslNode(nodeIndex: Int): CstNode = CstNode(nodeIndex)

/**
 * Returns the root node wrapped as a [CstNode], or [CstNode.NONE] if the tree is empty.
 */
public val ConcreteSyntaxTree.rootCstSchemaDslNode: CstNode
    get() = root

/**
 * Wraps the token index in a generic [CstToken].
 */
public fun ConcreteSyntaxTree.asCstSchemaDslToken(tokenIndex: Int): CstToken = CstToken(tokenIndex)

/**
 * Adds a [SchemaFileHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addSchemaFile(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): SchemaFileHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.SCHEMA_FILE,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return SchemaFileHandle(idx)
}

/**
 * Adds a missing [SchemaFileHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaFile(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaFileHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.SCHEMA_FILE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaFileHandle(idx)
}

/**
 * Adds a [SchemaHeaderHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addSchemaHeader(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): SchemaHeaderHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.SCHEMA_HEADER,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return SchemaHeaderHandle(idx)
}

/**
 * Adds a missing [SchemaHeaderHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaHeader(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaHeaderHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.SCHEMA_HEADER,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaHeaderHandle(idx)
}

/**
 * Adds a [PackageHeaderHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addPackageHeader(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): PackageHeaderHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.PACKAGE_HEADER,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return PackageHeaderHandle(idx)
}

/**
 * Adds a missing [PackageHeaderHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPackageHeader(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PackageHeaderHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.PACKAGE_HEADER,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PackageHeaderHandle(idx)
}

/**
 * Adds a [QualifiedNameHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addQualifiedName(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): QualifiedNameHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.QUALIFIED_NAME,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return QualifiedNameHandle(idx)
}

/**
 * Adds a missing [QualifiedNameHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingQualifiedName(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): QualifiedNameHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.QUALIFIED_NAME,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return QualifiedNameHandle(idx)
}

/**
 * Adds a [TokenDeclHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenDeclHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenDeclHandle(idx)
}

/**
 * Adds a missing [TokenDeclHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenDeclHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenDeclHandle(idx)
}

/**
 * Adds a [TokenCategoryDeclHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenCategoryDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenCategoryDeclHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenCategoryDeclHandle(idx)
}

/**
 * Adds a missing [TokenCategoryDeclHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenCategoryDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenCategoryDeclHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenCategoryDeclHandle(idx)
}

/**
 * Adds a [TokenMemberRefHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenMemberRef(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenMemberRefHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenMemberRefHandle(idx)
}

/**
 * Adds a missing [TokenMemberRefHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenMemberRef(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenMemberRefHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenMemberRefHandle(idx)
}

/**
 * Adds a [CategoryDeclHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addCategoryDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): CategoryDeclHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.CATEGORY_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return CategoryDeclHandle(idx)
}

/**
 * Adds a missing [CategoryDeclHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryDeclHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.CATEGORY_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryDeclHandle(idx)
}

/**
 * Adds a [NodeDeclHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addNodeDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): NodeDeclHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.NODE_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return NodeDeclHandle(idx)
}

/**
 * Adds a missing [NodeDeclHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNodeDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NodeDeclHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.NODE_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NodeDeclHandle(idx)
}

/**
 * Adds a [CategoryClauseHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addCategoryClause(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): CategoryClauseHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.CATEGORY_CLAUSE,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return CategoryClauseHandle(idx)
}

/**
 * Adds a missing [CategoryClauseHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryClause(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryClauseHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.CATEGORY_CLAUSE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryClauseHandle(idx)
}

/**
 * Adds a [FieldDeclHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addFieldDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): FieldDeclHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.FIELD_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return FieldDeclHandle(idx)
}

/**
 * Adds a missing [FieldDeclHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingFieldDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): FieldDeclHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.FIELD_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return FieldDeclHandle(idx)
}

/**
 * Adds a [NamedTypeExprHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addNamedTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): NamedTypeExprHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return NamedTypeExprHandle(idx)
}

/**
 * Adds a missing [NamedTypeExprHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNamedTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NamedTypeExprHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NamedTypeExprHandle(idx)
}

/**
 * Adds a [LiteralTokenTypeExprHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addLiteralTokenTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): LiteralTokenTypeExprHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return LiteralTokenTypeExprHandle(idx)
}

/**
 * Adds a missing [LiteralTokenTypeExprHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLiteralTokenTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LiteralTokenTypeExprHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LiteralTokenTypeExprHandle(idx)
}

/**
 * Adds a [InlineUnionTypeExprHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addInlineUnionTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): InlineUnionTypeExprHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return InlineUnionTypeExprHandle(idx)
}

/**
 * Adds a missing [InlineUnionTypeExprHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingInlineUnionTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): InlineUnionTypeExprHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return InlineUnionTypeExprHandle(idx)
}

/**
 * Adds a [AnnotationHandle] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addAnnotation(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): AnnotationHandle {
    val idx = node(
                kind = CstSchemaDslNodeKinds.ANNOTATION,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return AnnotationHandle(idx)
}

/**
 * Adds a missing [AnnotationHandle] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingAnnotation(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): AnnotationHandle {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.ANNOTATION,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return AnnotationHandle(idx)
}

/**
 * Adds a [WhitespaceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addWhitespaceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): WhitespaceTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.WHITESPACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return WhitespaceTokenHandle(idx)
}

/**
 * Adds a missing [WhitespaceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingWhitespaceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): WhitespaceTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.WHITESPACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return WhitespaceTokenHandle(idx)
}

/**
 * Adds a [LineCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLineCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LineCommentTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.LINE_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return LineCommentTokenHandle(idx)
}

/**
 * Adds a missing [LineCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLineCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LineCommentTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.LINE_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LineCommentTokenHandle(idx)
}

/**
 * Adds a [BlockCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addBlockCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): BlockCommentTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.BLOCK_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return BlockCommentTokenHandle(idx)
}

/**
 * Adds a missing [BlockCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingBlockCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): BlockCommentTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.BLOCK_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return BlockCommentTokenHandle(idx)
}

/**
 * Adds a [DocCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addDocCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): DocCommentTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOC_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return DocCommentTokenHandle(idx)
}

/**
 * Adds a missing [DocCommentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingDocCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DocCommentTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOC_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DocCommentTokenHandle(idx)
}

/**
 * Adds a [IdentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addIdentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): IdentTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return IdentTokenHandle(idx)
}

/**
 * Adds a missing [IdentTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingIdentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IdentTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IdentTokenHandle(idx)
}

/**
 * Adds a [StringLiteralTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addStringLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): StringLiteralTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.STRING_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return StringLiteralTokenHandle(idx)
}

/**
 * Adds a missing [StringLiteralTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingStringLiteralToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StringLiteralTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.STRING_LITERAL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StringLiteralTokenHandle(idx)
}

/**
 * Adds a [IntLiteralTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addIntLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): IntLiteralTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.INT_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return IntLiteralTokenHandle(idx)
}

/**
 * Adds a missing [IntLiteralTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingIntLiteralToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IntLiteralTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.INT_LITERAL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IntLiteralTokenHandle(idx)
}

/**
 * Adds a [SchemaKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addSchemaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): SchemaKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.SCHEMA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return SchemaKwTokenHandle(idx)
}

/**
 * Adds a missing [SchemaKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.SCHEMA_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaKwTokenHandle(idx)
}

/**
 * Adds a [PackageKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPackageKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PackageKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PACKAGE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PackageKwTokenHandle(idx)
}

/**
 * Adds a missing [PackageKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPackageKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PackageKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PACKAGE_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PackageKwTokenHandle(idx)
}

/**
 * Adds a [TriviaKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addTriviaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): TriviaKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.TRIVIA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return TriviaKwTokenHandle(idx)
}

/**
 * Adds a missing [TriviaKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTriviaKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TriviaKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.TRIVIA_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TriviaKwTokenHandle(idx)
}

/**
 * Adds a [TokenKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addTokenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): TokenKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.TOKEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return TokenKwTokenHandle(idx)
}

/**
 * Adds a missing [TokenKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.TOKEN_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenKwTokenHandle(idx)
}

/**
 * Adds a [OpenKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addOpenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): OpenKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.OPEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return OpenKwTokenHandle(idx)
}

/**
 * Adds a missing [OpenKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingOpenKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): OpenKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.OPEN_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return OpenKwTokenHandle(idx)
}

/**
 * Adds a [CategoryKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addCategoryKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): CategoryKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.CATEGORY_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return CategoryKwTokenHandle(idx)
}

/**
 * Adds a missing [CategoryKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.CATEGORY_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryKwTokenHandle(idx)
}

/**
 * Adds a [NodeKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addNodeKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): NodeKwTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.NODE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return NodeKwTokenHandle(idx)
}

/**
 * Adds a missing [NodeKwTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNodeKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NodeKwTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.NODE_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NodeKwTokenHandle(idx)
}

/**
 * Adds a [ColonTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addColonToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): ColonTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.COLON,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return ColonTokenHandle(idx)
}

/**
 * Adds a missing [ColonTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingColonToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): ColonTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.COLON,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return ColonTokenHandle(idx)
}

/**
 * Adds a [EqTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addEqToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): EqTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.EQ,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return EqTokenHandle(idx)
}

/**
 * Adds a missing [EqTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingEqToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): EqTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.EQ,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return EqTokenHandle(idx)
}

/**
 * Adds a [PipeTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPipeToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PipeTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PIPE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PipeTokenHandle(idx)
}

/**
 * Adds a missing [PipeTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPipeToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PipeTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PIPE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PipeTokenHandle(idx)
}

/**
 * Adds a [QuestionTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addQuestionToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): QuestionTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.QUESTION,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return QuestionTokenHandle(idx)
}

/**
 * Adds a missing [QuestionTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingQuestionToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): QuestionTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.QUESTION,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return QuestionTokenHandle(idx)
}

/**
 * Adds a [StarTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addStarToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): StarTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.STAR,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return StarTokenHandle(idx)
}

/**
 * Adds a missing [StarTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingStarToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StarTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.STAR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StarTokenHandle(idx)
}

/**
 * Adds a [PlusTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPlusToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PlusTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PLUS,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PlusTokenHandle(idx)
}

/**
 * Adds a missing [PlusTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPlusToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PlusTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PLUS,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PlusTokenHandle(idx)
}

/**
 * Adds a [AtTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addAtToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): AtTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.AT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return AtTokenHandle(idx)
}

/**
 * Adds a missing [AtTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingAtToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): AtTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.AT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return AtTokenHandle(idx)
}

/**
 * Adds a [LParenTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LParenTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return LParenTokenHandle(idx)
}

/**
 * Adds a missing [LParenTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLParenToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LParenTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_PAREN,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LParenTokenHandle(idx)
}

/**
 * Adds a [RParenTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addRParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): RParenTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return RParenTokenHandle(idx)
}

/**
 * Adds a missing [RParenTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingRParenToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RParenTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_PAREN,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RParenTokenHandle(idx)
}

/**
 * Adds a [LBraceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LBraceTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return LBraceTokenHandle(idx)
}

/**
 * Adds a missing [LBraceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLBraceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LBraceTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_BRACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LBraceTokenHandle(idx)
}

/**
 * Adds a [RBraceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addRBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): RBraceTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return RBraceTokenHandle(idx)
}

/**
 * Adds a missing [RBraceTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingRBraceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RBraceTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_BRACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RBraceTokenHandle(idx)
}

/**
 * Adds a [CommaTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addCommaToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): CommaTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.COMMA,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return CommaTokenHandle(idx)
}

/**
 * Adds a missing [CommaTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCommaToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CommaTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.COMMA,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CommaTokenHandle(idx)
}

/**
 * Adds a [DotTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addDotToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): DotTokenHandle {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return DotTokenHandle(idx)
}

/**
 * Adds a missing [DotTokenHandle] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingDotToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DotTokenHandle {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DotTokenHandle(idx)
}
