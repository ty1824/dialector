package dev.dialector.cst.schema.dsl.gen

import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.CstCategoryDescriptor
import dev.dialector.cst.CstNode
import dev.dialector.cst.CstNodeDescriptor
import dev.dialector.cst.CstSlotCardinality
import dev.dialector.cst.CstSlotDescriptor
import dev.dialector.cst.CstToken
import dev.dialector.cst.CstTokenCategoryDescriptor
import dev.dialector.cst.CstTokenDescriptor
import dev.dialector.cst.NO_SLOT
import dev.dialector.cst.NULL_NODE_INDEX
import dev.dialector.cst.NULL_TOKEN_INDEX
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.jvm.JvmInline

/**
 * True if this node belongs to category [Declaration].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isDeclaration: Boolean
    get() = isValid && CstSchemaDslCategories.isDeclaration(kind)

@JvmInline
public value class DeclarationSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val isTokenDecl: Boolean
        get() = isValid && asNode.kind == TokenDeclSyntax.KIND

    public val asTokenDecl: TokenDeclSyntax
        get() = TokenDeclSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asTokenDeclOrNone: TokenDeclSyntax
        get() = if (isTokenDecl) TokenDeclSyntax(nodeIndex) else TokenDeclSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public val isTokenCategoryDecl: Boolean
        get() = isValid && asNode.kind == TokenCategoryDeclSyntax.KIND

    public val asTokenCategoryDecl: TokenCategoryDeclSyntax
        get() = TokenCategoryDeclSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asTokenCategoryDeclOrNone: TokenCategoryDeclSyntax
        get() = if (isTokenCategoryDecl) TokenCategoryDeclSyntax(nodeIndex) else TokenCategoryDeclSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public val isCategoryDecl: Boolean
        get() = isValid && asNode.kind == CategoryDeclSyntax.KIND

    public val asCategoryDecl: CategoryDeclSyntax
        get() = CategoryDeclSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asCategoryDeclOrNone: CategoryDeclSyntax
        get() = if (isCategoryDecl) CategoryDeclSyntax(nodeIndex) else CategoryDeclSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public val isNodeDecl: Boolean
        get() = isValid && asNode.kind == NodeDeclSyntax.KIND

    public val asNodeDecl: NodeDeclSyntax
        get() = NodeDeclSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asNodeDeclOrNone: NodeDeclSyntax
        get() = if (isNodeDecl) NodeDeclSyntax(nodeIndex) else NodeDeclSyntax.NONE

    override fun toString(): String = if (isValid) """DeclarationSyntax($nodeIndex)""" else "DeclarationSyntax.NONE"

    public companion object : CstCategoryDescriptor("Declaration", intArrayOf(
        CstSchemaDslNodeKinds.TOKEN_DECL,
        CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
        CstSchemaDslNodeKinds.CATEGORY_DECL,
        CstSchemaDslNodeKinds.NODE_DECL,
    )) {
        public val NONE: DeclarationSyntax = DeclarationSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): DeclarationSyntax = DeclarationSyntax(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstNode] as a [DeclarationSyntax].
 */
public val CstNode.asDeclaration: DeclarationSyntax
    get() = DeclarationSyntax(nodeIndex)

/**
 * Safe conversion to [DeclarationSyntax]; returns [DeclarationSyntax.NONE] if this node does not belong to [Declaration].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asDeclarationOrNone: DeclarationSyntax
    get() = if (isDeclaration) DeclarationSyntax(nodeIndex) else DeclarationSyntax.NONE

/**
 * True if this node belongs to category [TypeExpr].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTypeExpr: Boolean
    get() = isValid && CstSchemaDslCategories.isTypeExpr(kind)

@JvmInline
public value class TypeExprSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val isNamedTypeExpr: Boolean
        get() = isValid && asNode.kind == NamedTypeExprSyntax.KIND

    public val asNamedTypeExpr: NamedTypeExprSyntax
        get() = NamedTypeExprSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asNamedTypeExprOrNone: NamedTypeExprSyntax
        get() = if (isNamedTypeExpr) NamedTypeExprSyntax(nodeIndex) else NamedTypeExprSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public val isLiteralTokenTypeExpr: Boolean
        get() = isValid && asNode.kind == LiteralTokenTypeExprSyntax.KIND

    public val asLiteralTokenTypeExpr: LiteralTokenTypeExprSyntax
        get() = LiteralTokenTypeExprSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asLiteralTokenTypeExprOrNone: LiteralTokenTypeExprSyntax
        get() = if (isLiteralTokenTypeExpr) LiteralTokenTypeExprSyntax(nodeIndex) else LiteralTokenTypeExprSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public val isInlineUnionTypeExpr: Boolean
        get() = isValid && asNode.kind == InlineUnionTypeExprSyntax.KIND

    public val asInlineUnionTypeExpr: InlineUnionTypeExprSyntax
        get() = InlineUnionTypeExprSyntax(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asInlineUnionTypeExprOrNone: InlineUnionTypeExprSyntax
        get() = if (isInlineUnionTypeExpr) InlineUnionTypeExprSyntax(nodeIndex) else InlineUnionTypeExprSyntax.NONE

    override fun toString(): String = if (isValid) """TypeExprSyntax($nodeIndex)""" else "TypeExprSyntax.NONE"

    public companion object : CstCategoryDescriptor("TypeExpr", intArrayOf(
        CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
        CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
        CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
    )) {
        public val NONE: TypeExprSyntax = TypeExprSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TypeExprSyntax = TypeExprSyntax(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstNode] as a [TypeExprSyntax].
 */
public val CstNode.asTypeExpr: TypeExprSyntax
    get() = TypeExprSyntax(nodeIndex)

/**
 * Safe conversion to [TypeExprSyntax]; returns [TypeExprSyntax.NONE] if this node does not belong to [TypeExpr].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTypeExprOrNone: TypeExprSyntax
    get() = if (isTypeExpr) TypeExprSyntax(nodeIndex) else TypeExprSyntax.NONE

/**
 * True if this token belongs to token category [QuantifierOp].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isQuantifierOp: Boolean
    get() = isValid && CstSchemaDslTokenCategories.isQuantifierOp(schemaId)

/**
 * True if this node is a token belonging to token category [QuantifierOp].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isQuantifierOpToken: Boolean
    get() = isToken && CstSchemaDslTokenCategories.isQuantifierOp(kind)

@JvmInline
public value class QuantifierOpToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val isQuestion: Boolean
        get() = isValid && asToken.schemaId == QuestionToken.ID

    public val asQuestion: QuestionToken
        get() = QuestionToken(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asQuestionOrNone: QuestionToken
        get() = if (isQuestion) QuestionToken(tokenIndex) else QuestionToken.NONE

    context(tree: ConcreteSyntaxTree)
    public val isStar: Boolean
        get() = isValid && asToken.schemaId == StarToken.ID

    public val asStar: StarToken
        get() = StarToken(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asStarOrNone: StarToken
        get() = if (isStar) StarToken(tokenIndex) else StarToken.NONE

    context(tree: ConcreteSyntaxTree)
    public val isPlus: Boolean
        get() = isValid && asToken.schemaId == PlusToken.ID

    public val asPlus: PlusToken
        get() = PlusToken(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asPlusOrNone: PlusToken
        get() = if (isPlus) PlusToken(tokenIndex) else PlusToken.NONE

    override fun toString(): String = if (isValid) """QuantifierOpToken($tokenIndex)""" else "QuantifierOpToken.NONE"

    public companion object : CstTokenCategoryDescriptor("QuantifierOp", intArrayOf(
        CstSchemaDslTokenKinds.QUESTION,
        CstSchemaDslTokenKinds.STAR,
        CstSchemaDslTokenKinds.PLUS,
    )) {
        public val NONE: QuantifierOpToken = QuantifierOpToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): QuantifierOpToken = QuantifierOpToken(token.tokenIndex)

        public fun fromNode(node: CstNode): QuantifierOpToken = QuantifierOpToken(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstToken] as a [QuantifierOpToken].
 */
public val CstToken.asQuantifierOp: QuantifierOpToken
    get() = QuantifierOpToken(tokenIndex)

/**
 * Safe conversion to [QuantifierOpToken]; returns [QuantifierOpToken.NONE] if this token does not belong to [QuantifierOp].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asQuantifierOpOrNone: QuantifierOpToken
    get() = if (isQuantifierOp) QuantifierOpToken(tokenIndex) else QuantifierOpToken.NONE

/**
 * Converts this generic node handle into a [QuantifierOpToken].
 */
public val CstNode.asQuantifierOpToken: QuantifierOpToken
    get() = QuantifierOpToken(nodeIndex)

@JvmInline
public value class SchemaFileSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Optional child at slot [SLOT_SCHEMA_HEADER] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val schemaHeader: SchemaHeaderSyntax
        get() = SchemaHeaderSyntax(tree.getChildBySlot(nodeIndex, SLOT_SCHEMA_HEADER))

    /**
     * (Optional child at slot [SLOT_PACKAGE_HEADER] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val packageHeader: PackageHeaderSyntax
        get() = PackageHeaderSyntax(tree.getChildBySlot(nodeIndex, SLOT_PACKAGE_HEADER))

    /**
     * (Child list at slot [SLOT_DECLARATIONS] = 2)
     */
    public val declarations: DeclarationList
        get() = DeclarationList.pack(nodeIndex, SLOT_DECLARATIONS)

    override fun toString(): String = if (isValid) """SchemaFileSyntax($nodeIndex)""" else "SchemaFileSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.SCHEMA_FILE, "SchemaFile", arrayOf(
        CstSlotDescriptor(slot = 0, name = "schemaHeader", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "SchemaHeader", allowedIds = intArrayOf(CstSchemaDslNodeKinds.SCHEMA_HEADER)),
        CstSlotDescriptor(slot = 1, name = "packageHeader", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "PackageHeader", allowedIds = intArrayOf(CstSchemaDslNodeKinds.PACKAGE_HEADER)),
        CstSlotDescriptor(slot = 2, name = "declarations", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "Declaration", allowedIds = intArrayOf(CstSchemaDslNodeKinds.TOKEN_DECL, CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL, CstSchemaDslNodeKinds.CATEGORY_DECL, CstSchemaDslNodeKinds.NODE_DECL)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.SCHEMA_FILE

        public const val SLOT_SCHEMA_HEADER: Int = 0

        public const val SLOT_PACKAGE_HEADER: Int = 1

        public const val SLOT_DECLARATIONS: Int = 2

        public val NONE: SchemaFileSyntax = SchemaFileSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): SchemaFileSyntax = SchemaFileSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [SchemaFileSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isSchemaFile: Boolean
    get() = isValid && kind == SchemaFileSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [SchemaFileSyntax].
 */
public val CstNode.asSchemaFile: SchemaFileSyntax
    get() = SchemaFileSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [SchemaFileSyntax], returning [SchemaFileSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asSchemaFileOrNone: SchemaFileSyntax
    get() = if (isSchemaFile) SchemaFileSyntax(nodeIndex) else SchemaFileSyntax.NONE

/**
 * Converts this generic node handle into a [SchemaFileSyntax].
 */
public fun CstNode.toSchemaFile(): SchemaFileSyntax = SchemaFileSyntax(nodeIndex)

@JvmInline
public value class SchemaHeaderSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_SCHEMA_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val schemaKw: SchemaKwToken
        get() = SchemaKwToken(tree.getChildBySlot(nodeIndex, SLOT_SCHEMA_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """SchemaHeaderSyntax($nodeIndex)""" else "SchemaHeaderSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.SCHEMA_HEADER, "SchemaHeader", arrayOf(
        CstSlotDescriptor(slot = 0, name = "schemaKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "SchemaKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.SCHEMA_KW)),
        CstSlotDescriptor(slot = 1, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.SCHEMA_HEADER

        public const val SLOT_SCHEMA_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public val NONE: SchemaHeaderSyntax = SchemaHeaderSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): SchemaHeaderSyntax = SchemaHeaderSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [SchemaHeaderSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isSchemaHeader: Boolean
    get() = isValid && kind == SchemaHeaderSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [SchemaHeaderSyntax].
 */
public val CstNode.asSchemaHeader: SchemaHeaderSyntax
    get() = SchemaHeaderSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [SchemaHeaderSyntax], returning [SchemaHeaderSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asSchemaHeaderOrNone: SchemaHeaderSyntax
    get() = if (isSchemaHeader) SchemaHeaderSyntax(nodeIndex) else SchemaHeaderSyntax.NONE

/**
 * Converts this generic node handle into a [SchemaHeaderSyntax].
 */
public fun CstNode.toSchemaHeader(): SchemaHeaderSyntax = SchemaHeaderSyntax(nodeIndex)

@JvmInline
public value class PackageHeaderSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_PACKAGE_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val packageKw: PackageKwToken
        get() = PackageKwToken(tree.getChildBySlot(nodeIndex, SLOT_PACKAGE_KW))

    /**
     * (Mandatory child (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: QualifiedNameSyntax
        get() = QualifiedNameSyntax(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """PackageHeaderSyntax($nodeIndex)""" else "PackageHeaderSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.PACKAGE_HEADER, "PackageHeader", arrayOf(
        CstSlotDescriptor(slot = 0, name = "packageKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "PackageKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.PACKAGE_KW)),
        CstSlotDescriptor(slot = 1, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = false, targetTypeName = "QualifiedName", allowedIds = intArrayOf(CstSchemaDslNodeKinds.QUALIFIED_NAME)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.PACKAGE_HEADER

        public const val SLOT_PACKAGE_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public val NONE: PackageHeaderSyntax = PackageHeaderSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): PackageHeaderSyntax = PackageHeaderSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [PackageHeaderSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isPackageHeader: Boolean
    get() = isValid && kind == PackageHeaderSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [PackageHeaderSyntax].
 */
public val CstNode.asPackageHeader: PackageHeaderSyntax
    get() = PackageHeaderSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [PackageHeaderSyntax], returning [PackageHeaderSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asPackageHeaderOrNone: PackageHeaderSyntax
    get() = if (isPackageHeader) PackageHeaderSyntax(nodeIndex) else PackageHeaderSyntax.NONE

/**
 * Converts this generic node handle into a [PackageHeaderSyntax].
 */
public fun CstNode.toPackageHeader(): PackageHeaderSyntax = PackageHeaderSyntax(nodeIndex)

@JvmInline
public value class QualifiedNameSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Child list at slot [SLOT_PARTS] = 0)
     */
    public val parts: IdentTokenList
        get() = IdentTokenList.pack(nodeIndex, SLOT_PARTS)

    /**
     * (Child list at slot [SLOT_DOTS] = 1)
     */
    public val dots: DotTokenList
        get() = DotTokenList.pack(nodeIndex, SLOT_DOTS)

    override fun toString(): String = if (isValid) """QualifiedNameSyntax($nodeIndex)""" else "QualifiedNameSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.QUALIFIED_NAME, "QualifiedName", arrayOf(
        CstSlotDescriptor(slot = 0, name = "parts", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 1, name = "dots", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Dot", allowedIds = intArrayOf(CstSchemaDslTokenKinds.DOT)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.QUALIFIED_NAME

        public const val SLOT_PARTS: Int = 0

        public const val SLOT_DOTS: Int = 1

        public val NONE: QualifiedNameSyntax = QualifiedNameSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): QualifiedNameSyntax = QualifiedNameSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [QualifiedNameSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isQualifiedName: Boolean
    get() = isValid && kind == QualifiedNameSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [QualifiedNameSyntax].
 */
public val CstNode.asQualifiedName: QualifiedNameSyntax
    get() = QualifiedNameSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [QualifiedNameSyntax], returning [QualifiedNameSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asQualifiedNameOrNone: QualifiedNameSyntax
    get() = if (isQualifiedName) QualifiedNameSyntax(nodeIndex) else QualifiedNameSyntax.NONE

/**
 * Converts this generic node handle into a [QualifiedNameSyntax].
 */
public fun CstNode.toQualifiedName(): QualifiedNameSyntax = QualifiedNameSyntax(nodeIndex)

@JvmInline
public value class TokenDeclSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asDeclaration: DeclarationSyntax
        get() = DeclarationSyntax(nodeIndex)

    /**
     * (Optional token at slot [SLOT_TRIVIA_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val triviaKw: TriviaKwToken
        get() = TriviaKwToken(tree.getChildBySlot(nodeIndex, SLOT_TRIVIA_KW))

    /**
     * (Optional token at slot [SLOT_TOKEN_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val tokenKw: TokenKwToken
        get() = TokenKwToken(tree.getChildBySlot(nodeIndex, SLOT_TOKEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_CATEGORIES] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val categories: CategoryClauseSyntax
        get() = CategoryClauseSyntax(tree.getChildBySlot(nodeIndex, SLOT_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqToken
        get() = EqToken(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    /**
     * (Optional token at slot [SLOT_FIXED_TEXT] = 5)
     */
    context(tree: ConcreteSyntaxTree)
    public val fixedText: StringLiteralToken
        get() = StringLiteralToken(tree.getChildBySlot(nodeIndex, SLOT_FIXED_TEXT))

    /**
     * (Child list at slot [SLOT_ANNOTATIONS] = 6)
     */
    public val annotations: AnnotationList
        get() = AnnotationList.pack(nodeIndex, SLOT_ANNOTATIONS)

    override fun toString(): String = if (isValid) """TokenDeclSyntax($nodeIndex)""" else "TokenDeclSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.TOKEN_DECL, "TokenDecl", arrayOf(
        CstSlotDescriptor(slot = 0, name = "triviaKw", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "TriviaKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.TRIVIA_KW)),
        CstSlotDescriptor(slot = 1, name = "tokenKw", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "TokenKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.TOKEN_KW)),
        CstSlotDescriptor(slot = 2, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 3, name = "categories", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "CategoryClause", allowedIds = intArrayOf(CstSchemaDslNodeKinds.CATEGORY_CLAUSE)),
        CstSlotDescriptor(slot = 4, name = "eq", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "Eq", allowedIds = intArrayOf(CstSchemaDslTokenKinds.EQ)),
        CstSlotDescriptor(slot = 5, name = "fixedText", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "StringLiteral", allowedIds = intArrayOf(CstSchemaDslTokenKinds.STRING_LITERAL)),
        CstSlotDescriptor(slot = 6, name = "annotations", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "Annotation", allowedIds = intArrayOf(CstSchemaDslNodeKinds.ANNOTATION)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_DECL

        public const val SLOT_TRIVIA_KW: Int = 0

        public const val SLOT_TOKEN_KW: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_CATEGORIES: Int = 3

        public const val SLOT_EQ: Int = 4

        public const val SLOT_FIXED_TEXT: Int = 5

        public const val SLOT_ANNOTATIONS: Int = 6

        public val NONE: TokenDeclSyntax = TokenDeclSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenDeclSyntax = TokenDeclSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenDeclSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenDecl: Boolean
    get() = isValid && kind == TokenDeclSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenDeclSyntax].
 */
public val CstNode.asTokenDecl: TokenDeclSyntax
    get() = TokenDeclSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenDeclSyntax], returning [TokenDeclSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenDeclOrNone: TokenDeclSyntax
    get() = if (isTokenDecl) TokenDeclSyntax(nodeIndex) else TokenDeclSyntax.NONE

/**
 * Converts this generic node handle into a [TokenDeclSyntax].
 */
public fun CstNode.toTokenDecl(): TokenDeclSyntax = TokenDeclSyntax(nodeIndex)

@JvmInline
public value class TokenCategoryDeclSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asDeclaration: DeclarationSyntax
        get() = DeclarationSyntax(nodeIndex)

    /**
     * (Optional token at slot [SLOT_OPEN_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val openKw: OpenKwToken
        get() = OpenKwToken(tree.getChildBySlot(nodeIndex, SLOT_OPEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_TOKEN_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val tokenKw: TokenKwToken
        get() = TokenKwToken(tree.getChildBySlot(nodeIndex, SLOT_TOKEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_CATEGORY_KW] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val categoryKw: CategoryKwToken
        get() = CategoryKwToken(tree.getChildBySlot(nodeIndex, SLOT_CATEGORY_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_SUPER_CATEGORIES] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val superCategories: CategoryClauseSyntax
        get() = CategoryClauseSyntax(tree.getChildBySlot(nodeIndex, SLOT_SUPER_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 5)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqToken
        get() = EqToken(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    /**
     * (Child list at slot [SLOT_MEMBERS] = 6)
     */
    public val members: TokenMemberRefList
        get() = TokenMemberRefList.pack(nodeIndex, SLOT_MEMBERS)

    /**
     * (Child list at slot [SLOT_PIPES] = 7)
     */
    public val pipes: PipeTokenList
        get() = PipeTokenList.pack(nodeIndex, SLOT_PIPES)

    override fun toString(): String = if (isValid) """TokenCategoryDeclSyntax($nodeIndex)""" else "TokenCategoryDeclSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL, "TokenCategoryDecl", arrayOf(
        CstSlotDescriptor(slot = 0, name = "openKw", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "OpenKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.OPEN_KW)),
        CstSlotDescriptor(slot = 1, name = "tokenKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "TokenKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.TOKEN_KW)),
        CstSlotDescriptor(slot = 2, name = "categoryKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "CategoryKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.CATEGORY_KW)),
        CstSlotDescriptor(slot = 3, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 4, name = "superCategories", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "CategoryClause", allowedIds = intArrayOf(CstSchemaDslNodeKinds.CATEGORY_CLAUSE)),
        CstSlotDescriptor(slot = 5, name = "eq", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "Eq", allowedIds = intArrayOf(CstSchemaDslTokenKinds.EQ)),
        CstSlotDescriptor(slot = 6, name = "members", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "TokenMemberRef", allowedIds = intArrayOf(CstSchemaDslNodeKinds.TOKEN_MEMBER_REF)),
        CstSlotDescriptor(slot = 7, name = "pipes", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Pipe", allowedIds = intArrayOf(CstSchemaDslTokenKinds.PIPE)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL

        public const val SLOT_OPEN_KW: Int = 0

        public const val SLOT_TOKEN_KW: Int = 1

        public const val SLOT_CATEGORY_KW: Int = 2

        public const val SLOT_NAME: Int = 3

        public const val SLOT_SUPER_CATEGORIES: Int = 4

        public const val SLOT_EQ: Int = 5

        public const val SLOT_MEMBERS: Int = 6

        public const val SLOT_PIPES: Int = 7

        public val NONE: TokenCategoryDeclSyntax = TokenCategoryDeclSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenCategoryDeclSyntax = TokenCategoryDeclSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenCategoryDeclSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenCategoryDecl: Boolean
    get() = isValid && kind == TokenCategoryDeclSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenCategoryDeclSyntax].
 */
public val CstNode.asTokenCategoryDecl: TokenCategoryDeclSyntax
    get() = TokenCategoryDeclSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenCategoryDeclSyntax], returning [TokenCategoryDeclSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenCategoryDeclOrNone: TokenCategoryDeclSyntax
    get() = if (isTokenCategoryDecl) TokenCategoryDeclSyntax(nodeIndex) else TokenCategoryDeclSyntax.NONE

/**
 * Converts this generic node handle into a [TokenCategoryDeclSyntax].
 */
public fun CstNode.toTokenCategoryDecl(): TokenCategoryDeclSyntax = TokenCategoryDeclSyntax(nodeIndex)

@JvmInline
public value class TokenMemberRefSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Optional token at slot [SLOT_NAME] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional token at slot [SLOT_LITERAL] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val literal: StringLiteralToken
        get() = StringLiteralToken(tree.getChildBySlot(nodeIndex, SLOT_LITERAL))

    override fun toString(): String = if (isValid) """TokenMemberRefSyntax($nodeIndex)""" else "TokenMemberRefSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.TOKEN_MEMBER_REF, "TokenMemberRef", arrayOf(
        CstSlotDescriptor(slot = 0, name = "name", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 1, name = "literal", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "StringLiteral", allowedIds = intArrayOf(CstSchemaDslTokenKinds.STRING_LITERAL)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF

        public const val SLOT_NAME: Int = 0

        public const val SLOT_LITERAL: Int = 1

        public val NONE: TokenMemberRefSyntax = TokenMemberRefSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenMemberRefSyntax = TokenMemberRefSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenMemberRefSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenMemberRef: Boolean
    get() = isValid && kind == TokenMemberRefSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenMemberRefSyntax].
 */
public val CstNode.asTokenMemberRef: TokenMemberRefSyntax
    get() = TokenMemberRefSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenMemberRefSyntax], returning [TokenMemberRefSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenMemberRefOrNone: TokenMemberRefSyntax
    get() = if (isTokenMemberRef) TokenMemberRefSyntax(nodeIndex) else TokenMemberRefSyntax.NONE

/**
 * Converts this generic node handle into a [TokenMemberRefSyntax].
 */
public fun CstNode.toTokenMemberRef(): TokenMemberRefSyntax = TokenMemberRefSyntax(nodeIndex)

@JvmInline
public value class CategoryDeclSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asDeclaration: DeclarationSyntax
        get() = DeclarationSyntax(nodeIndex)

    /**
     * (Optional token at slot [SLOT_OPEN_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val openKw: OpenKwToken
        get() = OpenKwToken(tree.getChildBySlot(nodeIndex, SLOT_OPEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_CATEGORY_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val categoryKw: CategoryKwToken
        get() = CategoryKwToken(tree.getChildBySlot(nodeIndex, SLOT_CATEGORY_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_SUPER_CATEGORIES] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val superCategories: CategoryClauseSyntax
        get() = CategoryClauseSyntax(tree.getChildBySlot(nodeIndex, SLOT_SUPER_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqToken
        get() = EqToken(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    /**
     * (Child list at slot [SLOT_MEMBERS] = 5)
     */
    public val members: IdentTokenList
        get() = IdentTokenList.pack(nodeIndex, SLOT_MEMBERS)

    /**
     * (Child list at slot [SLOT_PIPES] = 6)
     */
    public val pipes: PipeTokenList
        get() = PipeTokenList.pack(nodeIndex, SLOT_PIPES)

    override fun toString(): String = if (isValid) """CategoryDeclSyntax($nodeIndex)""" else "CategoryDeclSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.CATEGORY_DECL, "CategoryDecl", arrayOf(
        CstSlotDescriptor(slot = 0, name = "openKw", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "OpenKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.OPEN_KW)),
        CstSlotDescriptor(slot = 1, name = "categoryKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "CategoryKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.CATEGORY_KW)),
        CstSlotDescriptor(slot = 2, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 3, name = "superCategories", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "CategoryClause", allowedIds = intArrayOf(CstSchemaDslNodeKinds.CATEGORY_CLAUSE)),
        CstSlotDescriptor(slot = 4, name = "eq", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "Eq", allowedIds = intArrayOf(CstSchemaDslTokenKinds.EQ)),
        CstSlotDescriptor(slot = 5, name = "members", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 6, name = "pipes", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Pipe", allowedIds = intArrayOf(CstSchemaDslTokenKinds.PIPE)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.CATEGORY_DECL

        public const val SLOT_OPEN_KW: Int = 0

        public const val SLOT_CATEGORY_KW: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_SUPER_CATEGORIES: Int = 3

        public const val SLOT_EQ: Int = 4

        public const val SLOT_MEMBERS: Int = 5

        public const val SLOT_PIPES: Int = 6

        public val NONE: CategoryDeclSyntax = CategoryDeclSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): CategoryDeclSyntax = CategoryDeclSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [CategoryDeclSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isCategoryDecl: Boolean
    get() = isValid && kind == CategoryDeclSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [CategoryDeclSyntax].
 */
public val CstNode.asCategoryDecl: CategoryDeclSyntax
    get() = CategoryDeclSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [CategoryDeclSyntax], returning [CategoryDeclSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asCategoryDeclOrNone: CategoryDeclSyntax
    get() = if (isCategoryDecl) CategoryDeclSyntax(nodeIndex) else CategoryDeclSyntax.NONE

/**
 * Converts this generic node handle into a [CategoryDeclSyntax].
 */
public fun CstNode.toCategoryDecl(): CategoryDeclSyntax = CategoryDeclSyntax(nodeIndex)

@JvmInline
public value class NodeDeclSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asDeclaration: DeclarationSyntax
        get() = DeclarationSyntax(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NODE_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val nodeKw: NodeKwToken
        get() = NodeKwToken(tree.getChildBySlot(nodeIndex, SLOT_NODE_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_CATEGORIES] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val categories: CategoryClauseSyntax
        get() = CategoryClauseSyntax(tree.getChildBySlot(nodeIndex, SLOT_CATEGORIES))

    /**
     * (Child list at slot [SLOT_ANNOTATIONS] = 3)
     */
    public val annotations: AnnotationList
        get() = AnnotationList.pack(nodeIndex, SLOT_ANNOTATIONS)

    /**
     * (Optional token at slot [SLOT_L_BRACE] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val lBrace: LBraceToken
        get() = LBraceToken(tree.getChildBySlot(nodeIndex, SLOT_L_BRACE))

    /**
     * (Child list at slot [SLOT_FIELDS] = 5)
     */
    public val fields: FieldDeclList
        get() = FieldDeclList.pack(nodeIndex, SLOT_FIELDS)

    /**
     * (Optional token at slot [SLOT_R_BRACE] = 6)
     */
    context(tree: ConcreteSyntaxTree)
    public val rBrace: RBraceToken
        get() = RBraceToken(tree.getChildBySlot(nodeIndex, SLOT_R_BRACE))

    override fun toString(): String = if (isValid) """NodeDeclSyntax($nodeIndex)""" else "NodeDeclSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.NODE_DECL, "NodeDecl", arrayOf(
        CstSlotDescriptor(slot = 0, name = "nodeKw", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "NodeKw", allowedIds = intArrayOf(CstSchemaDslTokenKinds.NODE_KW)),
        CstSlotDescriptor(slot = 1, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 2, name = "categories", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = false, targetTypeName = "CategoryClause", allowedIds = intArrayOf(CstSchemaDslNodeKinds.CATEGORY_CLAUSE)),
        CstSlotDescriptor(slot = 3, name = "annotations", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "Annotation", allowedIds = intArrayOf(CstSchemaDslNodeKinds.ANNOTATION)),
        CstSlotDescriptor(slot = 4, name = "lBrace", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "LBrace", allowedIds = intArrayOf(CstSchemaDslTokenKinds.L_BRACE)),
        CstSlotDescriptor(slot = 5, name = "fields", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "FieldDecl", allowedIds = intArrayOf(CstSchemaDslNodeKinds.FIELD_DECL)),
        CstSlotDescriptor(slot = 6, name = "rBrace", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "RBrace", allowedIds = intArrayOf(CstSchemaDslTokenKinds.R_BRACE)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.NODE_DECL

        public const val SLOT_NODE_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public const val SLOT_CATEGORIES: Int = 2

        public const val SLOT_ANNOTATIONS: Int = 3

        public const val SLOT_L_BRACE: Int = 4

        public const val SLOT_FIELDS: Int = 5

        public const val SLOT_R_BRACE: Int = 6

        public val NONE: NodeDeclSyntax = NodeDeclSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): NodeDeclSyntax = NodeDeclSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [NodeDeclSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isNodeDecl: Boolean
    get() = isValid && kind == NodeDeclSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [NodeDeclSyntax].
 */
public val CstNode.asNodeDecl: NodeDeclSyntax
    get() = NodeDeclSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [NodeDeclSyntax], returning [NodeDeclSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asNodeDeclOrNone: NodeDeclSyntax
    get() = if (isNodeDecl) NodeDeclSyntax(nodeIndex) else NodeDeclSyntax.NONE

/**
 * Converts this generic node handle into a [NodeDeclSyntax].
 */
public fun CstNode.toNodeDecl(): NodeDeclSyntax = NodeDeclSyntax(nodeIndex)

@JvmInline
public value class CategoryClauseSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_COLON] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val colon: ColonToken
        get() = ColonToken(tree.getChildBySlot(nodeIndex, SLOT_COLON))

    /**
     * (Child list at slot [SLOT_NAMES] = 1)
     */
    public val names: IdentTokenList
        get() = IdentTokenList.pack(nodeIndex, SLOT_NAMES)

    /**
     * (Child list at slot [SLOT_COMMAS] = 2)
     */
    public val commas: CommaTokenList
        get() = CommaTokenList.pack(nodeIndex, SLOT_COMMAS)

    override fun toString(): String = if (isValid) """CategoryClauseSyntax($nodeIndex)""" else "CategoryClauseSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.CATEGORY_CLAUSE, "CategoryClause", arrayOf(
        CstSlotDescriptor(slot = 0, name = "colon", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Colon", allowedIds = intArrayOf(CstSchemaDslTokenKinds.COLON)),
        CstSlotDescriptor(slot = 1, name = "names", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 2, name = "commas", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Comma", allowedIds = intArrayOf(CstSchemaDslTokenKinds.COMMA)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.CATEGORY_CLAUSE

        public const val SLOT_COLON: Int = 0

        public const val SLOT_NAMES: Int = 1

        public const val SLOT_COMMAS: Int = 2

        public val NONE: CategoryClauseSyntax = CategoryClauseSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): CategoryClauseSyntax = CategoryClauseSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [CategoryClauseSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isCategoryClause: Boolean
    get() = isValid && kind == CategoryClauseSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [CategoryClauseSyntax].
 */
public val CstNode.asCategoryClause: CategoryClauseSyntax
    get() = CategoryClauseSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [CategoryClauseSyntax], returning [CategoryClauseSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asCategoryClauseOrNone: CategoryClauseSyntax
    get() = if (isCategoryClause) CategoryClauseSyntax(nodeIndex) else CategoryClauseSyntax.NONE

/**
 * Converts this generic node handle into a [CategoryClauseSyntax].
 */
public fun CstNode.toCategoryClause(): CategoryClauseSyntax = CategoryClauseSyntax(nodeIndex)

@JvmInline
public value class FieldDeclSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_COLON] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val colon: ColonToken
        get() = ColonToken(tree.getChildBySlot(nodeIndex, SLOT_COLON))

    /**
     * (Mandatory child (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_TYPE_EXPR] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val typeExpr: TypeExprSyntax
        get() = TypeExprSyntax(tree.getChildBySlot(nodeIndex, SLOT_TYPE_EXPR))

    /**
     * (Optional token at slot [SLOT_QUANTIFIER] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val quantifier: QuantifierOpToken
        get() = QuantifierOpToken(tree.getChildBySlot(nodeIndex, SLOT_QUANTIFIER))

    /**
     * (Child list at slot [SLOT_ANNOTATIONS] = 4)
     */
    public val annotations: AnnotationList
        get() = AnnotationList.pack(nodeIndex, SLOT_ANNOTATIONS)

    override fun toString(): String = if (isValid) """FieldDeclSyntax($nodeIndex)""" else "FieldDeclSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.FIELD_DECL, "FieldDecl", arrayOf(
        CstSlotDescriptor(slot = 0, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 1, name = "colon", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Colon", allowedIds = intArrayOf(CstSchemaDslTokenKinds.COLON)),
        CstSlotDescriptor(slot = 2, name = "typeExpr", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = false, targetTypeName = "TypeExpr", allowedIds = intArrayOf(CstSchemaDslNodeKinds.NAMED_TYPE_EXPR, CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR, CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR)),
        CstSlotDescriptor(slot = 3, name = "quantifier", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "QuantifierOp", allowedIds = intArrayOf(CstSchemaDslTokenKinds.QUESTION, CstSchemaDslTokenKinds.STAR, CstSchemaDslTokenKinds.PLUS)),
        CstSlotDescriptor(slot = 4, name = "annotations", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "Annotation", allowedIds = intArrayOf(CstSchemaDslNodeKinds.ANNOTATION)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.FIELD_DECL

        public const val SLOT_NAME: Int = 0

        public const val SLOT_COLON: Int = 1

        public const val SLOT_TYPE_EXPR: Int = 2

        public const val SLOT_QUANTIFIER: Int = 3

        public const val SLOT_ANNOTATIONS: Int = 4

        public val NONE: FieldDeclSyntax = FieldDeclSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): FieldDeclSyntax = FieldDeclSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [FieldDeclSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isFieldDecl: Boolean
    get() = isValid && kind == FieldDeclSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [FieldDeclSyntax].
 */
public val CstNode.asFieldDecl: FieldDeclSyntax
    get() = FieldDeclSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [FieldDeclSyntax], returning [FieldDeclSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asFieldDeclOrNone: FieldDeclSyntax
    get() = if (isFieldDecl) FieldDeclSyntax(nodeIndex) else FieldDeclSyntax.NONE

/**
 * Converts this generic node handle into a [FieldDeclSyntax].
 */
public fun CstNode.toFieldDecl(): FieldDeclSyntax = FieldDeclSyntax(nodeIndex)

@JvmInline
public value class NamedTypeExprSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asTypeExpr: TypeExprSyntax
        get() = TypeExprSyntax(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """NamedTypeExprSyntax($nodeIndex)""" else "NamedTypeExprSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.NAMED_TYPE_EXPR, "NamedTypeExpr", arrayOf(
        CstSlotDescriptor(slot = 0, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR

        public const val SLOT_NAME: Int = 0

        public val NONE: NamedTypeExprSyntax = NamedTypeExprSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): NamedTypeExprSyntax = NamedTypeExprSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [NamedTypeExprSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isNamedTypeExpr: Boolean
    get() = isValid && kind == NamedTypeExprSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [NamedTypeExprSyntax].
 */
public val CstNode.asNamedTypeExpr: NamedTypeExprSyntax
    get() = NamedTypeExprSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [NamedTypeExprSyntax], returning [NamedTypeExprSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asNamedTypeExprOrNone: NamedTypeExprSyntax
    get() = if (isNamedTypeExpr) NamedTypeExprSyntax(nodeIndex) else NamedTypeExprSyntax.NONE

/**
 * Converts this generic node handle into a [NamedTypeExprSyntax].
 */
public fun CstNode.toNamedTypeExpr(): NamedTypeExprSyntax = NamedTypeExprSyntax(nodeIndex)

@JvmInline
public value class LiteralTokenTypeExprSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asTypeExpr: TypeExprSyntax
        get() = TypeExprSyntax(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_LITERAL] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val literal: StringLiteralToken
        get() = StringLiteralToken(tree.getChildBySlot(nodeIndex, SLOT_LITERAL))

    override fun toString(): String = if (isValid) """LiteralTokenTypeExprSyntax($nodeIndex)""" else "LiteralTokenTypeExprSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR, "LiteralTokenTypeExpr", arrayOf(
        CstSlotDescriptor(slot = 0, name = "literal", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "StringLiteral", allowedIds = intArrayOf(CstSchemaDslTokenKinds.STRING_LITERAL)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR

        public const val SLOT_LITERAL: Int = 0

        public val NONE: LiteralTokenTypeExprSyntax = LiteralTokenTypeExprSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): LiteralTokenTypeExprSyntax = LiteralTokenTypeExprSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [LiteralTokenTypeExprSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isLiteralTokenTypeExpr: Boolean
    get() = isValid && kind == LiteralTokenTypeExprSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [LiteralTokenTypeExprSyntax].
 */
public val CstNode.asLiteralTokenTypeExpr: LiteralTokenTypeExprSyntax
    get() = LiteralTokenTypeExprSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [LiteralTokenTypeExprSyntax], returning [LiteralTokenTypeExprSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asLiteralTokenTypeExprOrNone: LiteralTokenTypeExprSyntax
    get() = if (isLiteralTokenTypeExpr) LiteralTokenTypeExprSyntax(nodeIndex) else LiteralTokenTypeExprSyntax.NONE

/**
 * Converts this generic node handle into a [LiteralTokenTypeExprSyntax].
 */
public fun CstNode.toLiteralTokenTypeExpr(): LiteralTokenTypeExprSyntax = LiteralTokenTypeExprSyntax(nodeIndex)

@JvmInline
public value class InlineUnionTypeExprSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    public val asTypeExpr: TypeExprSyntax
        get() = TypeExprSyntax(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_L_PAREN] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val lParen: LParenToken
        get() = LParenToken(tree.getChildBySlot(nodeIndex, SLOT_L_PAREN))

    /**
     * (Child list at slot [SLOT_MEMBERS] = 1)
     */
    public val members: TokenMemberRefList
        get() = TokenMemberRefList.pack(nodeIndex, SLOT_MEMBERS)

    /**
     * (Child list at slot [SLOT_PIPES] = 2)
     */
    public val pipes: PipeTokenList
        get() = PipeTokenList.pack(nodeIndex, SLOT_PIPES)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_R_PAREN] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val rParen: RParenToken
        get() = RParenToken(tree.getChildBySlot(nodeIndex, SLOT_R_PAREN))

    override fun toString(): String = if (isValid) """InlineUnionTypeExprSyntax($nodeIndex)""" else "InlineUnionTypeExprSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR, "InlineUnionTypeExpr", arrayOf(
        CstSlotDescriptor(slot = 0, name = "lParen", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "LParen", allowedIds = intArrayOf(CstSchemaDslTokenKinds.L_PAREN)),
        CstSlotDescriptor(slot = 1, name = "members", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = false, targetTypeName = "TokenMemberRef", allowedIds = intArrayOf(CstSchemaDslNodeKinds.TOKEN_MEMBER_REF)),
        CstSlotDescriptor(slot = 2, name = "pipes", cardinality = CstSlotCardinality.ZERO_OR_MORE, isTokenSlot = true, targetTypeName = "Pipe", allowedIds = intArrayOf(CstSchemaDslTokenKinds.PIPE)),
        CstSlotDescriptor(slot = 3, name = "rParen", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "RParen", allowedIds = intArrayOf(CstSchemaDslTokenKinds.R_PAREN)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR

        public const val SLOT_L_PAREN: Int = 0

        public const val SLOT_MEMBERS: Int = 1

        public const val SLOT_PIPES: Int = 2

        public const val SLOT_R_PAREN: Int = 3

        public val NONE: InlineUnionTypeExprSyntax = InlineUnionTypeExprSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): InlineUnionTypeExprSyntax = InlineUnionTypeExprSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [InlineUnionTypeExprSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isInlineUnionTypeExpr: Boolean
    get() = isValid && kind == InlineUnionTypeExprSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [InlineUnionTypeExprSyntax].
 */
public val CstNode.asInlineUnionTypeExpr: InlineUnionTypeExprSyntax
    get() = InlineUnionTypeExprSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [InlineUnionTypeExprSyntax], returning [InlineUnionTypeExprSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asInlineUnionTypeExprOrNone: InlineUnionTypeExprSyntax
    get() = if (isInlineUnionTypeExpr) InlineUnionTypeExprSyntax(nodeIndex) else InlineUnionTypeExprSyntax.NONE

/**
 * Converts this generic node handle into a [InlineUnionTypeExprSyntax].
 */
public fun CstNode.toInlineUnionTypeExpr(): InlineUnionTypeExprSyntax = InlineUnionTypeExprSyntax(nodeIndex)

@JvmInline
public value class AnnotationSyntax(
    public val nodeIndex: Int,
) {
    /**
     * True if this handle points to a valid node index (>= 0).
     */
    public val isValid: Boolean
        get() = nodeIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = nodeIndex < 0

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_AT] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val at: AtToken
        get() = AtToken(tree.getChildBySlot(nodeIndex, SLOT_AT))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentToken
        get() = IdentToken(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional token at slot [SLOT_L_PAREN] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val lParen: LParenToken
        get() = LParenToken(tree.getChildBySlot(nodeIndex, SLOT_L_PAREN))

    /**
     * (Optional token at slot [SLOT_ARG] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val arg: IntLiteralToken
        get() = IntLiteralToken(tree.getChildBySlot(nodeIndex, SLOT_ARG))

    /**
     * (Optional token at slot [SLOT_R_PAREN] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val rParen: RParenToken
        get() = RParenToken(tree.getChildBySlot(nodeIndex, SLOT_R_PAREN))

    override fun toString(): String = if (isValid) """AnnotationSyntax($nodeIndex)""" else "AnnotationSyntax.NONE"

    public companion object : CstNodeDescriptor(CstSchemaDslNodeKinds.ANNOTATION, "Annotation", arrayOf(
        CstSlotDescriptor(slot = 0, name = "at", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "At", allowedIds = intArrayOf(CstSchemaDslTokenKinds.AT)),
        CstSlotDescriptor(slot = 1, name = "name", cardinality = CstSlotCardinality.REQUIRED, isTokenSlot = true, targetTypeName = "Ident", allowedIds = intArrayOf(CstSchemaDslTokenKinds.IDENT)),
        CstSlotDescriptor(slot = 2, name = "lParen", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "LParen", allowedIds = intArrayOf(CstSchemaDslTokenKinds.L_PAREN)),
        CstSlotDescriptor(slot = 3, name = "arg", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "IntLiteral", allowedIds = intArrayOf(CstSchemaDslTokenKinds.INT_LITERAL)),
        CstSlotDescriptor(slot = 4, name = "rParen", cardinality = CstSlotCardinality.OPTIONAL, isTokenSlot = true, targetTypeName = "RParen", allowedIds = intArrayOf(CstSchemaDslTokenKinds.R_PAREN)),
    )) {
        public const val KIND: Int = CstSchemaDslNodeKinds.ANNOTATION

        public const val SLOT_AT: Int = 0

        public const val SLOT_NAME: Int = 1

        public const val SLOT_L_PAREN: Int = 2

        public const val SLOT_ARG: Int = 3

        public const val SLOT_R_PAREN: Int = 4

        public val NONE: AnnotationSyntax = AnnotationSyntax(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): AnnotationSyntax = AnnotationSyntax(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [AnnotationSyntax.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isAnnotation: Boolean
    get() = isValid && kind == AnnotationSyntax.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [AnnotationSyntax].
 */
public val CstNode.asAnnotation: AnnotationSyntax
    get() = AnnotationSyntax(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [AnnotationSyntax], returning [AnnotationSyntax.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asAnnotationOrNone: AnnotationSyntax
    get() = if (isAnnotation) AnnotationSyntax(nodeIndex) else AnnotationSyntax.NONE

/**
 * Converts this generic node handle into a [AnnotationSyntax].
 */
public fun CstNode.toAnnotation(): AnnotationSyntax = AnnotationSyntax(nodeIndex)

@JvmInline
public value class WhitespaceToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """WhitespaceToken($tokenIndex)""" else "WhitespaceToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.WHITESPACE, "Whitespace", CstSchemaDslTokenKinds.KIND_WHITESPACE, null, true) {
        public const val ID: Int = CstSchemaDslTokenKinds.WHITESPACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_WHITESPACE

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: WhitespaceToken = WhitespaceToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): WhitespaceToken = WhitespaceToken(token.tokenIndex)

        public fun fromNode(node: CstNode): WhitespaceToken = WhitespaceToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [WhitespaceToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isWhitespace: Boolean
    get() = isValid && schemaId == WhitespaceToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [WhitespaceToken].
 */
public val CstToken.asWhitespace: WhitespaceToken
    get() = WhitespaceToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [WhitespaceToken], returning [WhitespaceToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asWhitespaceOrNone: WhitespaceToken
    get() = if (isWhitespace) WhitespaceToken(tokenIndex) else WhitespaceToken.NONE

/**
 * Converts this generic node handle into a [WhitespaceToken].
 */
public val CstNode.asWhitespaceToken: WhitespaceToken
    get() = WhitespaceToken(nodeIndex)

/**
 * Converts this generic token handle into a [WhitespaceToken].
 */
public fun CstToken.toWhitespace(): WhitespaceToken = WhitespaceToken(tokenIndex)

@JvmInline
public value class LineCommentToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """LineCommentToken($tokenIndex)""" else "LineCommentToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.LINE_COMMENT, "LineComment", CstSchemaDslTokenKinds.KIND_LINE_COMMENT, null, true) {
        public const val ID: Int = CstSchemaDslTokenKinds.LINE_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_LINE_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: LineCommentToken = LineCommentToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LineCommentToken = LineCommentToken(token.tokenIndex)

        public fun fromNode(node: CstNode): LineCommentToken = LineCommentToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LineCommentToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLineComment: Boolean
    get() = isValid && schemaId == LineCommentToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LineCommentToken].
 */
public val CstToken.asLineComment: LineCommentToken
    get() = LineCommentToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LineCommentToken], returning [LineCommentToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLineCommentOrNone: LineCommentToken
    get() = if (isLineComment) LineCommentToken(tokenIndex) else LineCommentToken.NONE

/**
 * Converts this generic node handle into a [LineCommentToken].
 */
public val CstNode.asLineCommentToken: LineCommentToken
    get() = LineCommentToken(nodeIndex)

/**
 * Converts this generic token handle into a [LineCommentToken].
 */
public fun CstToken.toLineComment(): LineCommentToken = LineCommentToken(tokenIndex)

@JvmInline
public value class BlockCommentToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """BlockCommentToken($tokenIndex)""" else "BlockCommentToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.BLOCK_COMMENT, "BlockComment", CstSchemaDslTokenKinds.KIND_BLOCK_COMMENT, null, true) {
        public const val ID: Int = CstSchemaDslTokenKinds.BLOCK_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_BLOCK_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: BlockCommentToken = BlockCommentToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): BlockCommentToken = BlockCommentToken(token.tokenIndex)

        public fun fromNode(node: CstNode): BlockCommentToken = BlockCommentToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [BlockCommentToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isBlockComment: Boolean
    get() = isValid && schemaId == BlockCommentToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [BlockCommentToken].
 */
public val CstToken.asBlockComment: BlockCommentToken
    get() = BlockCommentToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [BlockCommentToken], returning [BlockCommentToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asBlockCommentOrNone: BlockCommentToken
    get() = if (isBlockComment) BlockCommentToken(tokenIndex) else BlockCommentToken.NONE

/**
 * Converts this generic node handle into a [BlockCommentToken].
 */
public val CstNode.asBlockCommentToken: BlockCommentToken
    get() = BlockCommentToken(nodeIndex)

/**
 * Converts this generic token handle into a [BlockCommentToken].
 */
public fun CstToken.toBlockComment(): BlockCommentToken = BlockCommentToken(tokenIndex)

@JvmInline
public value class DocCommentToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """DocCommentToken($tokenIndex)""" else "DocCommentToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.DOC_COMMENT, "DocComment", CstSchemaDslTokenKinds.KIND_DOC_COMMENT, null, true) {
        public const val ID: Int = CstSchemaDslTokenKinds.DOC_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_DOC_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: DocCommentToken = DocCommentToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): DocCommentToken = DocCommentToken(token.tokenIndex)

        public fun fromNode(node: CstNode): DocCommentToken = DocCommentToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [DocCommentToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isDocComment: Boolean
    get() = isValid && schemaId == DocCommentToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [DocCommentToken].
 */
public val CstToken.asDocComment: DocCommentToken
    get() = DocCommentToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [DocCommentToken], returning [DocCommentToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asDocCommentOrNone: DocCommentToken
    get() = if (isDocComment) DocCommentToken(tokenIndex) else DocCommentToken.NONE

/**
 * Converts this generic node handle into a [DocCommentToken].
 */
public val CstNode.asDocCommentToken: DocCommentToken
    get() = DocCommentToken(nodeIndex)

/**
 * Converts this generic token handle into a [DocCommentToken].
 */
public fun CstToken.toDocComment(): DocCommentToken = DocCommentToken(tokenIndex)

@JvmInline
public value class IdentToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """IdentToken($tokenIndex)""" else "IdentToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.IDENT, "Ident", CstSchemaDslTokenKinds.KIND_IDENT, null, false) {
        public const val ID: Int = CstSchemaDslTokenKinds.IDENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_IDENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: IdentToken = IdentToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): IdentToken = IdentToken(token.tokenIndex)

        public fun fromNode(node: CstNode): IdentToken = IdentToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [IdentToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isIdent: Boolean
    get() = isValid && schemaId == IdentToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [IdentToken].
 */
public val CstToken.asIdent: IdentToken
    get() = IdentToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [IdentToken], returning [IdentToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asIdentOrNone: IdentToken
    get() = if (isIdent) IdentToken(tokenIndex) else IdentToken.NONE

/**
 * Converts this generic node handle into a [IdentToken].
 */
public val CstNode.asIdentToken: IdentToken
    get() = IdentToken(nodeIndex)

/**
 * Converts this generic token handle into a [IdentToken].
 */
public fun CstToken.toIdent(): IdentToken = IdentToken(tokenIndex)

@JvmInline
public value class StringLiteralToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """StringLiteralToken($tokenIndex)""" else "StringLiteralToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.STRING_LITERAL, "StringLiteral", CstSchemaDslTokenKinds.KIND_STRING_LITERAL, null, false) {
        public const val ID: Int = CstSchemaDslTokenKinds.STRING_LITERAL

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_STRING_LITERAL

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: StringLiteralToken = StringLiteralToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): StringLiteralToken = StringLiteralToken(token.tokenIndex)

        public fun fromNode(node: CstNode): StringLiteralToken = StringLiteralToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [StringLiteralToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isStringLiteral: Boolean
    get() = isValid && schemaId == StringLiteralToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [StringLiteralToken].
 */
public val CstToken.asStringLiteral: StringLiteralToken
    get() = StringLiteralToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [StringLiteralToken], returning [StringLiteralToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asStringLiteralOrNone: StringLiteralToken
    get() = if (isStringLiteral) StringLiteralToken(tokenIndex) else StringLiteralToken.NONE

/**
 * Converts this generic node handle into a [StringLiteralToken].
 */
public val CstNode.asStringLiteralToken: StringLiteralToken
    get() = StringLiteralToken(nodeIndex)

/**
 * Converts this generic token handle into a [StringLiteralToken].
 */
public fun CstToken.toStringLiteral(): StringLiteralToken = StringLiteralToken(tokenIndex)

@JvmInline
public value class IntLiteralToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """IntLiteralToken($tokenIndex)""" else "IntLiteralToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.INT_LITERAL, "IntLiteral", CstSchemaDslTokenKinds.KIND_INT_LITERAL, null, false) {
        public const val ID: Int = CstSchemaDslTokenKinds.INT_LITERAL

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_INT_LITERAL

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: IntLiteralToken = IntLiteralToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): IntLiteralToken = IntLiteralToken(token.tokenIndex)

        public fun fromNode(node: CstNode): IntLiteralToken = IntLiteralToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [IntLiteralToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isIntLiteral: Boolean
    get() = isValid && schemaId == IntLiteralToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [IntLiteralToken].
 */
public val CstToken.asIntLiteral: IntLiteralToken
    get() = IntLiteralToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [IntLiteralToken], returning [IntLiteralToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asIntLiteralOrNone: IntLiteralToken
    get() = if (isIntLiteral) IntLiteralToken(tokenIndex) else IntLiteralToken.NONE

/**
 * Converts this generic node handle into a [IntLiteralToken].
 */
public val CstNode.asIntLiteralToken: IntLiteralToken
    get() = IntLiteralToken(nodeIndex)

/**
 * Converts this generic token handle into a [IntLiteralToken].
 */
public fun CstToken.toIntLiteral(): IntLiteralToken = IntLiteralToken(tokenIndex)

@JvmInline
public value class SchemaKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """SchemaKwToken($tokenIndex)""" else "SchemaKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.SCHEMA_KW, "SchemaKw", CstSchemaDslTokenKinds.KIND_SCHEMA_KW, "schema", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.SCHEMA_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_SCHEMA_KW

        public const val FIXED_TEXT: String = "schema"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: SchemaKwToken = SchemaKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): SchemaKwToken = SchemaKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): SchemaKwToken = SchemaKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [SchemaKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isSchemaKw: Boolean
    get() = isValid && schemaId == SchemaKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [SchemaKwToken].
 */
public val CstToken.asSchemaKw: SchemaKwToken
    get() = SchemaKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [SchemaKwToken], returning [SchemaKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asSchemaKwOrNone: SchemaKwToken
    get() = if (isSchemaKw) SchemaKwToken(tokenIndex) else SchemaKwToken.NONE

/**
 * Converts this generic node handle into a [SchemaKwToken].
 */
public val CstNode.asSchemaKwToken: SchemaKwToken
    get() = SchemaKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [SchemaKwToken].
 */
public fun CstToken.toSchemaKw(): SchemaKwToken = SchemaKwToken(tokenIndex)

@JvmInline
public value class PackageKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """PackageKwToken($tokenIndex)""" else "PackageKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.PACKAGE_KW, "PackageKw", CstSchemaDslTokenKinds.KIND_PACKAGE_KW, "package", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.PACKAGE_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PACKAGE_KW

        public const val FIXED_TEXT: String = "package"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PackageKwToken = PackageKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PackageKwToken = PackageKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): PackageKwToken = PackageKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PackageKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPackageKw: Boolean
    get() = isValid && schemaId == PackageKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PackageKwToken].
 */
public val CstToken.asPackageKw: PackageKwToken
    get() = PackageKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PackageKwToken], returning [PackageKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPackageKwOrNone: PackageKwToken
    get() = if (isPackageKw) PackageKwToken(tokenIndex) else PackageKwToken.NONE

/**
 * Converts this generic node handle into a [PackageKwToken].
 */
public val CstNode.asPackageKwToken: PackageKwToken
    get() = PackageKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [PackageKwToken].
 */
public fun CstToken.toPackageKw(): PackageKwToken = PackageKwToken(tokenIndex)

@JvmInline
public value class TriviaKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """TriviaKwToken($tokenIndex)""" else "TriviaKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.TRIVIA_KW, "TriviaKw", CstSchemaDslTokenKinds.KIND_TRIVIA_KW, "trivia", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.TRIVIA_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_TRIVIA_KW

        public const val FIXED_TEXT: String = "trivia"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: TriviaKwToken = TriviaKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): TriviaKwToken = TriviaKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): TriviaKwToken = TriviaKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [TriviaKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isTriviaKw: Boolean
    get() = isValid && schemaId == TriviaKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [TriviaKwToken].
 */
public val CstToken.asTriviaKw: TriviaKwToken
    get() = TriviaKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [TriviaKwToken], returning [TriviaKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asTriviaKwOrNone: TriviaKwToken
    get() = if (isTriviaKw) TriviaKwToken(tokenIndex) else TriviaKwToken.NONE

/**
 * Converts this generic node handle into a [TriviaKwToken].
 */
public val CstNode.asTriviaKwToken: TriviaKwToken
    get() = TriviaKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [TriviaKwToken].
 */
public fun CstToken.toTriviaKw(): TriviaKwToken = TriviaKwToken(tokenIndex)

@JvmInline
public value class TokenKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """TokenKwToken($tokenIndex)""" else "TokenKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.TOKEN_KW, "TokenKw", CstSchemaDslTokenKinds.KIND_TOKEN_KW, "token", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.TOKEN_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_TOKEN_KW

        public const val FIXED_TEXT: String = "token"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: TokenKwToken = TokenKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): TokenKwToken = TokenKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): TokenKwToken = TokenKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [TokenKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isTokenKw: Boolean
    get() = isValid && schemaId == TokenKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [TokenKwToken].
 */
public val CstToken.asTokenKw: TokenKwToken
    get() = TokenKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [TokenKwToken], returning [TokenKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asTokenKwOrNone: TokenKwToken
    get() = if (isTokenKw) TokenKwToken(tokenIndex) else TokenKwToken.NONE

/**
 * Converts this generic node handle into a [TokenKwToken].
 */
public val CstNode.asTokenKwToken: TokenKwToken
    get() = TokenKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [TokenKwToken].
 */
public fun CstToken.toTokenKw(): TokenKwToken = TokenKwToken(tokenIndex)

@JvmInline
public value class OpenKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """OpenKwToken($tokenIndex)""" else "OpenKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.OPEN_KW, "OpenKw", CstSchemaDslTokenKinds.KIND_OPEN_KW, "open", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.OPEN_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_OPEN_KW

        public const val FIXED_TEXT: String = "open"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: OpenKwToken = OpenKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): OpenKwToken = OpenKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): OpenKwToken = OpenKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [OpenKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isOpenKw: Boolean
    get() = isValid && schemaId == OpenKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [OpenKwToken].
 */
public val CstToken.asOpenKw: OpenKwToken
    get() = OpenKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [OpenKwToken], returning [OpenKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asOpenKwOrNone: OpenKwToken
    get() = if (isOpenKw) OpenKwToken(tokenIndex) else OpenKwToken.NONE

/**
 * Converts this generic node handle into a [OpenKwToken].
 */
public val CstNode.asOpenKwToken: OpenKwToken
    get() = OpenKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [OpenKwToken].
 */
public fun CstToken.toOpenKw(): OpenKwToken = OpenKwToken(tokenIndex)

@JvmInline
public value class CategoryKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """CategoryKwToken($tokenIndex)""" else "CategoryKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.CATEGORY_KW, "CategoryKw", CstSchemaDslTokenKinds.KIND_CATEGORY_KW, "category", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.CATEGORY_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_CATEGORY_KW

        public const val FIXED_TEXT: String = "category"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: CategoryKwToken = CategoryKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): CategoryKwToken = CategoryKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): CategoryKwToken = CategoryKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [CategoryKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isCategoryKw: Boolean
    get() = isValid && schemaId == CategoryKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [CategoryKwToken].
 */
public val CstToken.asCategoryKw: CategoryKwToken
    get() = CategoryKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [CategoryKwToken], returning [CategoryKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asCategoryKwOrNone: CategoryKwToken
    get() = if (isCategoryKw) CategoryKwToken(tokenIndex) else CategoryKwToken.NONE

/**
 * Converts this generic node handle into a [CategoryKwToken].
 */
public val CstNode.asCategoryKwToken: CategoryKwToken
    get() = CategoryKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [CategoryKwToken].
 */
public fun CstToken.toCategoryKw(): CategoryKwToken = CategoryKwToken(tokenIndex)

@JvmInline
public value class NodeKwToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """NodeKwToken($tokenIndex)""" else "NodeKwToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.NODE_KW, "NodeKw", CstSchemaDslTokenKinds.KIND_NODE_KW, "node", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.NODE_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_NODE_KW

        public const val FIXED_TEXT: String = "node"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: NodeKwToken = NodeKwToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): NodeKwToken = NodeKwToken(token.tokenIndex)

        public fun fromNode(node: CstNode): NodeKwToken = NodeKwToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [NodeKwToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isNodeKw: Boolean
    get() = isValid && schemaId == NodeKwToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [NodeKwToken].
 */
public val CstToken.asNodeKw: NodeKwToken
    get() = NodeKwToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [NodeKwToken], returning [NodeKwToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asNodeKwOrNone: NodeKwToken
    get() = if (isNodeKw) NodeKwToken(tokenIndex) else NodeKwToken.NONE

/**
 * Converts this generic node handle into a [NodeKwToken].
 */
public val CstNode.asNodeKwToken: NodeKwToken
    get() = NodeKwToken(nodeIndex)

/**
 * Converts this generic token handle into a [NodeKwToken].
 */
public fun CstToken.toNodeKw(): NodeKwToken = NodeKwToken(tokenIndex)

@JvmInline
public value class ColonToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """ColonToken($tokenIndex)""" else "ColonToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.COLON, "Colon", CstSchemaDslTokenKinds.KIND_COLON, ":", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.COLON

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_COLON

        public const val FIXED_TEXT: String = ":"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: ColonToken = ColonToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): ColonToken = ColonToken(token.tokenIndex)

        public fun fromNode(node: CstNode): ColonToken = ColonToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [ColonToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isColon: Boolean
    get() = isValid && schemaId == ColonToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [ColonToken].
 */
public val CstToken.asColon: ColonToken
    get() = ColonToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [ColonToken], returning [ColonToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asColonOrNone: ColonToken
    get() = if (isColon) ColonToken(tokenIndex) else ColonToken.NONE

/**
 * Converts this generic node handle into a [ColonToken].
 */
public val CstNode.asColonToken: ColonToken
    get() = ColonToken(nodeIndex)

/**
 * Converts this generic token handle into a [ColonToken].
 */
public fun CstToken.toColon(): ColonToken = ColonToken(tokenIndex)

@JvmInline
public value class EqToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """EqToken($tokenIndex)""" else "EqToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.EQ, "Eq", CstSchemaDslTokenKinds.KIND_EQ, "=", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.EQ

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_EQ

        public const val FIXED_TEXT: String = "="

        public const val IS_TRIVIA: Boolean = false

        public val NONE: EqToken = EqToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): EqToken = EqToken(token.tokenIndex)

        public fun fromNode(node: CstNode): EqToken = EqToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [EqToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isEq: Boolean
    get() = isValid && schemaId == EqToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [EqToken].
 */
public val CstToken.asEq: EqToken
    get() = EqToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [EqToken], returning [EqToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asEqOrNone: EqToken
    get() = if (isEq) EqToken(tokenIndex) else EqToken.NONE

/**
 * Converts this generic node handle into a [EqToken].
 */
public val CstNode.asEqToken: EqToken
    get() = EqToken(nodeIndex)

/**
 * Converts this generic token handle into a [EqToken].
 */
public fun CstToken.toEq(): EqToken = EqToken(tokenIndex)

@JvmInline
public value class PipeToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """PipeToken($tokenIndex)""" else "PipeToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.PIPE, "Pipe", CstSchemaDslTokenKinds.KIND_PIPE, "|", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.PIPE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PIPE

        public const val FIXED_TEXT: String = "|"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PipeToken = PipeToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PipeToken = PipeToken(token.tokenIndex)

        public fun fromNode(node: CstNode): PipeToken = PipeToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PipeToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPipe: Boolean
    get() = isValid && schemaId == PipeToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PipeToken].
 */
public val CstToken.asPipe: PipeToken
    get() = PipeToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PipeToken], returning [PipeToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPipeOrNone: PipeToken
    get() = if (isPipe) PipeToken(tokenIndex) else PipeToken.NONE

/**
 * Converts this generic node handle into a [PipeToken].
 */
public val CstNode.asPipeToken: PipeToken
    get() = PipeToken(nodeIndex)

/**
 * Converts this generic token handle into a [PipeToken].
 */
public fun CstToken.toPipe(): PipeToken = PipeToken(tokenIndex)

@JvmInline
public value class QuestionToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    public val asQuantifierOp: QuantifierOpToken
        get() = QuantifierOpToken(tokenIndex)

    override fun toString(): String = if (isValid) """QuestionToken($tokenIndex)""" else "QuestionToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.QUESTION, "Question", CstSchemaDslTokenKinds.KIND_QUESTION, "?", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.QUESTION

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_QUESTION

        public const val FIXED_TEXT: String = "?"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: QuestionToken = QuestionToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): QuestionToken = QuestionToken(token.tokenIndex)

        public fun fromNode(node: CstNode): QuestionToken = QuestionToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [QuestionToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isQuestion: Boolean
    get() = isValid && schemaId == QuestionToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [QuestionToken].
 */
public val CstToken.asQuestion: QuestionToken
    get() = QuestionToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [QuestionToken], returning [QuestionToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asQuestionOrNone: QuestionToken
    get() = if (isQuestion) QuestionToken(tokenIndex) else QuestionToken.NONE

/**
 * Converts this generic node handle into a [QuestionToken].
 */
public val CstNode.asQuestionToken: QuestionToken
    get() = QuestionToken(nodeIndex)

/**
 * Converts this generic token handle into a [QuestionToken].
 */
public fun CstToken.toQuestion(): QuestionToken = QuestionToken(tokenIndex)

@JvmInline
public value class StarToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    public val asQuantifierOp: QuantifierOpToken
        get() = QuantifierOpToken(tokenIndex)

    override fun toString(): String = if (isValid) """StarToken($tokenIndex)""" else "StarToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.STAR, "Star", CstSchemaDslTokenKinds.KIND_STAR, "*", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.STAR

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_STAR

        public const val FIXED_TEXT: String = "*"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: StarToken = StarToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): StarToken = StarToken(token.tokenIndex)

        public fun fromNode(node: CstNode): StarToken = StarToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [StarToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isStar: Boolean
    get() = isValid && schemaId == StarToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [StarToken].
 */
public val CstToken.asStar: StarToken
    get() = StarToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [StarToken], returning [StarToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asStarOrNone: StarToken
    get() = if (isStar) StarToken(tokenIndex) else StarToken.NONE

/**
 * Converts this generic node handle into a [StarToken].
 */
public val CstNode.asStarToken: StarToken
    get() = StarToken(nodeIndex)

/**
 * Converts this generic token handle into a [StarToken].
 */
public fun CstToken.toStar(): StarToken = StarToken(tokenIndex)

@JvmInline
public value class PlusToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    public val asQuantifierOp: QuantifierOpToken
        get() = QuantifierOpToken(tokenIndex)

    override fun toString(): String = if (isValid) """PlusToken($tokenIndex)""" else "PlusToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.PLUS, "Plus", CstSchemaDslTokenKinds.KIND_PLUS, "+", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.PLUS

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PLUS

        public const val FIXED_TEXT: String = "+"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PlusToken = PlusToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PlusToken = PlusToken(token.tokenIndex)

        public fun fromNode(node: CstNode): PlusToken = PlusToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PlusToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPlus: Boolean
    get() = isValid && schemaId == PlusToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PlusToken].
 */
public val CstToken.asPlus: PlusToken
    get() = PlusToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PlusToken], returning [PlusToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPlusOrNone: PlusToken
    get() = if (isPlus) PlusToken(tokenIndex) else PlusToken.NONE

/**
 * Converts this generic node handle into a [PlusToken].
 */
public val CstNode.asPlusToken: PlusToken
    get() = PlusToken(nodeIndex)

/**
 * Converts this generic token handle into a [PlusToken].
 */
public fun CstToken.toPlus(): PlusToken = PlusToken(tokenIndex)

@JvmInline
public value class AtToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """AtToken($tokenIndex)""" else "AtToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.AT, "At", CstSchemaDslTokenKinds.KIND_AT, "@", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.AT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_AT

        public const val FIXED_TEXT: String = "@"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: AtToken = AtToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): AtToken = AtToken(token.tokenIndex)

        public fun fromNode(node: CstNode): AtToken = AtToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [AtToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isAt: Boolean
    get() = isValid && schemaId == AtToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [AtToken].
 */
public val CstToken.asAt: AtToken
    get() = AtToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [AtToken], returning [AtToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asAtOrNone: AtToken
    get() = if (isAt) AtToken(tokenIndex) else AtToken.NONE

/**
 * Converts this generic node handle into a [AtToken].
 */
public val CstNode.asAtToken: AtToken
    get() = AtToken(nodeIndex)

/**
 * Converts this generic token handle into a [AtToken].
 */
public fun CstToken.toAt(): AtToken = AtToken(tokenIndex)

@JvmInline
public value class LParenToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """LParenToken($tokenIndex)""" else "LParenToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.L_PAREN, "LParen", CstSchemaDslTokenKinds.KIND_L_PAREN, "(", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.L_PAREN

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_L_PAREN

        public const val FIXED_TEXT: String = "("

        public const val IS_TRIVIA: Boolean = false

        public val NONE: LParenToken = LParenToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LParenToken = LParenToken(token.tokenIndex)

        public fun fromNode(node: CstNode): LParenToken = LParenToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LParenToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLParen: Boolean
    get() = isValid && schemaId == LParenToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LParenToken].
 */
public val CstToken.asLParen: LParenToken
    get() = LParenToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LParenToken], returning [LParenToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLParenOrNone: LParenToken
    get() = if (isLParen) LParenToken(tokenIndex) else LParenToken.NONE

/**
 * Converts this generic node handle into a [LParenToken].
 */
public val CstNode.asLParenToken: LParenToken
    get() = LParenToken(nodeIndex)

/**
 * Converts this generic token handle into a [LParenToken].
 */
public fun CstToken.toLParen(): LParenToken = LParenToken(tokenIndex)

@JvmInline
public value class RParenToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """RParenToken($tokenIndex)""" else "RParenToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.R_PAREN, "RParen", CstSchemaDslTokenKinds.KIND_R_PAREN, ")", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.R_PAREN

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_R_PAREN

        public const val FIXED_TEXT: String = ")"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: RParenToken = RParenToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): RParenToken = RParenToken(token.tokenIndex)

        public fun fromNode(node: CstNode): RParenToken = RParenToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [RParenToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isRParen: Boolean
    get() = isValid && schemaId == RParenToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [RParenToken].
 */
public val CstToken.asRParen: RParenToken
    get() = RParenToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [RParenToken], returning [RParenToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asRParenOrNone: RParenToken
    get() = if (isRParen) RParenToken(tokenIndex) else RParenToken.NONE

/**
 * Converts this generic node handle into a [RParenToken].
 */
public val CstNode.asRParenToken: RParenToken
    get() = RParenToken(nodeIndex)

/**
 * Converts this generic token handle into a [RParenToken].
 */
public fun CstToken.toRParen(): RParenToken = RParenToken(tokenIndex)

@JvmInline
public value class LBraceToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """LBraceToken($tokenIndex)""" else "LBraceToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.L_BRACE, "LBrace", CstSchemaDslTokenKinds.KIND_L_BRACE, "{", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.L_BRACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_L_BRACE

        public const val FIXED_TEXT: String = "{"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: LBraceToken = LBraceToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LBraceToken = LBraceToken(token.tokenIndex)

        public fun fromNode(node: CstNode): LBraceToken = LBraceToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LBraceToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLBrace: Boolean
    get() = isValid && schemaId == LBraceToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LBraceToken].
 */
public val CstToken.asLBrace: LBraceToken
    get() = LBraceToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LBraceToken], returning [LBraceToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLBraceOrNone: LBraceToken
    get() = if (isLBrace) LBraceToken(tokenIndex) else LBraceToken.NONE

/**
 * Converts this generic node handle into a [LBraceToken].
 */
public val CstNode.asLBraceToken: LBraceToken
    get() = LBraceToken(nodeIndex)

/**
 * Converts this generic token handle into a [LBraceToken].
 */
public fun CstToken.toLBrace(): LBraceToken = LBraceToken(tokenIndex)

@JvmInline
public value class RBraceToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """RBraceToken($tokenIndex)""" else "RBraceToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.R_BRACE, "RBrace", CstSchemaDslTokenKinds.KIND_R_BRACE, "}", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.R_BRACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_R_BRACE

        public const val FIXED_TEXT: String = "}"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: RBraceToken = RBraceToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): RBraceToken = RBraceToken(token.tokenIndex)

        public fun fromNode(node: CstNode): RBraceToken = RBraceToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [RBraceToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isRBrace: Boolean
    get() = isValid && schemaId == RBraceToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [RBraceToken].
 */
public val CstToken.asRBrace: RBraceToken
    get() = RBraceToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [RBraceToken], returning [RBraceToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asRBraceOrNone: RBraceToken
    get() = if (isRBrace) RBraceToken(tokenIndex) else RBraceToken.NONE

/**
 * Converts this generic node handle into a [RBraceToken].
 */
public val CstNode.asRBraceToken: RBraceToken
    get() = RBraceToken(nodeIndex)

/**
 * Converts this generic token handle into a [RBraceToken].
 */
public fun CstToken.toRBrace(): RBraceToken = RBraceToken(tokenIndex)

@JvmInline
public value class CommaToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """CommaToken($tokenIndex)""" else "CommaToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.COMMA, "Comma", CstSchemaDslTokenKinds.KIND_COMMA, ",", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.COMMA

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_COMMA

        public const val FIXED_TEXT: String = ","

        public const val IS_TRIVIA: Boolean = false

        public val NONE: CommaToken = CommaToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): CommaToken = CommaToken(token.tokenIndex)

        public fun fromNode(node: CstNode): CommaToken = CommaToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [CommaToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isComma: Boolean
    get() = isValid && schemaId == CommaToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [CommaToken].
 */
public val CstToken.asComma: CommaToken
    get() = CommaToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [CommaToken], returning [CommaToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asCommaOrNone: CommaToken
    get() = if (isComma) CommaToken(tokenIndex) else CommaToken.NONE

/**
 * Converts this generic node handle into a [CommaToken].
 */
public val CstNode.asCommaToken: CommaToken
    get() = CommaToken(nodeIndex)

/**
 * Converts this generic token handle into a [CommaToken].
 */
public fun CstToken.toComma(): CommaToken = CommaToken(tokenIndex)

@JvmInline
public value class DotToken(
    public val tokenIndex: Int,
) {
    /**
     * True if this handle points to a valid token index (>= 0).
     */
    public val isValid: Boolean
        get() = tokenIndex >= 0

    /**
     * True if this handle is the absent sentinel ([NONE]).
     */
    public val isNone: Boolean
        get() = tokenIndex < 0

    /**
     * Converts this specialized handle into a generic [CstToken].
     */
    public val asToken: CstToken
        get() = CstToken(tokenIndex)

    /**
     * Converts this specialized handle into a generic [CstNode].
     */
    public val asNode: CstNode
        get() = CstNode(tokenIndex)

    override fun toString(): String = if (isValid) """DotToken($tokenIndex)""" else "DotToken.NONE"

    public companion object : CstTokenDescriptor(CstSchemaDslTokenKinds.DOT, "Dot", CstSchemaDslTokenKinds.KIND_DOT, ".", false) {
        public const val ID: Int = CstSchemaDslTokenKinds.DOT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_DOT

        public const val FIXED_TEXT: String = "."

        public const val IS_TRIVIA: Boolean = false

        public val NONE: DotToken = DotToken(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): DotToken = DotToken(token.tokenIndex)

        public fun fromNode(node: CstNode): DotToken = DotToken(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [DotToken.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isDot: Boolean
    get() = isValid && schemaId == DotToken.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [DotToken].
 */
public val CstToken.asDot: DotToken
    get() = DotToken(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [DotToken], returning [DotToken.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asDotOrNone: DotToken
    get() = if (isDot) DotToken(tokenIndex) else DotToken.NONE

/**
 * Converts this generic node handle into a [DotToken].
 */
public val CstNode.asDotToken: DotToken
    get() = DotToken(nodeIndex)

/**
 * Converts this generic token handle into a [DotToken].
 */
public fun CstToken.toDot(): DotToken = DotToken(tokenIndex)

/**
 * Zero-allocation list view over [DeclarationSyntax] children in a specific schema slot.
 */
@JvmInline
public value class DeclarationList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): DeclarationSyntax = if (parentNodeIndex >= 0) DeclarationSyntax(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else DeclarationSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (DeclarationSyntax) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(DeclarationSyntax(childIdx))
        }
    }

    public companion object {
        public val EMPTY: DeclarationList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): DeclarationList = DeclarationList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [IdentToken] children in a specific schema slot.
 */
@JvmInline
public value class IdentTokenList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): IdentToken = if (parentNodeIndex >= 0) IdentToken(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else IdentToken.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (IdentToken) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(IdentToken(childIdx))
        }
    }

    public companion object {
        public val EMPTY: IdentTokenList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): IdentTokenList = IdentTokenList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [DotToken] children in a specific schema slot.
 */
@JvmInline
public value class DotTokenList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): DotToken = if (parentNodeIndex >= 0) DotToken(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else DotToken.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (DotToken) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(DotToken(childIdx))
        }
    }

    public companion object {
        public val EMPTY: DotTokenList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): DotTokenList = DotTokenList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [AnnotationSyntax] children in a specific schema slot.
 */
@JvmInline
public value class AnnotationList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): AnnotationSyntax = if (parentNodeIndex >= 0) AnnotationSyntax(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else AnnotationSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (AnnotationSyntax) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(AnnotationSyntax(childIdx))
        }
    }

    public companion object {
        public val EMPTY: AnnotationList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): AnnotationList = AnnotationList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [TokenMemberRefSyntax] children in a specific schema slot.
 */
@JvmInline
public value class TokenMemberRefList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): TokenMemberRefSyntax = if (parentNodeIndex >= 0) TokenMemberRefSyntax(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else TokenMemberRefSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (TokenMemberRefSyntax) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(TokenMemberRefSyntax(childIdx))
        }
    }

    public companion object {
        public val EMPTY: TokenMemberRefList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): TokenMemberRefList = TokenMemberRefList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [PipeToken] children in a specific schema slot.
 */
@JvmInline
public value class PipeTokenList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): PipeToken = if (parentNodeIndex >= 0) PipeToken(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else PipeToken.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (PipeToken) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(PipeToken(childIdx))
        }
    }

    public companion object {
        public val EMPTY: PipeTokenList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): PipeTokenList = PipeTokenList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [FieldDeclSyntax] children in a specific schema slot.
 */
@JvmInline
public value class FieldDeclList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): FieldDeclSyntax = if (parentNodeIndex >= 0) FieldDeclSyntax(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else FieldDeclSyntax.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (FieldDeclSyntax) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(FieldDeclSyntax(childIdx))
        }
    }

    public companion object {
        public val EMPTY: FieldDeclList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): FieldDeclList = FieldDeclList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}

/**
 * Zero-allocation list view over [CommaToken] children in a specific schema slot.
 */
@JvmInline
public value class CommaTokenList(
    public val packed: Long,
) {
    public val parentNodeIndex: Int
        get() = (packed ushr 32).toInt()

    public val slot: Int
        get() = packed.toInt()

    context(tree: ConcreteSyntaxTree)
    public val size: Int
        get() = if (parentNodeIndex >= 0) tree.getChildCountInSlot(parentNodeIndex, slot) else 0

    context(tree: ConcreteSyntaxTree)
    public val isEmpty: Boolean
        get() = parentNodeIndex < 0 || tree.getChildBySlot(parentNodeIndex, slot) == NULL_NODE_INDEX

    context(tree: ConcreteSyntaxTree)
    public operator fun get(index: Int): CommaToken = if (parentNodeIndex >= 0) CommaToken(tree.getChildInSlotAt(parentNodeIndex, slot, index)) else CommaToken.NONE

    context(tree: ConcreteSyntaxTree)
    public inline fun forEach(action: (CommaToken) -> Unit) {
        if (parentNodeIndex < 0) return
        tree.forEachChildInSlot(parentNodeIndex, slot) { childIdx ->
            action(CommaToken(childIdx))
        }
    }

    public companion object {
        public val EMPTY: CommaTokenList = pack(NULL_NODE_INDEX, NO_SLOT.toInt())

        public fun pack(parentNodeIndex: Int, slot: Int): CommaTokenList = CommaTokenList((parentNodeIndex.toLong() shl 32) or (slot.toLong() and 0xFFFF_FFFFL))
    }
}
