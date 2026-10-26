package dev.dialector.cst.schema.dsl

import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.CstNode
import dev.dialector.cst.CstToken
import dev.dialector.cst.NULL_NODE_INDEX
import dev.dialector.cst.NULL_TOKEN_INDEX
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.jvm.JvmInline

/**
 * True if this node belongs to category [Declaration].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isDeclaration: Boolean
    get() = isValid && CstSchemaDslCategories.isDeclaration(kind)

@JvmInline
public value class DeclarationHandle(
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
        get() = isValid && asNode.kind == TokenDeclHandle.KIND

    public val asTokenDecl: TokenDeclHandle
        get() = TokenDeclHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asTokenDeclOrNone: TokenDeclHandle
        get() = if (isTokenDecl) TokenDeclHandle(nodeIndex) else TokenDeclHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isTokenCategoryDecl: Boolean
        get() = isValid && asNode.kind == TokenCategoryDeclHandle.KIND

    public val asTokenCategoryDecl: TokenCategoryDeclHandle
        get() = TokenCategoryDeclHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asTokenCategoryDeclOrNone: TokenCategoryDeclHandle
        get() = if (isTokenCategoryDecl) TokenCategoryDeclHandle(nodeIndex) else TokenCategoryDeclHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isCategoryDecl: Boolean
        get() = isValid && asNode.kind == CategoryDeclHandle.KIND

    public val asCategoryDecl: CategoryDeclHandle
        get() = CategoryDeclHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asCategoryDeclOrNone: CategoryDeclHandle
        get() = if (isCategoryDecl) CategoryDeclHandle(nodeIndex) else CategoryDeclHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isNodeDecl: Boolean
        get() = isValid && asNode.kind == NodeDeclHandle.KIND

    public val asNodeDecl: NodeDeclHandle
        get() = NodeDeclHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asNodeDeclOrNone: NodeDeclHandle
        get() = if (isNodeDecl) NodeDeclHandle(nodeIndex) else NodeDeclHandle.NONE

    override fun toString(): String = if (isValid) """DeclarationHandle($nodeIndex)""" else "DeclarationHandle.NONE"

    public companion object {
        public val NONE: DeclarationHandle = DeclarationHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): DeclarationHandle = DeclarationHandle(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstNode] as a [DeclarationHandle].
 */
public val CstNode.asDeclaration: DeclarationHandle
    get() = DeclarationHandle(nodeIndex)

/**
 * Safe conversion to [DeclarationHandle]; returns [DeclarationHandle.NONE] if this node does not belong to [Declaration].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asDeclarationOrNone: DeclarationHandle
    get() = if (isDeclaration) DeclarationHandle(nodeIndex) else DeclarationHandle.NONE

/**
 * True if this node belongs to category [TypeExpr].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTypeExpr: Boolean
    get() = isValid && CstSchemaDslCategories.isTypeExpr(kind)

@JvmInline
public value class TypeExprHandle(
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
        get() = isValid && asNode.kind == NamedTypeExprHandle.KIND

    public val asNamedTypeExpr: NamedTypeExprHandle
        get() = NamedTypeExprHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asNamedTypeExprOrNone: NamedTypeExprHandle
        get() = if (isNamedTypeExpr) NamedTypeExprHandle(nodeIndex) else NamedTypeExprHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isLiteralTokenTypeExpr: Boolean
        get() = isValid && asNode.kind == LiteralTokenTypeExprHandle.KIND

    public val asLiteralTokenTypeExpr: LiteralTokenTypeExprHandle
        get() = LiteralTokenTypeExprHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asLiteralTokenTypeExprOrNone: LiteralTokenTypeExprHandle
        get() = if (isLiteralTokenTypeExpr) LiteralTokenTypeExprHandle(nodeIndex) else LiteralTokenTypeExprHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isInlineUnionTypeExpr: Boolean
        get() = isValid && asNode.kind == InlineUnionTypeExprHandle.KIND

    public val asInlineUnionTypeExpr: InlineUnionTypeExprHandle
        get() = InlineUnionTypeExprHandle(nodeIndex)

    context(tree: ConcreteSyntaxTree)
    public val asInlineUnionTypeExprOrNone: InlineUnionTypeExprHandle
        get() = if (isInlineUnionTypeExpr) InlineUnionTypeExprHandle(nodeIndex) else InlineUnionTypeExprHandle.NONE

    override fun toString(): String = if (isValid) """TypeExprHandle($nodeIndex)""" else "TypeExprHandle.NONE"

    public companion object {
        public val NONE: TypeExprHandle = TypeExprHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TypeExprHandle = TypeExprHandle(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstNode] as a [TypeExprHandle].
 */
public val CstNode.asTypeExpr: TypeExprHandle
    get() = TypeExprHandle(nodeIndex)

/**
 * Safe conversion to [TypeExprHandle]; returns [TypeExprHandle.NONE] if this node does not belong to [TypeExpr].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTypeExprOrNone: TypeExprHandle
    get() = if (isTypeExpr) TypeExprHandle(nodeIndex) else TypeExprHandle.NONE

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
public value class QuantifierOpTokenHandle(
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
        get() = isValid && asToken.schemaId == QuestionTokenHandle.ID

    public val asQuestion: QuestionTokenHandle
        get() = QuestionTokenHandle(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asQuestionOrNone: QuestionTokenHandle
        get() = if (isQuestion) QuestionTokenHandle(tokenIndex) else QuestionTokenHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isStar: Boolean
        get() = isValid && asToken.schemaId == StarTokenHandle.ID

    public val asStar: StarTokenHandle
        get() = StarTokenHandle(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asStarOrNone: StarTokenHandle
        get() = if (isStar) StarTokenHandle(tokenIndex) else StarTokenHandle.NONE

    context(tree: ConcreteSyntaxTree)
    public val isPlus: Boolean
        get() = isValid && asToken.schemaId == PlusTokenHandle.ID

    public val asPlus: PlusTokenHandle
        get() = PlusTokenHandle(tokenIndex)

    context(tree: ConcreteSyntaxTree)
    public val asPlusOrNone: PlusTokenHandle
        get() = if (isPlus) PlusTokenHandle(tokenIndex) else PlusTokenHandle.NONE

    override fun toString(): String = if (isValid) """QuantifierOpTokenHandle($tokenIndex)""" else "QuantifierOpTokenHandle.NONE"

    public companion object {
        public val NONE: QuantifierOpTokenHandle = QuantifierOpTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): QuantifierOpTokenHandle = QuantifierOpTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): QuantifierOpTokenHandle = QuantifierOpTokenHandle(node.nodeIndex)
    }
}

/**
 * Unchecked reinterpretation of this [CstToken] as a [QuantifierOpTokenHandle].
 */
public val CstToken.asQuantifierOp: QuantifierOpTokenHandle
    get() = QuantifierOpTokenHandle(tokenIndex)

/**
 * Safe conversion to [QuantifierOpTokenHandle]; returns [QuantifierOpTokenHandle.NONE] if this token does not belong to [QuantifierOp].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asQuantifierOpOrNone: QuantifierOpTokenHandle
    get() = if (isQuantifierOp) QuantifierOpTokenHandle(tokenIndex) else QuantifierOpTokenHandle.NONE

/**
 * Converts this generic node handle into a [QuantifierOpTokenHandle].
 */
public val CstNode.asQuantifierOpToken: QuantifierOpTokenHandle
    get() = QuantifierOpTokenHandle(nodeIndex)

@JvmInline
public value class SchemaFileHandle(
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
    public val schemaHeader: SchemaHeaderHandle
        get() = SchemaHeaderHandle(tree.getChildBySlot(nodeIndex, SLOT_SCHEMA_HEADER))

    /**
     * (Optional child at slot [SLOT_PACKAGE_HEADER] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val packageHeader: PackageHeaderHandle
        get() = PackageHeaderHandle(tree.getChildBySlot(nodeIndex, SLOT_PACKAGE_HEADER))

    context(tree: ConcreteSyntaxTree)
    public val declarations: List<DeclarationHandle>
        get() {
            val list = mutableListOf<DeclarationHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_DECLARATIONS) { childIdx ->
                list.add(DeclarationHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachDeclarations(action: (DeclarationHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_DECLARATIONS) { childIdx ->
            action(DeclarationHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """SchemaFileHandle($nodeIndex)""" else "SchemaFileHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.SCHEMA_FILE

        public const val SLOT_SCHEMA_HEADER: Int = 0

        public const val SLOT_PACKAGE_HEADER: Int = 1

        public const val SLOT_DECLARATIONS: Int = 2

        public val NONE: SchemaFileHandle = SchemaFileHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): SchemaFileHandle = SchemaFileHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [SchemaFileHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isSchemaFile: Boolean
    get() = isValid && kind == SchemaFileHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [SchemaFileHandle].
 */
public val CstNode.asSchemaFile: SchemaFileHandle
    get() = SchemaFileHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [SchemaFileHandle], returning [SchemaFileHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asSchemaFileOrNone: SchemaFileHandle
    get() = if (isSchemaFile) SchemaFileHandle(nodeIndex) else SchemaFileHandle.NONE

/**
 * Converts this generic node handle into a [SchemaFileHandle].
 */
public fun CstNode.toSchemaFile(): SchemaFileHandle = SchemaFileHandle(nodeIndex)

@JvmInline
public value class SchemaHeaderHandle(
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
    public val schemaKw: SchemaKwTokenHandle
        get() = SchemaKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_SCHEMA_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """SchemaHeaderHandle($nodeIndex)""" else "SchemaHeaderHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.SCHEMA_HEADER

        public const val SLOT_SCHEMA_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public val NONE: SchemaHeaderHandle = SchemaHeaderHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): SchemaHeaderHandle = SchemaHeaderHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [SchemaHeaderHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isSchemaHeader: Boolean
    get() = isValid && kind == SchemaHeaderHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [SchemaHeaderHandle].
 */
public val CstNode.asSchemaHeader: SchemaHeaderHandle
    get() = SchemaHeaderHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [SchemaHeaderHandle], returning [SchemaHeaderHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asSchemaHeaderOrNone: SchemaHeaderHandle
    get() = if (isSchemaHeader) SchemaHeaderHandle(nodeIndex) else SchemaHeaderHandle.NONE

/**
 * Converts this generic node handle into a [SchemaHeaderHandle].
 */
public fun CstNode.toSchemaHeader(): SchemaHeaderHandle = SchemaHeaderHandle(nodeIndex)

@JvmInline
public value class PackageHeaderHandle(
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
    public val packageKw: PackageKwTokenHandle
        get() = PackageKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_PACKAGE_KW))

    /**
     * (Mandatory child (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: QualifiedNameHandle
        get() = QualifiedNameHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """PackageHeaderHandle($nodeIndex)""" else "PackageHeaderHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.PACKAGE_HEADER

        public const val SLOT_PACKAGE_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public val NONE: PackageHeaderHandle = PackageHeaderHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): PackageHeaderHandle = PackageHeaderHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [PackageHeaderHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isPackageHeader: Boolean
    get() = isValid && kind == PackageHeaderHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [PackageHeaderHandle].
 */
public val CstNode.asPackageHeader: PackageHeaderHandle
    get() = PackageHeaderHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [PackageHeaderHandle], returning [PackageHeaderHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asPackageHeaderOrNone: PackageHeaderHandle
    get() = if (isPackageHeader) PackageHeaderHandle(nodeIndex) else PackageHeaderHandle.NONE

/**
 * Converts this generic node handle into a [PackageHeaderHandle].
 */
public fun CstNode.toPackageHeader(): PackageHeaderHandle = PackageHeaderHandle(nodeIndex)

@JvmInline
public value class QualifiedNameHandle(
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
    public val parts: List<IdentTokenHandle>
        get() {
            val list = mutableListOf<IdentTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_PARTS) { childIdx ->
                list.add(IdentTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public val dots: List<DotTokenHandle>
        get() {
            val list = mutableListOf<DotTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_DOTS) { childIdx ->
                list.add(DotTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachParts(action: (IdentTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_PARTS) { childIdx ->
            action(IdentTokenHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachDots(action: (DotTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_DOTS) { childIdx ->
            action(DotTokenHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """QualifiedNameHandle($nodeIndex)""" else "QualifiedNameHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.QUALIFIED_NAME

        public const val SLOT_PARTS: Int = 0

        public const val SLOT_DOTS: Int = 1

        public val NONE: QualifiedNameHandle = QualifiedNameHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): QualifiedNameHandle = QualifiedNameHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [QualifiedNameHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isQualifiedName: Boolean
    get() = isValid && kind == QualifiedNameHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [QualifiedNameHandle].
 */
public val CstNode.asQualifiedName: QualifiedNameHandle
    get() = QualifiedNameHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [QualifiedNameHandle], returning [QualifiedNameHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asQualifiedNameOrNone: QualifiedNameHandle
    get() = if (isQualifiedName) QualifiedNameHandle(nodeIndex) else QualifiedNameHandle.NONE

/**
 * Converts this generic node handle into a [QualifiedNameHandle].
 */
public fun CstNode.toQualifiedName(): QualifiedNameHandle = QualifiedNameHandle(nodeIndex)

@JvmInline
public value class TokenDeclHandle(
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
    public val isDeclaration: Boolean
        get() = true

    public val asDeclaration: DeclarationHandle
        get() = DeclarationHandle(nodeIndex)

    /**
     * (Optional token at slot [SLOT_TRIVIA_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val triviaKw: TriviaKwTokenHandle
        get() = TriviaKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_TRIVIA_KW))

    /**
     * (Optional token at slot [SLOT_TOKEN_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val tokenKw: TokenKwTokenHandle
        get() = TokenKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_TOKEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_CATEGORIES] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val categories: CategoryClauseHandle
        get() = CategoryClauseHandle(tree.getChildBySlot(nodeIndex, SLOT_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqTokenHandle
        get() = EqTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    /**
     * (Optional token at slot [SLOT_FIXED_TEXT] = 5)
     */
    context(tree: ConcreteSyntaxTree)
    public val fixedText: StringLiteralTokenHandle
        get() = StringLiteralTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_FIXED_TEXT))

    context(tree: ConcreteSyntaxTree)
    public val annotations: List<AnnotationHandle>
        get() {
            val list = mutableListOf<AnnotationHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
                list.add(AnnotationHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachAnnotations(action: (AnnotationHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
            action(AnnotationHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """TokenDeclHandle($nodeIndex)""" else "TokenDeclHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_DECL

        public const val SLOT_TRIVIA_KW: Int = 0

        public const val SLOT_TOKEN_KW: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_CATEGORIES: Int = 3

        public const val SLOT_EQ: Int = 4

        public const val SLOT_FIXED_TEXT: Int = 5

        public const val SLOT_ANNOTATIONS: Int = 6

        public val NONE: TokenDeclHandle = TokenDeclHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenDeclHandle = TokenDeclHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenDeclHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenDecl: Boolean
    get() = isValid && kind == TokenDeclHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenDeclHandle].
 */
public val CstNode.asTokenDecl: TokenDeclHandle
    get() = TokenDeclHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenDeclHandle], returning [TokenDeclHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenDeclOrNone: TokenDeclHandle
    get() = if (isTokenDecl) TokenDeclHandle(nodeIndex) else TokenDeclHandle.NONE

/**
 * Converts this generic node handle into a [TokenDeclHandle].
 */
public fun CstNode.toTokenDecl(): TokenDeclHandle = TokenDeclHandle(nodeIndex)

@JvmInline
public value class TokenCategoryDeclHandle(
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
    public val isDeclaration: Boolean
        get() = true

    public val asDeclaration: DeclarationHandle
        get() = DeclarationHandle(nodeIndex)

    /**
     * (Optional token at slot [SLOT_OPEN_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val openKw: OpenKwTokenHandle
        get() = OpenKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_OPEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_TOKEN_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val tokenKw: TokenKwTokenHandle
        get() = TokenKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_TOKEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_CATEGORY_KW] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val categoryKw: CategoryKwTokenHandle
        get() = CategoryKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_CATEGORY_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_SUPER_CATEGORIES] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val superCategories: CategoryClauseHandle
        get() = CategoryClauseHandle(tree.getChildBySlot(nodeIndex, SLOT_SUPER_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 5)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqTokenHandle
        get() = EqTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    context(tree: ConcreteSyntaxTree)
    public val members: List<TokenMemberRefHandle>
        get() {
            val list = mutableListOf<TokenMemberRefHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
                list.add(TokenMemberRefHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public val pipes: List<PipeTokenHandle>
        get() {
            val list = mutableListOf<PipeTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
                list.add(PipeTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachMembers(action: (TokenMemberRefHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
            action(TokenMemberRefHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachPipes(action: (PipeTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
            action(PipeTokenHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """TokenCategoryDeclHandle($nodeIndex)""" else "TokenCategoryDeclHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL

        public const val SLOT_OPEN_KW: Int = 0

        public const val SLOT_TOKEN_KW: Int = 1

        public const val SLOT_CATEGORY_KW: Int = 2

        public const val SLOT_NAME: Int = 3

        public const val SLOT_SUPER_CATEGORIES: Int = 4

        public const val SLOT_EQ: Int = 5

        public const val SLOT_MEMBERS: Int = 6

        public const val SLOT_PIPES: Int = 7

        public val NONE: TokenCategoryDeclHandle = TokenCategoryDeclHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenCategoryDeclHandle = TokenCategoryDeclHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenCategoryDeclHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenCategoryDecl: Boolean
    get() = isValid && kind == TokenCategoryDeclHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenCategoryDeclHandle].
 */
public val CstNode.asTokenCategoryDecl: TokenCategoryDeclHandle
    get() = TokenCategoryDeclHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenCategoryDeclHandle], returning [TokenCategoryDeclHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenCategoryDeclOrNone: TokenCategoryDeclHandle
    get() = if (isTokenCategoryDecl) TokenCategoryDeclHandle(nodeIndex) else TokenCategoryDeclHandle.NONE

/**
 * Converts this generic node handle into a [TokenCategoryDeclHandle].
 */
public fun CstNode.toTokenCategoryDecl(): TokenCategoryDeclHandle = TokenCategoryDeclHandle(nodeIndex)

@JvmInline
public value class TokenMemberRefHandle(
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
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional token at slot [SLOT_LITERAL] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val literal: StringLiteralTokenHandle
        get() = StringLiteralTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_LITERAL))

    override fun toString(): String = if (isValid) """TokenMemberRefHandle($nodeIndex)""" else "TokenMemberRefHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.TOKEN_MEMBER_REF

        public const val SLOT_NAME: Int = 0

        public const val SLOT_LITERAL: Int = 1

        public val NONE: TokenMemberRefHandle = TokenMemberRefHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): TokenMemberRefHandle = TokenMemberRefHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [TokenMemberRefHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isTokenMemberRef: Boolean
    get() = isValid && kind == TokenMemberRefHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [TokenMemberRefHandle].
 */
public val CstNode.asTokenMemberRef: TokenMemberRefHandle
    get() = TokenMemberRefHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [TokenMemberRefHandle], returning [TokenMemberRefHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asTokenMemberRefOrNone: TokenMemberRefHandle
    get() = if (isTokenMemberRef) TokenMemberRefHandle(nodeIndex) else TokenMemberRefHandle.NONE

/**
 * Converts this generic node handle into a [TokenMemberRefHandle].
 */
public fun CstNode.toTokenMemberRef(): TokenMemberRefHandle = TokenMemberRefHandle(nodeIndex)

@JvmInline
public value class CategoryDeclHandle(
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
    public val isDeclaration: Boolean
        get() = true

    public val asDeclaration: DeclarationHandle
        get() = DeclarationHandle(nodeIndex)

    /**
     * (Optional token at slot [SLOT_OPEN_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val openKw: OpenKwTokenHandle
        get() = OpenKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_OPEN_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_CATEGORY_KW] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val categoryKw: CategoryKwTokenHandle
        get() = CategoryKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_CATEGORY_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_SUPER_CATEGORIES] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val superCategories: CategoryClauseHandle
        get() = CategoryClauseHandle(tree.getChildBySlot(nodeIndex, SLOT_SUPER_CATEGORIES))

    /**
     * (Optional token at slot [SLOT_EQ] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val eq: EqTokenHandle
        get() = EqTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_EQ))

    context(tree: ConcreteSyntaxTree)
    public val members: List<IdentTokenHandle>
        get() {
            val list = mutableListOf<IdentTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
                list.add(IdentTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public val pipes: List<PipeTokenHandle>
        get() {
            val list = mutableListOf<PipeTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
                list.add(PipeTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachMembers(action: (IdentTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
            action(IdentTokenHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachPipes(action: (PipeTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
            action(PipeTokenHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """CategoryDeclHandle($nodeIndex)""" else "CategoryDeclHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.CATEGORY_DECL

        public const val SLOT_OPEN_KW: Int = 0

        public const val SLOT_CATEGORY_KW: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_SUPER_CATEGORIES: Int = 3

        public const val SLOT_EQ: Int = 4

        public const val SLOT_MEMBERS: Int = 5

        public const val SLOT_PIPES: Int = 6

        public val NONE: CategoryDeclHandle = CategoryDeclHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): CategoryDeclHandle = CategoryDeclHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [CategoryDeclHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isCategoryDecl: Boolean
    get() = isValid && kind == CategoryDeclHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [CategoryDeclHandle].
 */
public val CstNode.asCategoryDecl: CategoryDeclHandle
    get() = CategoryDeclHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [CategoryDeclHandle], returning [CategoryDeclHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asCategoryDeclOrNone: CategoryDeclHandle
    get() = if (isCategoryDecl) CategoryDeclHandle(nodeIndex) else CategoryDeclHandle.NONE

/**
 * Converts this generic node handle into a [CategoryDeclHandle].
 */
public fun CstNode.toCategoryDecl(): CategoryDeclHandle = CategoryDeclHandle(nodeIndex)

@JvmInline
public value class NodeDeclHandle(
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
    public val isDeclaration: Boolean
        get() = true

    public val asDeclaration: DeclarationHandle
        get() = DeclarationHandle(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NODE_KW] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val nodeKw: NodeKwTokenHandle
        get() = NodeKwTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NODE_KW))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional child at slot [SLOT_CATEGORIES] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val categories: CategoryClauseHandle
        get() = CategoryClauseHandle(tree.getChildBySlot(nodeIndex, SLOT_CATEGORIES))

    context(tree: ConcreteSyntaxTree)
    public val annotations: List<AnnotationHandle>
        get() {
            val list = mutableListOf<AnnotationHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
                list.add(AnnotationHandle(childIdx))
            }
            return list
        }

    /**
     * (Optional token at slot [SLOT_L_BRACE] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val lBrace: LBraceTokenHandle
        get() = LBraceTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_L_BRACE))

    context(tree: ConcreteSyntaxTree)
    public val fields: List<FieldDeclHandle>
        get() {
            val list = mutableListOf<FieldDeclHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_FIELDS) { childIdx ->
                list.add(FieldDeclHandle(childIdx))
            }
            return list
        }

    /**
     * (Optional token at slot [SLOT_R_BRACE] = 6)
     */
    context(tree: ConcreteSyntaxTree)
    public val rBrace: RBraceTokenHandle
        get() = RBraceTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_R_BRACE))

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachAnnotations(action: (AnnotationHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
            action(AnnotationHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachFields(action: (FieldDeclHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_FIELDS) { childIdx ->
            action(FieldDeclHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """NodeDeclHandle($nodeIndex)""" else "NodeDeclHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.NODE_DECL

        public const val SLOT_NODE_KW: Int = 0

        public const val SLOT_NAME: Int = 1

        public const val SLOT_CATEGORIES: Int = 2

        public const val SLOT_ANNOTATIONS: Int = 3

        public const val SLOT_L_BRACE: Int = 4

        public const val SLOT_FIELDS: Int = 5

        public const val SLOT_R_BRACE: Int = 6

        public val NONE: NodeDeclHandle = NodeDeclHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): NodeDeclHandle = NodeDeclHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [NodeDeclHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isNodeDecl: Boolean
    get() = isValid && kind == NodeDeclHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [NodeDeclHandle].
 */
public val CstNode.asNodeDecl: NodeDeclHandle
    get() = NodeDeclHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [NodeDeclHandle], returning [NodeDeclHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asNodeDeclOrNone: NodeDeclHandle
    get() = if (isNodeDecl) NodeDeclHandle(nodeIndex) else NodeDeclHandle.NONE

/**
 * Converts this generic node handle into a [NodeDeclHandle].
 */
public fun CstNode.toNodeDecl(): NodeDeclHandle = NodeDeclHandle(nodeIndex)

@JvmInline
public value class CategoryClauseHandle(
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
    public val colon: ColonTokenHandle
        get() = ColonTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_COLON))

    context(tree: ConcreteSyntaxTree)
    public val names: List<IdentTokenHandle>
        get() {
            val list = mutableListOf<IdentTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_NAMES) { childIdx ->
                list.add(IdentTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public val commas: List<CommaTokenHandle>
        get() {
            val list = mutableListOf<CommaTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_COMMAS) { childIdx ->
                list.add(CommaTokenHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachNames(action: (IdentTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_NAMES) { childIdx ->
            action(IdentTokenHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachCommas(action: (CommaTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_COMMAS) { childIdx ->
            action(CommaTokenHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """CategoryClauseHandle($nodeIndex)""" else "CategoryClauseHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.CATEGORY_CLAUSE

        public const val SLOT_COLON: Int = 0

        public const val SLOT_NAMES: Int = 1

        public const val SLOT_COMMAS: Int = 2

        public val NONE: CategoryClauseHandle = CategoryClauseHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): CategoryClauseHandle = CategoryClauseHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [CategoryClauseHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isCategoryClause: Boolean
    get() = isValid && kind == CategoryClauseHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [CategoryClauseHandle].
 */
public val CstNode.asCategoryClause: CategoryClauseHandle
    get() = CategoryClauseHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [CategoryClauseHandle], returning [CategoryClauseHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asCategoryClauseOrNone: CategoryClauseHandle
    get() = if (isCategoryClause) CategoryClauseHandle(nodeIndex) else CategoryClauseHandle.NONE

/**
 * Converts this generic node handle into a [CategoryClauseHandle].
 */
public fun CstNode.toCategoryClause(): CategoryClauseHandle = CategoryClauseHandle(nodeIndex)

@JvmInline
public value class FieldDeclHandle(
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
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_COLON] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val colon: ColonTokenHandle
        get() = ColonTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_COLON))

    /**
     * (Mandatory child (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_TYPE_EXPR] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val typeExpr: TypeExprHandle
        get() = TypeExprHandle(tree.getChildBySlot(nodeIndex, SLOT_TYPE_EXPR))

    /**
     * (Optional token at slot [SLOT_QUANTIFIER] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val quantifier: QuantifierOpTokenHandle
        get() = QuantifierOpTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_QUANTIFIER))

    context(tree: ConcreteSyntaxTree)
    public val annotations: List<AnnotationHandle>
        get() {
            val list = mutableListOf<AnnotationHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
                list.add(AnnotationHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachAnnotations(action: (AnnotationHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_ANNOTATIONS) { childIdx ->
            action(AnnotationHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """FieldDeclHandle($nodeIndex)""" else "FieldDeclHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.FIELD_DECL

        public const val SLOT_NAME: Int = 0

        public const val SLOT_COLON: Int = 1

        public const val SLOT_TYPE_EXPR: Int = 2

        public const val SLOT_QUANTIFIER: Int = 3

        public const val SLOT_ANNOTATIONS: Int = 4

        public val NONE: FieldDeclHandle = FieldDeclHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): FieldDeclHandle = FieldDeclHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [FieldDeclHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isFieldDecl: Boolean
    get() = isValid && kind == FieldDeclHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [FieldDeclHandle].
 */
public val CstNode.asFieldDecl: FieldDeclHandle
    get() = FieldDeclHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [FieldDeclHandle], returning [FieldDeclHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asFieldDeclOrNone: FieldDeclHandle
    get() = if (isFieldDecl) FieldDeclHandle(nodeIndex) else FieldDeclHandle.NONE

/**
 * Converts this generic node handle into a [FieldDeclHandle].
 */
public fun CstNode.toFieldDecl(): FieldDeclHandle = FieldDeclHandle(nodeIndex)

@JvmInline
public value class NamedTypeExprHandle(
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
    public val isTypeExpr: Boolean
        get() = true

    public val asTypeExpr: TypeExprHandle
        get() = TypeExprHandle(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    override fun toString(): String = if (isValid) """NamedTypeExprHandle($nodeIndex)""" else "NamedTypeExprHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.NAMED_TYPE_EXPR

        public const val SLOT_NAME: Int = 0

        public val NONE: NamedTypeExprHandle = NamedTypeExprHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): NamedTypeExprHandle = NamedTypeExprHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [NamedTypeExprHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isNamedTypeExpr: Boolean
    get() = isValid && kind == NamedTypeExprHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [NamedTypeExprHandle].
 */
public val CstNode.asNamedTypeExpr: NamedTypeExprHandle
    get() = NamedTypeExprHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [NamedTypeExprHandle], returning [NamedTypeExprHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asNamedTypeExprOrNone: NamedTypeExprHandle
    get() = if (isNamedTypeExpr) NamedTypeExprHandle(nodeIndex) else NamedTypeExprHandle.NONE

/**
 * Converts this generic node handle into a [NamedTypeExprHandle].
 */
public fun CstNode.toNamedTypeExpr(): NamedTypeExprHandle = NamedTypeExprHandle(nodeIndex)

@JvmInline
public value class LiteralTokenTypeExprHandle(
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
    public val isTypeExpr: Boolean
        get() = true

    public val asTypeExpr: TypeExprHandle
        get() = TypeExprHandle(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_LITERAL] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val literal: StringLiteralTokenHandle
        get() = StringLiteralTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_LITERAL))

    override fun toString(): String = if (isValid) """LiteralTokenTypeExprHandle($nodeIndex)""" else "LiteralTokenTypeExprHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR

        public const val SLOT_LITERAL: Int = 0

        public val NONE: LiteralTokenTypeExprHandle = LiteralTokenTypeExprHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): LiteralTokenTypeExprHandle = LiteralTokenTypeExprHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [LiteralTokenTypeExprHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isLiteralTokenTypeExpr: Boolean
    get() = isValid && kind == LiteralTokenTypeExprHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [LiteralTokenTypeExprHandle].
 */
public val CstNode.asLiteralTokenTypeExpr: LiteralTokenTypeExprHandle
    get() = LiteralTokenTypeExprHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [LiteralTokenTypeExprHandle], returning [LiteralTokenTypeExprHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asLiteralTokenTypeExprOrNone: LiteralTokenTypeExprHandle
    get() = if (isLiteralTokenTypeExpr) LiteralTokenTypeExprHandle(nodeIndex) else LiteralTokenTypeExprHandle.NONE

/**
 * Converts this generic node handle into a [LiteralTokenTypeExprHandle].
 */
public fun CstNode.toLiteralTokenTypeExpr(): LiteralTokenTypeExprHandle = LiteralTokenTypeExprHandle(nodeIndex)

@JvmInline
public value class InlineUnionTypeExprHandle(
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
    public val isTypeExpr: Boolean
        get() = true

    public val asTypeExpr: TypeExprHandle
        get() = TypeExprHandle(nodeIndex)

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_L_PAREN] = 0)
     */
    context(tree: ConcreteSyntaxTree)
    public val lParen: LParenTokenHandle
        get() = LParenTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_L_PAREN))

    context(tree: ConcreteSyntaxTree)
    public val members: List<TokenMemberRefHandle>
        get() {
            val list = mutableListOf<TokenMemberRefHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
                list.add(TokenMemberRefHandle(childIdx))
            }
            return list
        }

    context(tree: ConcreteSyntaxTree)
    public val pipes: List<PipeTokenHandle>
        get() {
            val list = mutableListOf<PipeTokenHandle>()
            tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
                list.add(PipeTokenHandle(childIdx))
            }
            return list
        }

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_R_PAREN] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val rParen: RParenTokenHandle
        get() = RParenTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_R_PAREN))

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachMembers(action: (TokenMemberRefHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_MEMBERS) { childIdx ->
            action(TokenMemberRefHandle(childIdx))
        }
    }

    context(tree: ConcreteSyntaxTree)
    public inline fun forEachPipes(action: (PipeTokenHandle) -> Unit) {
        tree.forEachChildInSlot(nodeIndex, SLOT_PIPES) { childIdx ->
            action(PipeTokenHandle(childIdx))
        }
    }

    override fun toString(): String = if (isValid) """InlineUnionTypeExprHandle($nodeIndex)""" else "InlineUnionTypeExprHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR

        public const val SLOT_L_PAREN: Int = 0

        public const val SLOT_MEMBERS: Int = 1

        public const val SLOT_PIPES: Int = 2

        public const val SLOT_R_PAREN: Int = 3

        public val NONE: InlineUnionTypeExprHandle = InlineUnionTypeExprHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): InlineUnionTypeExprHandle = InlineUnionTypeExprHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [InlineUnionTypeExprHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isInlineUnionTypeExpr: Boolean
    get() = isValid && kind == InlineUnionTypeExprHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [InlineUnionTypeExprHandle].
 */
public val CstNode.asInlineUnionTypeExpr: InlineUnionTypeExprHandle
    get() = InlineUnionTypeExprHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [InlineUnionTypeExprHandle], returning [InlineUnionTypeExprHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asInlineUnionTypeExprOrNone: InlineUnionTypeExprHandle
    get() = if (isInlineUnionTypeExpr) InlineUnionTypeExprHandle(nodeIndex) else InlineUnionTypeExprHandle.NONE

/**
 * Converts this generic node handle into a [InlineUnionTypeExprHandle].
 */
public fun CstNode.toInlineUnionTypeExpr(): InlineUnionTypeExprHandle = InlineUnionTypeExprHandle(nodeIndex)

@JvmInline
public value class AnnotationHandle(
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
    public val at: AtTokenHandle
        get() = AtTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_AT))

    /**
     * (Mandatory token (may be [NONE] or `isMissing` on syntax error) at slot [SLOT_NAME] = 1)
     */
    context(tree: ConcreteSyntaxTree)
    public val name: IdentTokenHandle
        get() = IdentTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_NAME))

    /**
     * (Optional token at slot [SLOT_L_PAREN] = 2)
     */
    context(tree: ConcreteSyntaxTree)
    public val lParen: LParenTokenHandle
        get() = LParenTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_L_PAREN))

    /**
     * (Optional token at slot [SLOT_ARG] = 3)
     */
    context(tree: ConcreteSyntaxTree)
    public val arg: IntLiteralTokenHandle
        get() = IntLiteralTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_ARG))

    /**
     * (Optional token at slot [SLOT_R_PAREN] = 4)
     */
    context(tree: ConcreteSyntaxTree)
    public val rParen: RParenTokenHandle
        get() = RParenTokenHandle(tree.getChildBySlot(nodeIndex, SLOT_R_PAREN))

    override fun toString(): String = if (isValid) """AnnotationHandle($nodeIndex)""" else "AnnotationHandle.NONE"

    public companion object {
        public const val KIND: Int = CstSchemaDslNodeKinds.ANNOTATION

        public const val SLOT_AT: Int = 0

        public const val SLOT_NAME: Int = 1

        public const val SLOT_L_PAREN: Int = 2

        public const val SLOT_ARG: Int = 3

        public const val SLOT_R_PAREN: Int = 4

        public val NONE: AnnotationHandle = AnnotationHandle(NULL_NODE_INDEX)

        public fun fromNode(node: CstNode): AnnotationHandle = AnnotationHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstNode] has kind [AnnotationHandle.KIND].
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.isAnnotation: Boolean
    get() = isValid && kind == AnnotationHandle.KIND

/**
 * Unchecked reinterpretation of this generic node handle into a [AnnotationHandle].
 */
public val CstNode.asAnnotation: AnnotationHandle
    get() = AnnotationHandle(nodeIndex)

/**
 * Safe conversion of this generic node handle into a [AnnotationHandle], returning [AnnotationHandle.NONE] if kinds do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstNode.asAnnotationOrNone: AnnotationHandle
    get() = if (isAnnotation) AnnotationHandle(nodeIndex) else AnnotationHandle.NONE

/**
 * Converts this generic node handle into a [AnnotationHandle].
 */
public fun CstNode.toAnnotation(): AnnotationHandle = AnnotationHandle(nodeIndex)

@JvmInline
public value class WhitespaceTokenHandle(
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

    override fun toString(): String = if (isValid) """WhitespaceTokenHandle($tokenIndex)""" else "WhitespaceTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.WHITESPACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_WHITESPACE

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: WhitespaceTokenHandle = WhitespaceTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): WhitespaceTokenHandle = WhitespaceTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): WhitespaceTokenHandle = WhitespaceTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [WhitespaceTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isWhitespace: Boolean
    get() = isValid && schemaId == WhitespaceTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [WhitespaceTokenHandle].
 */
public val CstToken.asWhitespace: WhitespaceTokenHandle
    get() = WhitespaceTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [WhitespaceTokenHandle], returning [WhitespaceTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asWhitespaceOrNone: WhitespaceTokenHandle
    get() = if (isWhitespace) WhitespaceTokenHandle(tokenIndex) else WhitespaceTokenHandle.NONE

/**
 * Converts this generic node handle into a [WhitespaceTokenHandle].
 */
public val CstNode.asWhitespaceToken: WhitespaceTokenHandle
    get() = WhitespaceTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [WhitespaceTokenHandle].
 */
public fun CstToken.toWhitespace(): WhitespaceTokenHandle = WhitespaceTokenHandle(tokenIndex)

@JvmInline
public value class LineCommentTokenHandle(
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

    override fun toString(): String = if (isValid) """LineCommentTokenHandle($tokenIndex)""" else "LineCommentTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.LINE_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_LINE_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: LineCommentTokenHandle = LineCommentTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LineCommentTokenHandle = LineCommentTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): LineCommentTokenHandle = LineCommentTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LineCommentTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLineComment: Boolean
    get() = isValid && schemaId == LineCommentTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LineCommentTokenHandle].
 */
public val CstToken.asLineComment: LineCommentTokenHandle
    get() = LineCommentTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LineCommentTokenHandle], returning [LineCommentTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLineCommentOrNone: LineCommentTokenHandle
    get() = if (isLineComment) LineCommentTokenHandle(tokenIndex) else LineCommentTokenHandle.NONE

/**
 * Converts this generic node handle into a [LineCommentTokenHandle].
 */
public val CstNode.asLineCommentToken: LineCommentTokenHandle
    get() = LineCommentTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [LineCommentTokenHandle].
 */
public fun CstToken.toLineComment(): LineCommentTokenHandle = LineCommentTokenHandle(tokenIndex)

@JvmInline
public value class BlockCommentTokenHandle(
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

    override fun toString(): String = if (isValid) """BlockCommentTokenHandle($tokenIndex)""" else "BlockCommentTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.BLOCK_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_BLOCK_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: BlockCommentTokenHandle = BlockCommentTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): BlockCommentTokenHandle = BlockCommentTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): BlockCommentTokenHandle = BlockCommentTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [BlockCommentTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isBlockComment: Boolean
    get() = isValid && schemaId == BlockCommentTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [BlockCommentTokenHandle].
 */
public val CstToken.asBlockComment: BlockCommentTokenHandle
    get() = BlockCommentTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [BlockCommentTokenHandle], returning [BlockCommentTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asBlockCommentOrNone: BlockCommentTokenHandle
    get() = if (isBlockComment) BlockCommentTokenHandle(tokenIndex) else BlockCommentTokenHandle.NONE

/**
 * Converts this generic node handle into a [BlockCommentTokenHandle].
 */
public val CstNode.asBlockCommentToken: BlockCommentTokenHandle
    get() = BlockCommentTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [BlockCommentTokenHandle].
 */
public fun CstToken.toBlockComment(): BlockCommentTokenHandle = BlockCommentTokenHandle(tokenIndex)

@JvmInline
public value class DocCommentTokenHandle(
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

    override fun toString(): String = if (isValid) """DocCommentTokenHandle($tokenIndex)""" else "DocCommentTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.DOC_COMMENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_DOC_COMMENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = true

        public val NONE: DocCommentTokenHandle = DocCommentTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): DocCommentTokenHandle = DocCommentTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): DocCommentTokenHandle = DocCommentTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [DocCommentTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isDocComment: Boolean
    get() = isValid && schemaId == DocCommentTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [DocCommentTokenHandle].
 */
public val CstToken.asDocComment: DocCommentTokenHandle
    get() = DocCommentTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [DocCommentTokenHandle], returning [DocCommentTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asDocCommentOrNone: DocCommentTokenHandle
    get() = if (isDocComment) DocCommentTokenHandle(tokenIndex) else DocCommentTokenHandle.NONE

/**
 * Converts this generic node handle into a [DocCommentTokenHandle].
 */
public val CstNode.asDocCommentToken: DocCommentTokenHandle
    get() = DocCommentTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [DocCommentTokenHandle].
 */
public fun CstToken.toDocComment(): DocCommentTokenHandle = DocCommentTokenHandle(tokenIndex)

@JvmInline
public value class IdentTokenHandle(
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

    override fun toString(): String = if (isValid) """IdentTokenHandle($tokenIndex)""" else "IdentTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.IDENT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_IDENT

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: IdentTokenHandle = IdentTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): IdentTokenHandle = IdentTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): IdentTokenHandle = IdentTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [IdentTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isIdent: Boolean
    get() = isValid && schemaId == IdentTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [IdentTokenHandle].
 */
public val CstToken.asIdent: IdentTokenHandle
    get() = IdentTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [IdentTokenHandle], returning [IdentTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asIdentOrNone: IdentTokenHandle
    get() = if (isIdent) IdentTokenHandle(tokenIndex) else IdentTokenHandle.NONE

/**
 * Converts this generic node handle into a [IdentTokenHandle].
 */
public val CstNode.asIdentToken: IdentTokenHandle
    get() = IdentTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [IdentTokenHandle].
 */
public fun CstToken.toIdent(): IdentTokenHandle = IdentTokenHandle(tokenIndex)

@JvmInline
public value class StringLiteralTokenHandle(
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

    override fun toString(): String = if (isValid) """StringLiteralTokenHandle($tokenIndex)""" else "StringLiteralTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.STRING_LITERAL

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_STRING_LITERAL

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: StringLiteralTokenHandle = StringLiteralTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): StringLiteralTokenHandle = StringLiteralTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): StringLiteralTokenHandle = StringLiteralTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [StringLiteralTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isStringLiteral: Boolean
    get() = isValid && schemaId == StringLiteralTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [StringLiteralTokenHandle].
 */
public val CstToken.asStringLiteral: StringLiteralTokenHandle
    get() = StringLiteralTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [StringLiteralTokenHandle], returning [StringLiteralTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asStringLiteralOrNone: StringLiteralTokenHandle
    get() = if (isStringLiteral) StringLiteralTokenHandle(tokenIndex) else StringLiteralTokenHandle.NONE

/**
 * Converts this generic node handle into a [StringLiteralTokenHandle].
 */
public val CstNode.asStringLiteralToken: StringLiteralTokenHandle
    get() = StringLiteralTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [StringLiteralTokenHandle].
 */
public fun CstToken.toStringLiteral(): StringLiteralTokenHandle = StringLiteralTokenHandle(tokenIndex)

@JvmInline
public value class IntLiteralTokenHandle(
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

    override fun toString(): String = if (isValid) """IntLiteralTokenHandle($tokenIndex)""" else "IntLiteralTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.INT_LITERAL

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_INT_LITERAL

        public val FIXED_TEXT: String? = null

        public const val IS_TRIVIA: Boolean = false

        public val NONE: IntLiteralTokenHandle = IntLiteralTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): IntLiteralTokenHandle = IntLiteralTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): IntLiteralTokenHandle = IntLiteralTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [IntLiteralTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isIntLiteral: Boolean
    get() = isValid && schemaId == IntLiteralTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [IntLiteralTokenHandle].
 */
public val CstToken.asIntLiteral: IntLiteralTokenHandle
    get() = IntLiteralTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [IntLiteralTokenHandle], returning [IntLiteralTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asIntLiteralOrNone: IntLiteralTokenHandle
    get() = if (isIntLiteral) IntLiteralTokenHandle(tokenIndex) else IntLiteralTokenHandle.NONE

/**
 * Converts this generic node handle into a [IntLiteralTokenHandle].
 */
public val CstNode.asIntLiteralToken: IntLiteralTokenHandle
    get() = IntLiteralTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [IntLiteralTokenHandle].
 */
public fun CstToken.toIntLiteral(): IntLiteralTokenHandle = IntLiteralTokenHandle(tokenIndex)

@JvmInline
public value class SchemaKwTokenHandle(
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

    override fun toString(): String = if (isValid) """SchemaKwTokenHandle($tokenIndex)""" else "SchemaKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.SCHEMA_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_SCHEMA_KW

        public const val FIXED_TEXT: String = "schema"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: SchemaKwTokenHandle = SchemaKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): SchemaKwTokenHandle = SchemaKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): SchemaKwTokenHandle = SchemaKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [SchemaKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isSchemaKw: Boolean
    get() = isValid && schemaId == SchemaKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [SchemaKwTokenHandle].
 */
public val CstToken.asSchemaKw: SchemaKwTokenHandle
    get() = SchemaKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [SchemaKwTokenHandle], returning [SchemaKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asSchemaKwOrNone: SchemaKwTokenHandle
    get() = if (isSchemaKw) SchemaKwTokenHandle(tokenIndex) else SchemaKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [SchemaKwTokenHandle].
 */
public val CstNode.asSchemaKwToken: SchemaKwTokenHandle
    get() = SchemaKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [SchemaKwTokenHandle].
 */
public fun CstToken.toSchemaKw(): SchemaKwTokenHandle = SchemaKwTokenHandle(tokenIndex)

@JvmInline
public value class PackageKwTokenHandle(
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

    override fun toString(): String = if (isValid) """PackageKwTokenHandle($tokenIndex)""" else "PackageKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.PACKAGE_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PACKAGE_KW

        public const val FIXED_TEXT: String = "package"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PackageKwTokenHandle = PackageKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PackageKwTokenHandle = PackageKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): PackageKwTokenHandle = PackageKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PackageKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPackageKw: Boolean
    get() = isValid && schemaId == PackageKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PackageKwTokenHandle].
 */
public val CstToken.asPackageKw: PackageKwTokenHandle
    get() = PackageKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PackageKwTokenHandle], returning [PackageKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPackageKwOrNone: PackageKwTokenHandle
    get() = if (isPackageKw) PackageKwTokenHandle(tokenIndex) else PackageKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [PackageKwTokenHandle].
 */
public val CstNode.asPackageKwToken: PackageKwTokenHandle
    get() = PackageKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [PackageKwTokenHandle].
 */
public fun CstToken.toPackageKw(): PackageKwTokenHandle = PackageKwTokenHandle(tokenIndex)

@JvmInline
public value class TriviaKwTokenHandle(
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

    override fun toString(): String = if (isValid) """TriviaKwTokenHandle($tokenIndex)""" else "TriviaKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.TRIVIA_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_TRIVIA_KW

        public const val FIXED_TEXT: String = "trivia"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: TriviaKwTokenHandle = TriviaKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): TriviaKwTokenHandle = TriviaKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): TriviaKwTokenHandle = TriviaKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [TriviaKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isTriviaKw: Boolean
    get() = isValid && schemaId == TriviaKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [TriviaKwTokenHandle].
 */
public val CstToken.asTriviaKw: TriviaKwTokenHandle
    get() = TriviaKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [TriviaKwTokenHandle], returning [TriviaKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asTriviaKwOrNone: TriviaKwTokenHandle
    get() = if (isTriviaKw) TriviaKwTokenHandle(tokenIndex) else TriviaKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [TriviaKwTokenHandle].
 */
public val CstNode.asTriviaKwToken: TriviaKwTokenHandle
    get() = TriviaKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [TriviaKwTokenHandle].
 */
public fun CstToken.toTriviaKw(): TriviaKwTokenHandle = TriviaKwTokenHandle(tokenIndex)

@JvmInline
public value class TokenKwTokenHandle(
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

    override fun toString(): String = if (isValid) """TokenKwTokenHandle($tokenIndex)""" else "TokenKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.TOKEN_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_TOKEN_KW

        public const val FIXED_TEXT: String = "token"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: TokenKwTokenHandle = TokenKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): TokenKwTokenHandle = TokenKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): TokenKwTokenHandle = TokenKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [TokenKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isTokenKw: Boolean
    get() = isValid && schemaId == TokenKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [TokenKwTokenHandle].
 */
public val CstToken.asTokenKw: TokenKwTokenHandle
    get() = TokenKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [TokenKwTokenHandle], returning [TokenKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asTokenKwOrNone: TokenKwTokenHandle
    get() = if (isTokenKw) TokenKwTokenHandle(tokenIndex) else TokenKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [TokenKwTokenHandle].
 */
public val CstNode.asTokenKwToken: TokenKwTokenHandle
    get() = TokenKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [TokenKwTokenHandle].
 */
public fun CstToken.toTokenKw(): TokenKwTokenHandle = TokenKwTokenHandle(tokenIndex)

@JvmInline
public value class OpenKwTokenHandle(
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

    override fun toString(): String = if (isValid) """OpenKwTokenHandle($tokenIndex)""" else "OpenKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.OPEN_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_OPEN_KW

        public const val FIXED_TEXT: String = "open"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: OpenKwTokenHandle = OpenKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): OpenKwTokenHandle = OpenKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): OpenKwTokenHandle = OpenKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [OpenKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isOpenKw: Boolean
    get() = isValid && schemaId == OpenKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [OpenKwTokenHandle].
 */
public val CstToken.asOpenKw: OpenKwTokenHandle
    get() = OpenKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [OpenKwTokenHandle], returning [OpenKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asOpenKwOrNone: OpenKwTokenHandle
    get() = if (isOpenKw) OpenKwTokenHandle(tokenIndex) else OpenKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [OpenKwTokenHandle].
 */
public val CstNode.asOpenKwToken: OpenKwTokenHandle
    get() = OpenKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [OpenKwTokenHandle].
 */
public fun CstToken.toOpenKw(): OpenKwTokenHandle = OpenKwTokenHandle(tokenIndex)

@JvmInline
public value class CategoryKwTokenHandle(
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

    override fun toString(): String = if (isValid) """CategoryKwTokenHandle($tokenIndex)""" else "CategoryKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.CATEGORY_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_CATEGORY_KW

        public const val FIXED_TEXT: String = "category"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: CategoryKwTokenHandle = CategoryKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): CategoryKwTokenHandle = CategoryKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): CategoryKwTokenHandle = CategoryKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [CategoryKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isCategoryKw: Boolean
    get() = isValid && schemaId == CategoryKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [CategoryKwTokenHandle].
 */
public val CstToken.asCategoryKw: CategoryKwTokenHandle
    get() = CategoryKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [CategoryKwTokenHandle], returning [CategoryKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asCategoryKwOrNone: CategoryKwTokenHandle
    get() = if (isCategoryKw) CategoryKwTokenHandle(tokenIndex) else CategoryKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [CategoryKwTokenHandle].
 */
public val CstNode.asCategoryKwToken: CategoryKwTokenHandle
    get() = CategoryKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [CategoryKwTokenHandle].
 */
public fun CstToken.toCategoryKw(): CategoryKwTokenHandle = CategoryKwTokenHandle(tokenIndex)

@JvmInline
public value class NodeKwTokenHandle(
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

    override fun toString(): String = if (isValid) """NodeKwTokenHandle($tokenIndex)""" else "NodeKwTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.NODE_KW

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_NODE_KW

        public const val FIXED_TEXT: String = "node"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: NodeKwTokenHandle = NodeKwTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): NodeKwTokenHandle = NodeKwTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): NodeKwTokenHandle = NodeKwTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [NodeKwTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isNodeKw: Boolean
    get() = isValid && schemaId == NodeKwTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [NodeKwTokenHandle].
 */
public val CstToken.asNodeKw: NodeKwTokenHandle
    get() = NodeKwTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [NodeKwTokenHandle], returning [NodeKwTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asNodeKwOrNone: NodeKwTokenHandle
    get() = if (isNodeKw) NodeKwTokenHandle(tokenIndex) else NodeKwTokenHandle.NONE

/**
 * Converts this generic node handle into a [NodeKwTokenHandle].
 */
public val CstNode.asNodeKwToken: NodeKwTokenHandle
    get() = NodeKwTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [NodeKwTokenHandle].
 */
public fun CstToken.toNodeKw(): NodeKwTokenHandle = NodeKwTokenHandle(tokenIndex)

@JvmInline
public value class ColonTokenHandle(
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

    override fun toString(): String = if (isValid) """ColonTokenHandle($tokenIndex)""" else "ColonTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.COLON

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_COLON

        public const val FIXED_TEXT: String = ":"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: ColonTokenHandle = ColonTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): ColonTokenHandle = ColonTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): ColonTokenHandle = ColonTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [ColonTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isColon: Boolean
    get() = isValid && schemaId == ColonTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [ColonTokenHandle].
 */
public val CstToken.asColon: ColonTokenHandle
    get() = ColonTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [ColonTokenHandle], returning [ColonTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asColonOrNone: ColonTokenHandle
    get() = if (isColon) ColonTokenHandle(tokenIndex) else ColonTokenHandle.NONE

/**
 * Converts this generic node handle into a [ColonTokenHandle].
 */
public val CstNode.asColonToken: ColonTokenHandle
    get() = ColonTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [ColonTokenHandle].
 */
public fun CstToken.toColon(): ColonTokenHandle = ColonTokenHandle(tokenIndex)

@JvmInline
public value class EqTokenHandle(
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

    override fun toString(): String = if (isValid) """EqTokenHandle($tokenIndex)""" else "EqTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.EQ

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_EQ

        public const val FIXED_TEXT: String = "="

        public const val IS_TRIVIA: Boolean = false

        public val NONE: EqTokenHandle = EqTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): EqTokenHandle = EqTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): EqTokenHandle = EqTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [EqTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isEq: Boolean
    get() = isValid && schemaId == EqTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [EqTokenHandle].
 */
public val CstToken.asEq: EqTokenHandle
    get() = EqTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [EqTokenHandle], returning [EqTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asEqOrNone: EqTokenHandle
    get() = if (isEq) EqTokenHandle(tokenIndex) else EqTokenHandle.NONE

/**
 * Converts this generic node handle into a [EqTokenHandle].
 */
public val CstNode.asEqToken: EqTokenHandle
    get() = EqTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [EqTokenHandle].
 */
public fun CstToken.toEq(): EqTokenHandle = EqTokenHandle(tokenIndex)

@JvmInline
public value class PipeTokenHandle(
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

    override fun toString(): String = if (isValid) """PipeTokenHandle($tokenIndex)""" else "PipeTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.PIPE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PIPE

        public const val FIXED_TEXT: String = "|"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PipeTokenHandle = PipeTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PipeTokenHandle = PipeTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): PipeTokenHandle = PipeTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PipeTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPipe: Boolean
    get() = isValid && schemaId == PipeTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PipeTokenHandle].
 */
public val CstToken.asPipe: PipeTokenHandle
    get() = PipeTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PipeTokenHandle], returning [PipeTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPipeOrNone: PipeTokenHandle
    get() = if (isPipe) PipeTokenHandle(tokenIndex) else PipeTokenHandle.NONE

/**
 * Converts this generic node handle into a [PipeTokenHandle].
 */
public val CstNode.asPipeToken: PipeTokenHandle
    get() = PipeTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [PipeTokenHandle].
 */
public fun CstToken.toPipe(): PipeTokenHandle = PipeTokenHandle(tokenIndex)

@JvmInline
public value class QuestionTokenHandle(
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
    public val isQuantifierOp: Boolean
        get() = true

    public val asQuantifierOp: QuantifierOpTokenHandle
        get() = QuantifierOpTokenHandle(tokenIndex)

    override fun toString(): String = if (isValid) """QuestionTokenHandle($tokenIndex)""" else "QuestionTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.QUESTION

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_QUESTION

        public const val FIXED_TEXT: String = "?"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: QuestionTokenHandle = QuestionTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): QuestionTokenHandle = QuestionTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): QuestionTokenHandle = QuestionTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [QuestionTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isQuestion: Boolean
    get() = isValid && schemaId == QuestionTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [QuestionTokenHandle].
 */
public val CstToken.asQuestion: QuestionTokenHandle
    get() = QuestionTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [QuestionTokenHandle], returning [QuestionTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asQuestionOrNone: QuestionTokenHandle
    get() = if (isQuestion) QuestionTokenHandle(tokenIndex) else QuestionTokenHandle.NONE

/**
 * Converts this generic node handle into a [QuestionTokenHandle].
 */
public val CstNode.asQuestionToken: QuestionTokenHandle
    get() = QuestionTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [QuestionTokenHandle].
 */
public fun CstToken.toQuestion(): QuestionTokenHandle = QuestionTokenHandle(tokenIndex)

@JvmInline
public value class StarTokenHandle(
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
    public val isQuantifierOp: Boolean
        get() = true

    public val asQuantifierOp: QuantifierOpTokenHandle
        get() = QuantifierOpTokenHandle(tokenIndex)

    override fun toString(): String = if (isValid) """StarTokenHandle($tokenIndex)""" else "StarTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.STAR

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_STAR

        public const val FIXED_TEXT: String = "*"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: StarTokenHandle = StarTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): StarTokenHandle = StarTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): StarTokenHandle = StarTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [StarTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isStar: Boolean
    get() = isValid && schemaId == StarTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [StarTokenHandle].
 */
public val CstToken.asStar: StarTokenHandle
    get() = StarTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [StarTokenHandle], returning [StarTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asStarOrNone: StarTokenHandle
    get() = if (isStar) StarTokenHandle(tokenIndex) else StarTokenHandle.NONE

/**
 * Converts this generic node handle into a [StarTokenHandle].
 */
public val CstNode.asStarToken: StarTokenHandle
    get() = StarTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [StarTokenHandle].
 */
public fun CstToken.toStar(): StarTokenHandle = StarTokenHandle(tokenIndex)

@JvmInline
public value class PlusTokenHandle(
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
    public val isQuantifierOp: Boolean
        get() = true

    public val asQuantifierOp: QuantifierOpTokenHandle
        get() = QuantifierOpTokenHandle(tokenIndex)

    override fun toString(): String = if (isValid) """PlusTokenHandle($tokenIndex)""" else "PlusTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.PLUS

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_PLUS

        public const val FIXED_TEXT: String = "+"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: PlusTokenHandle = PlusTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): PlusTokenHandle = PlusTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): PlusTokenHandle = PlusTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [PlusTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isPlus: Boolean
    get() = isValid && schemaId == PlusTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [PlusTokenHandle].
 */
public val CstToken.asPlus: PlusTokenHandle
    get() = PlusTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [PlusTokenHandle], returning [PlusTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asPlusOrNone: PlusTokenHandle
    get() = if (isPlus) PlusTokenHandle(tokenIndex) else PlusTokenHandle.NONE

/**
 * Converts this generic node handle into a [PlusTokenHandle].
 */
public val CstNode.asPlusToken: PlusTokenHandle
    get() = PlusTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [PlusTokenHandle].
 */
public fun CstToken.toPlus(): PlusTokenHandle = PlusTokenHandle(tokenIndex)

@JvmInline
public value class AtTokenHandle(
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

    override fun toString(): String = if (isValid) """AtTokenHandle($tokenIndex)""" else "AtTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.AT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_AT

        public const val FIXED_TEXT: String = "@"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: AtTokenHandle = AtTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): AtTokenHandle = AtTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): AtTokenHandle = AtTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [AtTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isAt: Boolean
    get() = isValid && schemaId == AtTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [AtTokenHandle].
 */
public val CstToken.asAt: AtTokenHandle
    get() = AtTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [AtTokenHandle], returning [AtTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asAtOrNone: AtTokenHandle
    get() = if (isAt) AtTokenHandle(tokenIndex) else AtTokenHandle.NONE

/**
 * Converts this generic node handle into a [AtTokenHandle].
 */
public val CstNode.asAtToken: AtTokenHandle
    get() = AtTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [AtTokenHandle].
 */
public fun CstToken.toAt(): AtTokenHandle = AtTokenHandle(tokenIndex)

@JvmInline
public value class LParenTokenHandle(
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

    override fun toString(): String = if (isValid) """LParenTokenHandle($tokenIndex)""" else "LParenTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.L_PAREN

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_L_PAREN

        public const val FIXED_TEXT: String = "("

        public const val IS_TRIVIA: Boolean = false

        public val NONE: LParenTokenHandle = LParenTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LParenTokenHandle = LParenTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): LParenTokenHandle = LParenTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LParenTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLParen: Boolean
    get() = isValid && schemaId == LParenTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LParenTokenHandle].
 */
public val CstToken.asLParen: LParenTokenHandle
    get() = LParenTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LParenTokenHandle], returning [LParenTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLParenOrNone: LParenTokenHandle
    get() = if (isLParen) LParenTokenHandle(tokenIndex) else LParenTokenHandle.NONE

/**
 * Converts this generic node handle into a [LParenTokenHandle].
 */
public val CstNode.asLParenToken: LParenTokenHandle
    get() = LParenTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [LParenTokenHandle].
 */
public fun CstToken.toLParen(): LParenTokenHandle = LParenTokenHandle(tokenIndex)

@JvmInline
public value class RParenTokenHandle(
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

    override fun toString(): String = if (isValid) """RParenTokenHandle($tokenIndex)""" else "RParenTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.R_PAREN

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_R_PAREN

        public const val FIXED_TEXT: String = ")"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: RParenTokenHandle = RParenTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): RParenTokenHandle = RParenTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): RParenTokenHandle = RParenTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [RParenTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isRParen: Boolean
    get() = isValid && schemaId == RParenTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [RParenTokenHandle].
 */
public val CstToken.asRParen: RParenTokenHandle
    get() = RParenTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [RParenTokenHandle], returning [RParenTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asRParenOrNone: RParenTokenHandle
    get() = if (isRParen) RParenTokenHandle(tokenIndex) else RParenTokenHandle.NONE

/**
 * Converts this generic node handle into a [RParenTokenHandle].
 */
public val CstNode.asRParenToken: RParenTokenHandle
    get() = RParenTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [RParenTokenHandle].
 */
public fun CstToken.toRParen(): RParenTokenHandle = RParenTokenHandle(tokenIndex)

@JvmInline
public value class LBraceTokenHandle(
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

    override fun toString(): String = if (isValid) """LBraceTokenHandle($tokenIndex)""" else "LBraceTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.L_BRACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_L_BRACE

        public const val FIXED_TEXT: String = "{"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: LBraceTokenHandle = LBraceTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): LBraceTokenHandle = LBraceTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): LBraceTokenHandle = LBraceTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [LBraceTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isLBrace: Boolean
    get() = isValid && schemaId == LBraceTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [LBraceTokenHandle].
 */
public val CstToken.asLBrace: LBraceTokenHandle
    get() = LBraceTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [LBraceTokenHandle], returning [LBraceTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asLBraceOrNone: LBraceTokenHandle
    get() = if (isLBrace) LBraceTokenHandle(tokenIndex) else LBraceTokenHandle.NONE

/**
 * Converts this generic node handle into a [LBraceTokenHandle].
 */
public val CstNode.asLBraceToken: LBraceTokenHandle
    get() = LBraceTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [LBraceTokenHandle].
 */
public fun CstToken.toLBrace(): LBraceTokenHandle = LBraceTokenHandle(tokenIndex)

@JvmInline
public value class RBraceTokenHandle(
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

    override fun toString(): String = if (isValid) """RBraceTokenHandle($tokenIndex)""" else "RBraceTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.R_BRACE

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_R_BRACE

        public const val FIXED_TEXT: String = "}"

        public const val IS_TRIVIA: Boolean = false

        public val NONE: RBraceTokenHandle = RBraceTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): RBraceTokenHandle = RBraceTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): RBraceTokenHandle = RBraceTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [RBraceTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isRBrace: Boolean
    get() = isValid && schemaId == RBraceTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [RBraceTokenHandle].
 */
public val CstToken.asRBrace: RBraceTokenHandle
    get() = RBraceTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [RBraceTokenHandle], returning [RBraceTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asRBraceOrNone: RBraceTokenHandle
    get() = if (isRBrace) RBraceTokenHandle(tokenIndex) else RBraceTokenHandle.NONE

/**
 * Converts this generic node handle into a [RBraceTokenHandle].
 */
public val CstNode.asRBraceToken: RBraceTokenHandle
    get() = RBraceTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [RBraceTokenHandle].
 */
public fun CstToken.toRBrace(): RBraceTokenHandle = RBraceTokenHandle(tokenIndex)

@JvmInline
public value class CommaTokenHandle(
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

    override fun toString(): String = if (isValid) """CommaTokenHandle($tokenIndex)""" else "CommaTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.COMMA

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_COMMA

        public const val FIXED_TEXT: String = ","

        public const val IS_TRIVIA: Boolean = false

        public val NONE: CommaTokenHandle = CommaTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): CommaTokenHandle = CommaTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): CommaTokenHandle = CommaTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [CommaTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isComma: Boolean
    get() = isValid && schemaId == CommaTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [CommaTokenHandle].
 */
public val CstToken.asComma: CommaTokenHandle
    get() = CommaTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [CommaTokenHandle], returning [CommaTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asCommaOrNone: CommaTokenHandle
    get() = if (isComma) CommaTokenHandle(tokenIndex) else CommaTokenHandle.NONE

/**
 * Converts this generic node handle into a [CommaTokenHandle].
 */
public val CstNode.asCommaToken: CommaTokenHandle
    get() = CommaTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [CommaTokenHandle].
 */
public fun CstToken.toComma(): CommaTokenHandle = CommaTokenHandle(tokenIndex)

@JvmInline
public value class DotTokenHandle(
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

    override fun toString(): String = if (isValid) """DotTokenHandle($tokenIndex)""" else "DotTokenHandle.NONE"

    public companion object {
        public const val ID: Int = CstSchemaDslTokenKinds.DOT

        public const val KIND: Int = CstSchemaDslTokenKinds.KIND_DOT

        public const val FIXED_TEXT: String = "."

        public const val IS_TRIVIA: Boolean = false

        public val NONE: DotTokenHandle = DotTokenHandle(NULL_TOKEN_INDEX)

        public fun fromToken(token: CstToken): DotTokenHandle = DotTokenHandle(token.tokenIndex)

        public fun fromNode(node: CstNode): DotTokenHandle = DotTokenHandle(node.nodeIndex)
    }
}

/**
 * True if this [CstToken] has schema ID [DotTokenHandle.ID].
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.isDot: Boolean
    get() = isValid && schemaId == DotTokenHandle.ID

/**
 * Unchecked reinterpretation of this generic token handle into a [DotTokenHandle].
 */
public val CstToken.asDot: DotTokenHandle
    get() = DotTokenHandle(tokenIndex)

/**
 * Safe conversion of this generic token handle into a [DotTokenHandle], returning [DotTokenHandle.NONE] if token IDs do not match.
 */
context(tree: ConcreteSyntaxTree)
public val CstToken.asDotOrNone: DotTokenHandle
    get() = if (isDot) DotTokenHandle(tokenIndex) else DotTokenHandle.NONE

/**
 * Converts this generic node handle into a [DotTokenHandle].
 */
public val CstNode.asDotToken: DotTokenHandle
    get() = DotTokenHandle(nodeIndex)

/**
 * Converts this generic token handle into a [DotTokenHandle].
 */
public fun CstToken.toDot(): DotTokenHandle = DotTokenHandle(tokenIndex)
