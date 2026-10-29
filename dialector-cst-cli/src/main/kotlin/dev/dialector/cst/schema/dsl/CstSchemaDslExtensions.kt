package dev.dialector.cst.schema.dsl

import dev.dialector.cst.ConcreteSyntaxBuilder
import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.CstDiagnostic
import dev.dialector.cst.CstNode
import dev.dialector.cst.CstToken
import dev.dialector.cst.Document
import dev.dialector.cst.NO_SLOT
import dev.dialector.cst.NULL_NODE_INDEX
import dev.dialector.cst.buildCst
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

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
 * Builds a [ConcreteSyntaxTree] configured with [CstSchemaDslSchema] schema metadata and [Document].
 */
public inline fun buildCstSchemaDslCst(
    document: Document,
    checkCycles: Boolean = false,
    block: ConcreteSyntaxBuilder.() -> Unit,
): ConcreteSyntaxTree = buildCst(document = document, checkCycles = checkCycles, schema = CstSchemaDslSchema, block = block)

/**
 * Builds a [ConcreteSyntaxTree] configured with [CstSchemaDslSchema] schema metadata.
 */
public inline fun buildCstSchemaDslCst(
    sourceText: String = "",
    checkCycles: Boolean = false,
    block: ConcreteSyntaxBuilder.() -> Unit,
): ConcreteSyntaxTree = buildCst(sourceText = sourceText, checkCycles = checkCycles, schema = CstSchemaDslSchema, block = block)

/**
 * Adds a [SchemaFileSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addSchemaFile(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): SchemaFileSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.SCHEMA_FILE,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return SchemaFileSyntax(idx)
}

/**
 * Adds a missing [SchemaFileSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaFile(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaFileSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.SCHEMA_FILE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaFileSyntax(idx)
}

/**
 * Adds a [SchemaHeaderSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addSchemaHeader(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): SchemaHeaderSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.SCHEMA_HEADER,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return SchemaHeaderSyntax(idx)
}

/**
 * Adds a missing [SchemaHeaderSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaHeader(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaHeaderSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.SCHEMA_HEADER,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaHeaderSyntax(idx)
}

/**
 * Adds a [PackageHeaderSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addPackageHeader(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): PackageHeaderSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.PACKAGE_HEADER,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return PackageHeaderSyntax(idx)
}

/**
 * Adds a missing [PackageHeaderSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPackageHeader(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PackageHeaderSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.PACKAGE_HEADER,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PackageHeaderSyntax(idx)
}

/**
 * Adds a [QualifiedNameSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addQualifiedName(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): QualifiedNameSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.QUALIFIED_NAME,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return QualifiedNameSyntax(idx)
}

/**
 * Adds a missing [QualifiedNameSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingQualifiedName(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): QualifiedNameSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.QUALIFIED_NAME,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return QualifiedNameSyntax(idx)
}

/**
 * Adds a [TokenDeclSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenDeclSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenDeclSyntax(idx)
}

/**
 * Adds a missing [TokenDeclSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenDeclSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenDeclSyntax(idx)
}

/**
 * Adds a [TokenCategoryDeclSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenCategoryDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenCategoryDeclSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenCategoryDeclSyntax(idx)
}

/**
 * Adds a missing [TokenCategoryDeclSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenCategoryDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenCategoryDeclSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenCategoryDeclSyntax(idx)
}

/**
 * Adds a [TokenMemberRefSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addTokenMemberRef(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): TokenMemberRefSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return TokenMemberRefSyntax(idx)
}

/**
 * Adds a missing [TokenMemberRefSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenMemberRef(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenMemberRefSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenMemberRefSyntax(idx)
}

/**
 * Adds a [CategoryDeclSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addCategoryDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): CategoryDeclSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.CATEGORY_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return CategoryDeclSyntax(idx)
}

/**
 * Adds a missing [CategoryDeclSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryDeclSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.CATEGORY_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryDeclSyntax(idx)
}

/**
 * Adds a [NodeDeclSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addNodeDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): NodeDeclSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.NODE_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return NodeDeclSyntax(idx)
}

/**
 * Adds a missing [NodeDeclSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNodeDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NodeDeclSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.NODE_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NodeDeclSyntax(idx)
}

/**
 * Adds a [CategoryClauseSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addCategoryClause(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): CategoryClauseSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.CATEGORY_CLAUSE,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return CategoryClauseSyntax(idx)
}

/**
 * Adds a missing [CategoryClauseSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryClause(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryClauseSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.CATEGORY_CLAUSE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryClauseSyntax(idx)
}

/**
 * Adds a [FieldDeclSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addFieldDecl(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): FieldDeclSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.FIELD_DECL,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return FieldDeclSyntax(idx)
}

/**
 * Adds a missing [FieldDeclSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingFieldDecl(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): FieldDeclSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.FIELD_DECL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return FieldDeclSyntax(idx)
}

/**
 * Adds a [NamedTypeExprSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addNamedTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): NamedTypeExprSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return NamedTypeExprSyntax(idx)
}

/**
 * Adds a missing [NamedTypeExprSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNamedTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NamedTypeExprSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NamedTypeExprSyntax(idx)
}

/**
 * Adds a [LiteralTokenTypeExprSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addLiteralTokenTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): LiteralTokenTypeExprSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return LiteralTokenTypeExprSyntax(idx)
}

/**
 * Adds a missing [LiteralTokenTypeExprSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLiteralTokenTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LiteralTokenTypeExprSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LiteralTokenTypeExprSyntax(idx)
}

/**
 * Adds a [InlineUnionTypeExprSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addInlineUnionTypeExpr(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): InlineUnionTypeExprSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return InlineUnionTypeExprSyntax(idx)
}

/**
 * Adds a missing [InlineUnionTypeExprSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingInlineUnionTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): InlineUnionTypeExprSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return InlineUnionTypeExprSyntax(idx)
}

/**
 * Adds a [AnnotationSyntax] node to this [ConcreteSyntaxBuilder].
 */
public inline fun ConcreteSyntaxBuilder.addAnnotation(
    startOffset: Int = -1,
    endOffset: Int = -1,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    block: (nodeIndex: Int) -> Unit = {},
): AnnotationSyntax {
    val idx = node(
                kind = CstSchemaDslNodeKinds.ANNOTATION,
                parentNode = parentNode,
                slot = slot,
                startOffset = startOffset,
                endOffset = endOffset,
                block = block,
            )
    return AnnotationSyntax(idx)
}

/**
 * Adds a missing [AnnotationSyntax] recovery node to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingAnnotation(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): AnnotationSyntax {
    val idx = addMissingNode(
                kind = CstSchemaDslNodeKinds.ANNOTATION,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return AnnotationSyntax(idx)
}

/**
 * Marks a [DeclarationSyntax] slot as unavailable (`isError == true`) when recovery could not establish a usable binding.
 */
public fun ConcreteSyntaxBuilder.addUnavailableDeclaration(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    endOffset: Int = offset,
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DeclarationSyntax {
    val idx = addUnavailableNode(
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                endOffset = endOffset,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DeclarationSyntax(idx)
}

/**
 * Marks a [TypeExprSyntax] slot as unavailable (`isError == true`) when recovery could not establish a usable binding.
 */
public fun ConcreteSyntaxBuilder.addUnavailableTypeExpr(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    endOffset: Int = offset,
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TypeExprSyntax {
    val idx = addUnavailableNode(
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                endOffset = endOffset,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TypeExprSyntax(idx)
}

/**
 * Adds a [WhitespaceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addWhitespaceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): WhitespaceToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.WHITESPACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return WhitespaceToken(idx)
}

/**
 * Adds a missing [WhitespaceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingWhitespaceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): WhitespaceToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.WHITESPACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return WhitespaceToken(idx)
}

/**
 * Adds a [LineCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLineCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LineCommentToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.LINE_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return LineCommentToken(idx)
}

/**
 * Adds a missing [LineCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLineCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LineCommentToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.LINE_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LineCommentToken(idx)
}

/**
 * Adds a [BlockCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addBlockCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): BlockCommentToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.BLOCK_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return BlockCommentToken(idx)
}

/**
 * Adds a missing [BlockCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingBlockCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): BlockCommentToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.BLOCK_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return BlockCommentToken(idx)
}

/**
 * Adds a [DocCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addDocCommentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): DocCommentToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOC_COMMENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = true,
            )
    return DocCommentToken(idx)
}

/**
 * Adds a missing [DocCommentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingDocCommentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DocCommentToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOC_COMMENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DocCommentToken(idx)
}

/**
 * Adds a [IdentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addIdentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): IdentToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return IdentToken(idx)
}

/**
 * Adds a missing [IdentToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingIdentToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IdentToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IdentToken(idx)
}

/**
 * Adds a [IdentToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorIdentToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IdentToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IdentToken(idx)
}

/**
 * Adds a [StringLiteralToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addStringLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): StringLiteralToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.STRING_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return StringLiteralToken(idx)
}

/**
 * Adds a missing [StringLiteralToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingStringLiteralToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StringLiteralToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.STRING_LITERAL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StringLiteralToken(idx)
}

/**
 * Adds a [StringLiteralToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorStringLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StringLiteralToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.STRING_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StringLiteralToken(idx)
}

/**
 * Adds a [IntLiteralToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addIntLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): IntLiteralToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.INT_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return IntLiteralToken(idx)
}

/**
 * Adds a missing [IntLiteralToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingIntLiteralToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IntLiteralToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.INT_LITERAL,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IntLiteralToken(idx)
}

/**
 * Adds a [IntLiteralToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorIntLiteralToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): IntLiteralToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.INT_LITERAL,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return IntLiteralToken(idx)
}

/**
 * Adds a [SchemaKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addSchemaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): SchemaKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.SCHEMA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return SchemaKwToken(idx)
}

/**
 * Adds a missing [SchemaKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingSchemaKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.SCHEMA_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaKwToken(idx)
}

/**
 * Adds a [SchemaKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorSchemaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): SchemaKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.SCHEMA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return SchemaKwToken(idx)
}

/**
 * Adds a [PackageKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPackageKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PackageKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PACKAGE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PackageKwToken(idx)
}

/**
 * Adds a missing [PackageKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPackageKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PackageKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PACKAGE_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PackageKwToken(idx)
}

/**
 * Adds a [PackageKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorPackageKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PackageKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.PACKAGE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PackageKwToken(idx)
}

/**
 * Adds a [TriviaKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addTriviaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): TriviaKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.TRIVIA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return TriviaKwToken(idx)
}

/**
 * Adds a missing [TriviaKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTriviaKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TriviaKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.TRIVIA_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TriviaKwToken(idx)
}

/**
 * Adds a [TriviaKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorTriviaKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TriviaKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.TRIVIA_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TriviaKwToken(idx)
}

/**
 * Adds a [TokenKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addTokenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): TokenKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.TOKEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return TokenKwToken(idx)
}

/**
 * Adds a missing [TokenKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingTokenKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.TOKEN_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenKwToken(idx)
}

/**
 * Adds a [TokenKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorTokenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): TokenKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.TOKEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return TokenKwToken(idx)
}

/**
 * Adds a [OpenKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addOpenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): OpenKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.OPEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return OpenKwToken(idx)
}

/**
 * Adds a missing [OpenKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingOpenKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): OpenKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.OPEN_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return OpenKwToken(idx)
}

/**
 * Adds a [OpenKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorOpenKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): OpenKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.OPEN_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return OpenKwToken(idx)
}

/**
 * Adds a [CategoryKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addCategoryKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): CategoryKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.CATEGORY_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return CategoryKwToken(idx)
}

/**
 * Adds a missing [CategoryKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCategoryKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.CATEGORY_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryKwToken(idx)
}

/**
 * Adds a [CategoryKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorCategoryKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CategoryKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.CATEGORY_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CategoryKwToken(idx)
}

/**
 * Adds a [NodeKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addNodeKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): NodeKwToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.NODE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return NodeKwToken(idx)
}

/**
 * Adds a missing [NodeKwToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingNodeKwToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NodeKwToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.NODE_KW,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NodeKwToken(idx)
}

/**
 * Adds a [NodeKwToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorNodeKwToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): NodeKwToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.NODE_KW,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return NodeKwToken(idx)
}

/**
 * Adds a [ColonToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addColonToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): ColonToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.COLON,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return ColonToken(idx)
}

/**
 * Adds a missing [ColonToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingColonToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): ColonToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.COLON,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return ColonToken(idx)
}

/**
 * Adds a [ColonToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorColonToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): ColonToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.COLON,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return ColonToken(idx)
}

/**
 * Adds a [EqToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addEqToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): EqToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.EQ,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return EqToken(idx)
}

/**
 * Adds a missing [EqToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingEqToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): EqToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.EQ,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return EqToken(idx)
}

/**
 * Adds a [EqToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorEqToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): EqToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.EQ,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return EqToken(idx)
}

/**
 * Adds a [PipeToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPipeToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PipeToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PIPE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PipeToken(idx)
}

/**
 * Adds a missing [PipeToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPipeToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PipeToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PIPE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PipeToken(idx)
}

/**
 * Adds a [PipeToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorPipeToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PipeToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.PIPE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PipeToken(idx)
}

/**
 * Adds a [QuestionToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addQuestionToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): QuestionToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.QUESTION,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return QuestionToken(idx)
}

/**
 * Adds a missing [QuestionToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingQuestionToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): QuestionToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.QUESTION,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return QuestionToken(idx)
}

/**
 * Adds a [QuestionToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorQuestionToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): QuestionToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.QUESTION,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return QuestionToken(idx)
}

/**
 * Adds a [StarToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addStarToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): StarToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.STAR,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return StarToken(idx)
}

/**
 * Adds a missing [StarToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingStarToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StarToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.STAR,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StarToken(idx)
}

/**
 * Adds a [StarToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorStarToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): StarToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.STAR,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return StarToken(idx)
}

/**
 * Adds a [PlusToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addPlusToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): PlusToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.PLUS,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return PlusToken(idx)
}

/**
 * Adds a missing [PlusToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingPlusToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PlusToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.PLUS,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PlusToken(idx)
}

/**
 * Adds a [PlusToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorPlusToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): PlusToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.PLUS,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return PlusToken(idx)
}

/**
 * Adds a [AtToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addAtToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): AtToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.AT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return AtToken(idx)
}

/**
 * Adds a missing [AtToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingAtToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): AtToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.AT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return AtToken(idx)
}

/**
 * Adds a [AtToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorAtToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): AtToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.AT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return AtToken(idx)
}

/**
 * Adds a [LParenToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LParenToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return LParenToken(idx)
}

/**
 * Adds a missing [LParenToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLParenToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LParenToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_PAREN,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LParenToken(idx)
}

/**
 * Adds a [LParenToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorLParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LParenToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LParenToken(idx)
}

/**
 * Adds a [RParenToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addRParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): RParenToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return RParenToken(idx)
}

/**
 * Adds a missing [RParenToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingRParenToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RParenToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_PAREN,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RParenToken(idx)
}

/**
 * Adds a [RParenToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorRParenToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RParenToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_PAREN,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RParenToken(idx)
}

/**
 * Adds a [LBraceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addLBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): LBraceToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return LBraceToken(idx)
}

/**
 * Adds a missing [LBraceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingLBraceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LBraceToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_BRACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LBraceToken(idx)
}

/**
 * Adds a [LBraceToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorLBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): LBraceToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.L_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return LBraceToken(idx)
}

/**
 * Adds a [RBraceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addRBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): RBraceToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return RBraceToken(idx)
}

/**
 * Adds a missing [RBraceToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingRBraceToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RBraceToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_BRACE,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RBraceToken(idx)
}

/**
 * Adds a [RBraceToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorRBraceToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): RBraceToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.R_BRACE,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return RBraceToken(idx)
}

/**
 * Adds a [CommaToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addCommaToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): CommaToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.COMMA,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return CommaToken(idx)
}

/**
 * Adds a missing [CommaToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingCommaToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CommaToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.COMMA,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CommaToken(idx)
}

/**
 * Adds a [CommaToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorCommaToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): CommaToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.COMMA,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return CommaToken(idx)
}

/**
 * Adds a [DotToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addDotToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
): DotToken {
    val idx = addToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
    return DotToken(idx)
}

/**
 * Adds a missing [DotToken] token to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addMissingDotToken(
    offset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DotToken {
    val idx = addMissingToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOT,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DotToken(idx)
}

/**
 * Adds a [DotToken] token flagged as an error/recovery token (`isError == true`) to this [ConcreteSyntaxBuilder].
 */
public fun ConcreteSyntaxBuilder.addErrorDotToken(
    startOffset: Int,
    endOffset: Int,
    parentNode: Int = NULL_NODE_INDEX,
    slot: Int = NO_SLOT.toInt(),
    diagnosticCode: String? = null,
    diagnosticMessage: String? = null,
): DotToken {
    val idx = addErrorToken(
                schemaTokenId = CstSchemaDslTokenKinds.DOT,
                startOffset = startOffset,
                endOffset = endOffset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = diagnosticCode,
                diagnosticMessage = diagnosticMessage,
            )
    return DotToken(idx)
}

/**
 * Validates that this [ConcreteSyntaxTree] conforms to the [CstSchemaDsl] schema, checking:
 * - Recognized composite node kinds (`UNKNOWN_NODE_KIND`) and token schema IDs (`UNKNOWN_TOKEN_KIND`).
 * - Required scalar fields (`MISSING_REQUIRED_SLOT`) and required non-empty `+` list fields (`EMPTY_REQUIRED_LIST_SLOT`) when [checkRequiredFields] is `true`.
 * - Single-element enforcement on scalar slots (`DUPLICATE_SCALAR_SLOT`).
 * - Permitted child node and token kinds per slot (`INVALID_CHILD_KIND`), undefined slots (`UNKNOWN_SLOT`), and unslotted non-trivia/non-error children (`UNSLOTTED_CHILD`).
 * - Non-overlapping child spans (`OVERLAPPING_CHILDREN`) and optional contiguous source coverage (`SOURCE_GAP`) when [checkSourceGaps] is `true`.
 */
public fun ConcreteSyntaxTree.validateCstSchemaDslTree(checkRequiredFields: Boolean = true, checkSourceGaps: Boolean = false): List<CstDiagnostic> = validate(schema = CstSchemaDslSchema, checkRequiredFields = checkRequiredFields, checkSourceGaps = checkSourceGaps)
