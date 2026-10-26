package dev.dialector.cst.schema.dsl

import dev.dialector.cst.ERROR_NODE_KIND
import dev.dialector.cst.decodeTokenSchemaId
import dev.dialector.cst.isTokenKind
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.Set

/**
 * Integer kind identifiers for [CstSchemaDsl] CST nodes.
 */
public object CstSchemaDslNodeKinds {
    public const val ERROR: Int = ERROR_NODE_KIND

    public const val UNKNOWN: Int = -1

    public const val SCHEMA_FILE: Int = 1

    public const val SCHEMA_HEADER: Int = 2

    public const val PACKAGE_HEADER: Int = 3

    public const val QUALIFIED_NAME: Int = 4

    public const val TOKEN_DECL: Int = 5

    public const val TOKEN_CATEGORY_DECL: Int = 6

    public const val TOKEN_MEMBER_REF: Int = 7

    public const val CATEGORY_DECL: Int = 8

    public const val NODE_DECL: Int = 9

    public const val CATEGORY_CLAUSE: Int = 10

    public const val FIELD_DECL: Int = 11

    public const val NAMED_TYPE_EXPR: Int = 12

    public const val LITERAL_TOKEN_TYPE_EXPR: Int = 13

    public const val INLINE_UNION_TYPE_EXPR: Int = 14

    public const val ANNOTATION: Int = 15

    public fun getKindName(kindId: Int): String = when (kindId) {
        ERROR -> "ErrorNode"
        SCHEMA_FILE -> "SchemaFile"
        SCHEMA_HEADER -> "SchemaHeader"
        PACKAGE_HEADER -> "PackageHeader"
        QUALIFIED_NAME -> "QualifiedName"
        TOKEN_DECL -> "TokenDecl"
        TOKEN_CATEGORY_DECL -> "TokenCategoryDecl"
        TOKEN_MEMBER_REF -> "TokenMemberRef"
        CATEGORY_DECL -> "CategoryDecl"
        NODE_DECL -> "NodeDecl"
        CATEGORY_CLAUSE -> "CategoryClause"
        FIELD_DECL -> "FieldDecl"
        NAMED_TYPE_EXPR -> "NamedTypeExpr"
        LITERAL_TOKEN_TYPE_EXPR -> "LiteralTokenTypeExpr"
        INLINE_UNION_TYPE_EXPR -> "InlineUnionTypeExpr"
        ANNOTATION -> "Annotation"
        else -> """UnknownNodeKind($kindId)"""
    }
}

/**
 * Integer kind identifiers for [CstSchemaDsl] CST tokens.
 * - Positive constants (`ID_*` / token names) are 1-based schema token IDs.
 * - Negative constants (`KIND_*`) are pre-encoded runtime token kinds stored in `ConcreteSyntaxTree.nodeKinds`.
 */
public object CstSchemaDslTokenKinds {
    public const val UNKNOWN: Int = 0

    public const val WHITESPACE: Int = 1

    public const val KIND_WHITESPACE: Int = -1_073_741_825

    public const val LINE_COMMENT: Int = 2

    public const val KIND_LINE_COMMENT: Int = -1_073_741_826

    public const val BLOCK_COMMENT: Int = 3

    public const val KIND_BLOCK_COMMENT: Int = -1_073_741_827

    public const val DOC_COMMENT: Int = 4

    public const val KIND_DOC_COMMENT: Int = -1_073_741_828

    public const val IDENT: Int = 5

    public const val KIND_IDENT: Int = -5

    public const val STRING_LITERAL: Int = 6

    public const val KIND_STRING_LITERAL: Int = -6

    public const val INT_LITERAL: Int = 7

    public const val KIND_INT_LITERAL: Int = -7

    public const val SCHEMA_KW: Int = 8

    public const val KIND_SCHEMA_KW: Int = -8

    public const val PACKAGE_KW: Int = 9

    public const val KIND_PACKAGE_KW: Int = -9

    public const val TRIVIA_KW: Int = 10

    public const val KIND_TRIVIA_KW: Int = -10

    public const val TOKEN_KW: Int = 11

    public const val KIND_TOKEN_KW: Int = -11

    public const val OPEN_KW: Int = 12

    public const val KIND_OPEN_KW: Int = -12

    public const val CATEGORY_KW: Int = 13

    public const val KIND_CATEGORY_KW: Int = -13

    public const val NODE_KW: Int = 14

    public const val KIND_NODE_KW: Int = -14

    public const val COLON: Int = 15

    public const val KIND_COLON: Int = -15

    public const val EQ: Int = 16

    public const val KIND_EQ: Int = -16

    public const val PIPE: Int = 17

    public const val KIND_PIPE: Int = -17

    public const val QUESTION: Int = 18

    public const val KIND_QUESTION: Int = -18

    public const val STAR: Int = 19

    public const val KIND_STAR: Int = -19

    public const val PLUS: Int = 20

    public const val KIND_PLUS: Int = -20

    public const val AT: Int = 21

    public const val KIND_AT: Int = -21

    public const val L_PAREN: Int = 22

    public const val KIND_L_PAREN: Int = -22

    public const val R_PAREN: Int = 23

    public const val KIND_R_PAREN: Int = -23

    public const val L_BRACE: Int = 24

    public const val KIND_L_BRACE: Int = -24

    public const val R_BRACE: Int = 25

    public const val KIND_R_BRACE: Int = -25

    public const val COMMA: Int = 26

    public const val KIND_COMMA: Int = -26

    public const val DOT: Int = 27

    public const val KIND_DOT: Int = -27

    /**
     * Resolves a token name from either its positive schema [idOrRuntimeKind] or negative runtime kind.
     */
    public fun getKindName(idOrRuntimeKind: Int): String {
        val schemaId = if (isTokenKind(idOrRuntimeKind)) decodeTokenSchemaId(idOrRuntimeKind) else idOrRuntimeKind
        return when (schemaId) {
            UNKNOWN -> "UNKNOWN"
            WHITESPACE -> "Whitespace"
            LINE_COMMENT -> "LineComment"
            BLOCK_COMMENT -> "BlockComment"
            DOC_COMMENT -> "DocComment"
            IDENT -> "Ident"
            STRING_LITERAL -> "StringLiteral"
            INT_LITERAL -> "IntLiteral"
            SCHEMA_KW -> "SchemaKw"
            PACKAGE_KW -> "PackageKw"
            TRIVIA_KW -> "TriviaKw"
            TOKEN_KW -> "TokenKw"
            OPEN_KW -> "OpenKw"
            CATEGORY_KW -> "CategoryKw"
            NODE_KW -> "NodeKw"
            COLON -> "Colon"
            EQ -> "Eq"
            PIPE -> "Pipe"
            QUESTION -> "Question"
            STAR -> "Star"
            PLUS -> "Plus"
            AT -> "At"
            L_PAREN -> "LParen"
            R_PAREN -> "RParen"
            L_BRACE -> "LBrace"
            R_BRACE -> "RBrace"
            COMMA -> "Comma"
            DOT -> "Dot"
            else -> """UnknownTokenKind($idOrRuntimeKind)"""
        }
    }

    /**
     * Returns the fixed literal text for a token by its positive schema ID or negative runtime kind.
     */
    public fun getTokenText(kindId: Int): String? {
        val schemaId = if (isTokenKind(kindId)) decodeTokenSchemaId(kindId) else kindId
        return when (schemaId) {
            SCHEMA_KW -> "schema"
            PACKAGE_KW -> "package"
            TRIVIA_KW -> "trivia"
            TOKEN_KW -> "token"
            OPEN_KW -> "open"
            CATEGORY_KW -> "category"
            NODE_KW -> "node"
            COLON -> ":"
            EQ -> "="
            PIPE -> "|"
            QUESTION -> "?"
            STAR -> "*"
            PLUS -> "+"
            AT -> "@"
            L_PAREN -> "("
            R_PAREN -> ")"
            L_BRACE -> "{"
            R_BRACE -> "}"
            COMMA -> ","
            DOT -> "."
            else -> null
        }
    }
}

/**
 * Category membership sets and lookup functions for [CstSchemaDsl] CST nodes.
 */
public object CstSchemaDslCategories {
    public val DECLARATION_KINDS: Set<Int> = setOf(
                CstSchemaDslNodeKinds.TOKEN_DECL,
                CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL,
                CstSchemaDslNodeKinds.CATEGORY_DECL,
                CstSchemaDslNodeKinds.NODE_DECL,
            )

    public val TYPE_EXPR_KINDS: Set<Int> = setOf(
                CstSchemaDslNodeKinds.NAMED_TYPE_EXPR,
                CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR,
                CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR,
            )

    public fun isDeclaration(kindId: Int): Boolean = kindId in DECLARATION_KINDS

    public fun isTypeExpr(kindId: Int): Boolean = kindId in TYPE_EXPR_KINDS
}

/**
 * Token category membership sets and lookup functions for [CstSchemaDsl] CST tokens.
 */
public object CstSchemaDslTokenCategories {
    public val QUANTIFIER_OP_IDS: Set<Int> = setOf(
                CstSchemaDslTokenKinds.QUESTION,
                CstSchemaDslTokenKinds.STAR,
                CstSchemaDslTokenKinds.PLUS,
            )

    public fun isQuantifierOp(idOrRuntimeKind: Int): Boolean {
        val schemaId = if (isTokenKind(idOrRuntimeKind)) decodeTokenSchemaId(idOrRuntimeKind) else idOrRuntimeKind
        return schemaId in QUANTIFIER_OP_IDS
    }
}
