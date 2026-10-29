package dev.dialector.cst.schema.dsl

import dev.dialector.cst.CstNode
import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.ConcreteSyntaxBuilder
import dev.dialector.cst.schema.CategorySchema
import dev.dialector.cst.schema.CstSchema
import dev.dialector.cst.schema.CstSchemaException
import dev.dialector.cst.schema.FieldSchema
import dev.dialector.cst.schema.FieldType
import dev.dialector.cst.schema.NodeSchema
import dev.dialector.cst.schema.TokenCategorySchema
import dev.dialector.cst.schema.TokenSchema
import dev.dialector.cst.schema.dsl.gen.AnnotationList
import dev.dialector.cst.schema.dsl.gen.AnnotationSyntax
import dev.dialector.cst.schema.dsl.gen.CategoryClauseSyntax
import dev.dialector.cst.schema.dsl.gen.CategoryDeclSyntax
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslNodeKinds
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslSchema
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslTokenKinds
import dev.dialector.cst.schema.dsl.gen.FieldDeclSyntax
import dev.dialector.cst.schema.dsl.gen.InlineUnionTypeExprSyntax
import dev.dialector.cst.schema.dsl.gen.LiteralTokenTypeExprSyntax
import dev.dialector.cst.schema.dsl.gen.NamedTypeExprSyntax
import dev.dialector.cst.schema.dsl.gen.NodeDeclSyntax
import dev.dialector.cst.schema.dsl.gen.PackageHeaderSyntax
import dev.dialector.cst.schema.dsl.gen.QualifiedNameSyntax
import dev.dialector.cst.schema.dsl.gen.SchemaFileSyntax
import dev.dialector.cst.schema.dsl.gen.SchemaHeaderSyntax
import dev.dialector.cst.schema.dsl.gen.TokenCategoryDeclSyntax
import dev.dialector.cst.schema.dsl.gen.TokenDeclSyntax
import dev.dialector.cst.schema.dsl.gen.TokenMemberRefSyntax
import dev.dialector.cst.schema.dsl.gen.addAnnotation
import dev.dialector.cst.schema.dsl.gen.addCategoryClause
import dev.dialector.cst.schema.dsl.gen.addCategoryDecl
import dev.dialector.cst.schema.dsl.gen.addFieldDecl
import dev.dialector.cst.schema.dsl.gen.addInlineUnionTypeExpr
import dev.dialector.cst.schema.dsl.gen.addLiteralTokenTypeExpr
import dev.dialector.cst.schema.dsl.gen.addNamedTypeExpr
import dev.dialector.cst.schema.dsl.gen.addNodeDecl
import dev.dialector.cst.schema.dsl.gen.addPackageHeader
import dev.dialector.cst.schema.dsl.gen.addQualifiedName
import dev.dialector.cst.schema.dsl.gen.addSchemaHeader
import dev.dialector.cst.schema.dsl.gen.addTokenCategoryDecl
import dev.dialector.cst.schema.dsl.gen.addTokenDecl
import dev.dialector.cst.schema.dsl.gen.addTokenMemberRef
import dev.dialector.cst.schema.dsl.gen.asSchemaFileOrNone

/**
 * Self-hosted parser and extractor for the Dialector `.cst` Schema DSL (`CstSchemaDsl`).
 *
 * - [parseToCst] lexes and parses any `.cst` source text directly into a lossless [ConcreteSyntaxTree]
 *   conforming to the `CstSchemaDsl` schema (including doc comments, trivia, error nodes, and recovery).
 * - [extractSchema] walks a [ConcreteSyntaxTree] using the generated `CstSchemaDsl` value-class syntax types
 *   ([dev.dialector.cst.schema.dsl.gen.SchemaFileSyntax], [dev.dialector.cst.schema.dsl.gen.NodeDeclSyntax], [dev.dialector.cst.schema.dsl.gen.FieldDeclSyntax], etc.) to build a validated [CstSchema].
 * - [parse] runs both phases in one step.
 * - [selfHostedSchema] returns the [CstSchema] of `CstSchemaDsl` itself parsed from [CST_SCHEMA_DSL_SOURCE].
 */
public object CstSchemaParser {

    /**
     * The canonical `.cst` source defining the `CstSchemaDsl` language itself.
     */
    public val CST_SCHEMA_DSL_SOURCE: String = """
// Self-hosting CST Schema specification for the Dialector `.cst` Schema DSL.
schema CstSchemaDsl
package dev.dialector.cst.schema.dsl.gen

// --- Trivia Tokens ---
trivia Whitespace
trivia LineComment
trivia BlockComment
trivia DocComment

// --- Lexical Tokens ---
token Ident
token StringLiteral
token IntLiteral

// --- Keywords ---
token SchemaKw   = "schema"
token PackageKw  = "package"
token TriviaKw   = "trivia"
token TokenKw    = "token"
token OpenKw     = "open"
token CategoryKw = "category"
token NodeKw     = "node"

// --- Punctuation & Operators ---
token Colon    = ":"
token Eq       = "="
token Pipe     = "|"
token Question = "?"
token Star     = "*"
token Plus     = "+"
token At       = "@"
token LParen   = "("
token RParen   = ")"
token LBrace   = "{"
token RBrace   = "}"
token Comma    = ","
token Dot      = "."

// --- Token Categories ---
token category QuantifierOp = '?' | '*' | '+'

// --- Node Categories ---
open category Declaration

category TypeExpr =
    NamedTypeExpr
  | LiteralTokenTypeExpr
  | InlineUnionTypeExpr

// --- Root & Header Nodes ---
node SchemaFile {
    schemaHeader: SchemaHeader?
    packageHeader: PackageHeader?
    declarations: Declaration*
}

node SchemaHeader {
    schemaKw: 'schema'
    name: Ident
}

node PackageHeader {
    packageKw: 'package'
    name: QualifiedName
}

node QualifiedName {
    parts: Ident*
    dots: '.'*
}

// --- Declarations ---
node TokenDecl : Declaration {
    triviaKw: 'trivia'?
    tokenKw: 'token'?
    name: Ident
    categories: CategoryClause?
    eq: '='?
    fixedText: StringLiteral?
    annotations: Annotation*
}

node TokenCategoryDecl : Declaration {
    openKw: 'open'?
    tokenKw: 'token'
    categoryKw: 'category'
    name: Ident
    superCategories: CategoryClause?
    eq: '='?
    members: TokenMemberRef*
    pipes: '|'*
}

node TokenMemberRef {
    name: Ident?
    literal: StringLiteral?
}

node CategoryDecl : Declaration {
    openKw: 'open'?
    categoryKw: 'category'
    name: Ident
    superCategories: CategoryClause?
    eq: '='?
    members: Ident*
    pipes: '|'*
}

node NodeDecl : Declaration {
    nodeKw: 'node'
    name: Ident
    categories: CategoryClause?
    annotations: Annotation*
    lBrace: '{'?
    fields: FieldDecl*
    rBrace: '}'?
}

node CategoryClause {
    colon: ':'
    names: Ident*
    commas: ','*
}

node FieldDecl {
    name: Ident
    colon: ':'
    typeExpr: TypeExpr
    quantifier: QuantifierOp?
    annotations: Annotation*
}

// --- Field Type Expressions ---
node NamedTypeExpr {
    name: Ident
}

node LiteralTokenTypeExpr {
    literal: StringLiteral
}

node InlineUnionTypeExpr {
    lParen: '('
    members: TokenMemberRef*
    pipes: '|'*
    rParen: ')'
}

node Annotation {
    at: '@'
    name: Ident
    lParen: '('?
    arg: IntLiteral?
    rParen: ')'?
}
""".trimIndent()

    /**
     * Parses a `.cst` schema definition string into a validated [CstSchema].
     */
    public fun parse(
        source: String,
        defaultSchemaName: String = "DialectorCst",
        defaultPackageName: String = "dev.dialector.cst.generated",
    ): CstSchema {
        val cst = parseToCst(source)
        if (cst.hasErrors) {
            val messages = cst.diagnostics.map {
                "Syntax error at ${it.startOffset}..${it.endOffset} [${it.code}]: ${it.message}"
            }.ifEmpty { listOf("Syntax error in .cst schema input") }
            throw CstSchemaException(messages)
        }
        return extractSchema(
            cst = cst,
            defaultSchemaName = defaultSchemaName,
            defaultPackageName = defaultPackageName,
        )
    }

    /**
     * Returns the self-hosted [CstSchema] for `CstSchemaDsl` by parsing [CST_SCHEMA_DSL_SOURCE].
     */
    public fun selfHostedSchema(): CstSchema = parse(CST_SCHEMA_DSL_SOURCE)

    /**
     * Lexes and parses `.cst` source text into a lossless [ConcreteSyntaxTree] using `CstSchemaDsl` node & token kinds.
     */
    public fun parseToCst(source: String): ConcreteSyntaxTree {
        val rawTokens = tokenize(source)
        val builder = ConcreteSyntaxBuilder(schema = CstSchemaDslSchema)
        val parser = DslParser(source, rawTokens, builder)
        parser.parseSchemaFile()
        return builder.build(source, schema = CstSchemaDslSchema)
    }

    /**
     * Extracts a semantic [CstSchema] from a [ConcreteSyntaxTree] produced by [parseToCst],
     * using the generated `CstSchemaDsl` value-class handles and context parameters.
     */
    public fun extractSchema(
        cst: ConcreteSyntaxTree,
        defaultSchemaName: String = "DialectorCst",
        defaultPackageName: String = "dev.dialector.cst.generated",
    ): CstSchema = with(cst) {
        val rootFile = cst.root.asSchemaFileOrNone
        if (rootFile.isNone) {
            throw CstSchemaException(listOf("Root node is not a SchemaFile"))
        }

        val schemaName = rootFile.schemaHeader.let {
            if (it.isValid && it.name.isValid && !it.name.asNode.isMissing) it.name.asNode.text else defaultSchemaName
        }

        val packageName = rootFile.packageHeader.let { hdr ->
            if (hdr.isValid && hdr.name.isValid) {
                val sb = StringBuilder()
                hdr.name.parts.forEach { part ->
                    if (part.isValid && !part.asNode.isMissing) {
                        if (sb.isNotEmpty()) sb.append('.')
                        sb.append(part.asNode.text)
                    }
                }
                if (sb.isNotEmpty()) sb.toString() else defaultPackageName
            } else {
                defaultPackageName
            }
        }

        // Accumulators for merging top-down (`category A = B | C`) and bottom-up (`node B : A`) definitions
        data class MutableTokenDef(
            val name: String,
            var explicitId: Int = -1,
            var fixedText: String? = null,
            var isTrivia: Boolean = false,
            val categories: MutableSet<String> = linkedSetOf(),
            var doc: String? = null,
        )

        data class MutableTokenCatDef(
            val name: String,
            var isOpen: Boolean = false,
            var hasEq: Boolean = false,
            val superCategories: MutableSet<String> = linkedSetOf(),
            val memberRefs: MutableList<Pair<String?, String?>> = mutableListOf(), // (identName, literalText)
            val resolvedMembers: MutableSet<String> = linkedSetOf(),
            var doc: String? = null,
        )

        data class MutableNodeCatDef(
            val name: String,
            var isOpen: Boolean = false,
            var hasEq: Boolean = false,
            val superCategories: MutableSet<String> = linkedSetOf(),
            val memberNames: MutableSet<String> = linkedSetOf(),
            var doc: String? = null,
        )

        val semanticErrors = mutableListOf<String>()
        val tokenDefs = linkedMapOf<String, MutableTokenDef>()
        val literalToTokenName = linkedMapOf<String, String>()
        val tokenCatDefs = linkedMapOf<String, MutableTokenCatDef>()
        val nodeCatDefs = linkedMapOf<String, MutableNodeCatDef>()
        val nodeDecls = mutableListOf<NodeDeclSyntax>()

        // Pass 1: Collect all declarations
        rootFile.declarations.forEach { decl ->
            val doc = extractLeadingDocComment(decl.asNode)
            when {
                decl.isTokenDecl -> {
                    val td = decl.asTokenDecl
                    val tName = td.name.asNode.text
                    if (tName.isNotBlank()) {
                        val isTrivia = td.triviaKw.isValid && !td.triviaKw.asNode.isMissing
                        val fixedText = td.fixedText.let {
                            if (it.isValid && !it.asNode.isMissing) unquoteStringLiteral(it.asNode.text) else null
                        }
                        val explicitId = extractAnnotationInt(td.annotations, "id") ?: -1
                        val entry = tokenDefs.getOrPut(tName) { MutableTokenDef(tName) }
                        if (explicitId > 0) entry.explicitId = explicitId
                        if (fixedText != null) {
                            entry.fixedText = fixedText
                            literalToTokenName.putIfAbsent(fixedText, tName)
                        }
                        if (isTrivia) entry.isTrivia = true
                        if (doc != null) entry.doc = doc
                        if (td.categories.isValid) {
                            td.categories.names.forEach { catIdent ->
                                if (catIdent.isValid && !catIdent.asNode.isMissing) {
                                    entry.categories.add(catIdent.asNode.text)
                                }
                            }
                        }
                    }
                }

                decl.isTokenCategoryDecl -> {
                    val tcd = decl.asTokenCategoryDecl
                    val tcName = tcd.name.asNode.text
                    if (tcName.isNotBlank()) {
                        val entry = tokenCatDefs.getOrPut(tcName) { MutableTokenCatDef(tcName) }
                        val isOpen = tcd.openKw.isValid && !tcd.openKw.asNode.isMissing
                        val hasEq = tcd.eq.isValid && !tcd.eq.asNode.isMissing
                        if (isOpen) entry.isOpen = true
                        if (hasEq) entry.hasEq = true
                        if (doc != null) entry.doc = doc
                        if (tcd.superCategories.isValid) {
                            tcd.superCategories.names.forEach { sup ->
                                if (sup.isValid && !sup.asNode.isMissing) entry.superCategories.add(sup.asNode.text)
                            }
                        }
                        tcd.members.forEach { mem ->
                            val mName = if (mem.name.isValid && !mem.name.asNode.isMissing) mem.name.asNode.text else null
                            val mLit = if (mem.literal.isValid && !mem.literal.asNode.isMissing) unquoteStringLiteral(mem.literal.asNode.text) else null
                            entry.memberRefs.add(mName to mLit)
                        }
                        if (isOpen && hasEq && entry.memberRefs.isEmpty()) {
                            semanticErrors.add("Open token category '$tcName' cannot define a '= ...' member union")
                        }
                    }
                }

                decl.isCategoryDecl -> {
                    val cd = decl.asCategoryDecl
                    val cName = cd.name.asNode.text
                    if (cName.isNotBlank()) {
                        val entry = nodeCatDefs.getOrPut(cName) { MutableNodeCatDef(cName) }
                        val isOpen = cd.openKw.isValid && !cd.openKw.asNode.isMissing
                        val hasEq = cd.eq.isValid && !cd.eq.asNode.isMissing
                        if (isOpen) entry.isOpen = true
                        if (hasEq) entry.hasEq = true
                        if (doc != null) entry.doc = doc
                        if (cd.superCategories.isValid) {
                            cd.superCategories.names.forEach { sup ->
                                if (sup.isValid && !sup.asNode.isMissing) entry.superCategories.add(sup.asNode.text)
                            }
                        }
                        cd.members.forEach { mem ->
                            if (mem.isValid && !mem.asNode.isMissing) entry.memberNames.add(mem.asNode.text)
                        }
                        if (isOpen && hasEq && entry.memberNames.isEmpty()) {
                            semanticErrors.add("Open category '$cName' cannot define a '= ...' member union")
                        }
                    }
                }

                decl.isNodeDecl -> {
                    nodeDecls.add(decl.asNodeDecl)
                }
            }
        }

        // Helper to resolve or auto-synthesize a token by fixed literal text (e.g. '+' or 'schema')
        fun resolveOrSynthesizeLiteralToken(literalText: String): String {
            literalToTokenName[literalText]?.let { return it }
            val synthesizedName = synthesizeTokenNameFromLiteral(literalText, tokenDefs.keys)
            val newTok = MutableTokenDef(
                name = synthesizedName,
                fixedText = literalText,
                isTrivia = false,
            )
            tokenDefs[synthesizedName] = newTok
            literalToTokenName[literalText] = synthesizedName
            return synthesizedName
        }

        // Pass 2: Resolve top-down token category members (`token category Op = '+' | '-' | OtherTokenCat`)
        for ((_, tcDef) in tokenCatDefs) {
            for ((memberIdent, memberLiteral) in tcDef.memberRefs) {
                if (memberLiteral != null) {
                    val tokName = resolveOrSynthesizeLiteralToken(memberLiteral)
                    tcDef.resolvedMembers.add(tokName)
                } else if (memberIdent != null) {
                    if (!tokenDefs.containsKey(memberIdent) && !tokenCatDefs.containsKey(memberIdent)) {
                        // Auto-register variable token if referenced in token category union
                        tokenDefs[memberIdent] = MutableTokenDef(name = memberIdent)
                    }
                    tcDef.resolvedMembers.add(memberIdent)
                }
            }
        }

        // Pass 3: Extract nodes and fields (including inline token unions `op: ('+' | '-')`)
        val usedNodeIds = mutableSetOf<Int>()
        for (nd in nodeDecls) {
            val expId = extractAnnotationInt(nd.annotations, "id")
            if (expId != null && expId > 0) usedNodeIds.add(expId)
        }

        var nextAutoNodeId = 1
        val extractedNodes = mutableListOf<NodeSchema>()

        for (nd in nodeDecls) {
            val nName = nd.name.asNode.text
            if (nName.isBlank()) continue
            val nodeDoc = extractLeadingDocComment(nd.asNode)
            val explicitId = extractAnnotationInt(nd.annotations, "id")
            val resolvedNodeId = if (explicitId != null && explicitId > 0) {
                explicitId
            } else {
                while (usedNodeIds.contains(nextAutoNodeId)) nextAutoNodeId++
                usedNodeIds.add(nextAutoNodeId)
                nextAutoNodeId++
            }

            val categories = linkedSetOf<String>()
            if (nd.categories.isValid) {
                nd.categories.names.forEach { catIdent ->
                    if (catIdent.isValid && !catIdent.asNode.isMissing) categories.add(catIdent.asNode.text)
                }
            }

            // Extract fields and resolve slots
            val rawFields = mutableListOf<FieldSchema>()
            nd.fields.forEach { fd ->
                val fName = fd.name.asNode.text
                if (fName.isNotBlank() && fd.typeExpr.isValid) {
                    val fDoc = extractLeadingDocComment(fd.asNode)
                    val explicitSlot = extractAnnotationInt(fd.annotations, "slot") ?: -1
                    val quantText = if (fd.quantifier.isValid && !fd.quantifier.asNode.isMissing) fd.quantifier.asNode.text else ""
                    val isList = quantText == "*" || quantText == "+"
                    val isNonEmpty = quantText == "+"
                    val isOptional = quantText == "?"

                    val typeExpr = fd.typeExpr
                    val fieldType: FieldType = when {
                        typeExpr.isLiteralTokenTypeExpr -> {
                            val lit = unquoteStringLiteral(typeExpr.asLiteralTokenTypeExpr.literal.asNode.text)
                            val tokName = resolveOrSynthesizeLiteralToken(lit)
                            if (isList) {
                                FieldType.ChildTokenList(tokenTypeName = tokName, isNonEmpty = isNonEmpty)
                            } else {
                                FieldType.ChildToken(tokenTypeName = tokName, isOptional = isOptional)
                            }
                        }

                        typeExpr.isInlineUnionTypeExpr -> {
                            val unionExpr = typeExpr.asInlineUnionTypeExpr
                            val capField = fName.replaceFirstChar { it.uppercaseChar() }
                            val synthCatName = "${nName}${capField}Token"
                            val catEntry = tokenCatDefs.getOrPut(synthCatName) {
                                MutableTokenCatDef(
                                    name = synthCatName,
                                    doc = "Synthesized inline token union for [$nName.$fName].",
                                )
                            }
                            unionExpr.members.forEach { mem ->
                                val mName = if (mem.name.isValid && !mem.name.asNode.isMissing) mem.name.asNode.text else null
                                val mLit = if (mem.literal.isValid && !mem.literal.asNode.isMissing) unquoteStringLiteral(mem.literal.asNode.text) else null
                                if (mLit != null) {
                                    val tokName = resolveOrSynthesizeLiteralToken(mLit)
                                    catEntry.resolvedMembers.add(tokName)
                                } else if (mName != null) {
                                    if (!tokenDefs.containsKey(mName) && !tokenCatDefs.containsKey(mName)) {
                                        tokenDefs[mName] = MutableTokenDef(name = mName)
                                    }
                                    catEntry.resolvedMembers.add(mName)
                                }
                            }
                            if (isList) {
                                FieldType.ChildTokenList(tokenTypeName = synthCatName, isNonEmpty = isNonEmpty)
                            } else {
                                FieldType.ChildToken(tokenTypeName = synthCatName, isOptional = isOptional)
                            }
                        }

                        typeExpr.isNamedTypeExpr -> {
                            val targetName = typeExpr.asNamedTypeExpr.name.asNode.text
                            val isTokenTarget = targetName == "CstToken" ||
                                targetName == "Token" ||
                                tokenDefs.containsKey(targetName) ||
                                tokenCatDefs.containsKey(targetName)
                            val normalizedTokenTarget = if (targetName == "Token" || targetName == "CstToken") null else targetName
                            val normalizedNodeTarget = if (targetName == "Node") "CstNode" else targetName

                            if (isTokenTarget) {
                                if (isList) {
                                    FieldType.ChildTokenList(tokenTypeName = normalizedTokenTarget, isNonEmpty = isNonEmpty)
                                } else {
                                    FieldType.ChildToken(tokenTypeName = normalizedTokenTarget, isOptional = isOptional)
                                }
                            } else {
                                if (isList) {
                                    FieldType.ChildNodeList(nodeTypeName = normalizedNodeTarget, isNonEmpty = isNonEmpty)
                                } else {
                                    FieldType.ChildNode(nodeTypeName = normalizedNodeTarget, isOptional = isOptional)
                                }
                            }
                        }

                        else -> FieldType.ChildNode("CstNode", isOptional = true)
                    }

                    rawFields.add(
                        FieldSchema(
                            name = fName,
                            type = fieldType,
                            slot = explicitSlot,
                            doc = fDoc,
                        )
                    )
                }
            }

            // Assign deterministic slots
            val usedSlots = mutableSetOf<Int>()
            for (f in rawFields) {
                if (f.slot >= 0) usedSlots.add(f.slot)
            }
            var nextSlot = 0
            val resolvedFields = rawFields.map { f ->
                if (f.slot >= 0) {
                    f
                } else {
                    while (usedSlots.contains(nextSlot)) nextSlot++
                    usedSlots.add(nextSlot)
                    f.copy(slot = nextSlot++)
                }
            }

            extractedNodes.add(
                NodeSchema(
                    name = nName,
                    id = resolvedNodeId,
                    categories = categories.toList(),
                    fields = resolvedFields,
                    doc = nodeDoc,
                )
            )
        }

        // Pass 5: Finalize Token IDs
        val usedTokenIds = mutableSetOf<Int>()
        for (td in tokenDefs.values) {
            if (td.explicitId > 0) usedTokenIds.add(td.explicitId)
        }
        var nextAutoTokenId = 1
        val finalTokens = tokenDefs.values.map { td ->
            val id = if (td.explicitId > 0) {
                td.explicitId
            } else {
                while (usedTokenIds.contains(nextAutoTokenId)) nextAutoTokenId++
                usedTokenIds.add(nextAutoTokenId)
                nextAutoTokenId++
            }
            TokenSchema(
                name = td.name,
                id = id,
                fixedText = td.fixedText,
                isTrivia = td.isTrivia,
                categories = td.categories.toList(),
                doc = td.doc,
            )
        }

        val finalTokenCategories = tokenCatDefs.values.map { tcd ->
            TokenCategorySchema(
                name = tcd.name,
                superCategories = tcd.superCategories.toList(),
                members = tcd.resolvedMembers.toList(),
                isOpen = tcd.isOpen,
                doc = tcd.doc,
            )
        }

        val finalNodeCategories = nodeCatDefs.values.map { ncd ->
            CategorySchema(
                name = ncd.name,
                superCategories = ncd.superCategories.toList(),
                members = ncd.memberNames.toList(),
                isOpen = ncd.isOpen,
                doc = ncd.doc,
            )
        }

        val schema = CstSchema(
            name = schemaName,
            packageName = packageName,
            tokens = finalTokens,
            tokenCategories = finalTokenCategories,
            categories = finalNodeCategories,
            nodes = extractedNodes,
        )
        return try {
            val validated = schema.validate()
            if (semanticErrors.isNotEmpty()) {
                throw CstSchemaException(semanticErrors)
            }
            validated
        } catch (e: CstSchemaException) {
            if (semanticErrors.isNotEmpty() && e.errors !== semanticErrors) {
                throw CstSchemaException(semanticErrors + e.errors)
            }
            throw e
        }
    }

    context(tree: ConcreteSyntaxTree)
    private fun extractAnnotationInt(annotations: AnnotationList, targetName: String): Int? {
        annotations.forEach { ann ->
            if (ann.name.asNode.text == targetName && ann.arg.isValid && !ann.arg.asNode.isMissing) {
                return ann.arg.asNode.text.toIntOrNull()
            }
        }
        return null
    }

    context(tree: ConcreteSyntaxTree)
    private fun extractLeadingDocComment(node: CstNode): String? {
        // Collect contiguous preceding DOC_COMMENT trivia siblings (separated only by whitespace without blank lines)
        val docParts = mutableListOf<String>()
        var prev = node.prevSibling
        while (prev.isValid && prev.isTrivia) {
            when (prev.asToken.schemaId) {
                CstSchemaDslTokenKinds.DOC_COMMENT -> {
                    val cleaned = cleanDocComment(prev.text)
                    if (cleaned.isNotEmpty()) {
                        docParts.add(cleaned)
                    }
                }
                CstSchemaDslTokenKinds.WHITESPACE -> {
                    if (hasBlankLine(prev.text)) {
                        break
                    }
                }
                else -> break
            }
            prev = prev.prevSibling
        }
        if (docParts.isNotEmpty()) {
            return docParts.asReversed().joinToString("\n")
        }

        // Fallback: check leading trivia children inside the node
        val childParts = mutableListOf<String>()
        var i = 0
        val count = node.childCount
        while (i < count) {
            val child = node.childAt(i)
            if (!child.isValid || !child.isTrivia) break
            when (child.asToken.schemaId) {
                CstSchemaDslTokenKinds.DOC_COMMENT -> {
                    val cleaned = cleanDocComment(child.text)
                    if (cleaned.isNotEmpty()) {
                        childParts.add(cleaned)
                    }
                }
                CstSchemaDslTokenKinds.WHITESPACE -> {
                    if (hasBlankLine(child.text)) {
                        childParts.clear()
                    }
                }
                else -> childParts.clear()
            }
            i++
        }
        if (childParts.isNotEmpty()) {
            return childParts.joinToString("\n")
        }
        return null
    }

    private fun hasBlankLine(whitespace: String): Boolean {
        var newlines = 0
        var i = 0
        while (i < whitespace.length) {
            val c = whitespace[i]
            if (c == '\r') {
                newlines++
                if (i + 1 < whitespace.length && whitespace[i + 1] == '\n') {
                    i++
                }
            } else if (c == '\n') {
                newlines++
            }
            if (newlines >= 2) return true
            i++
        }
        return false
    }

    private fun cleanDocComment(raw: String): String {
        if (raw.startsWith("///")) {
            return raw.removePrefix("///").trim()
        }
        if (raw.startsWith("/**") && raw.endsWith("*/")) {
            return raw.substring(3, raw.length - 2)
                .lines()
                .map { it.trim().removePrefix("*").trim() }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        }
        return raw.trim()
    }

    private fun unquoteStringLiteral(raw: String): String {
        if (raw.length < 2) return raw
        val inner = raw.substring(1, raw.length - 1)
        val sb = StringBuilder(inner.length)
        var i = 0
        while (i < inner.length) {
            val c = inner[i]
            if (c == '\\' && i + 1 < inner.length) {
                when (val next = inner[i + 1]) {
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    '\\' -> sb.append('\\')
                    '\'' -> sb.append('\'')
                    '"' -> sb.append('"')
                    else -> sb.append(next)
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private val SYMBOL_NAME_MAP = mapOf(
        "+" to "Plus",
        "-" to "Minus",
        "*" to "Star",
        "/" to "Slash",
        "%" to "Percent",
        "=" to "Eq",
        "==" to "EqEq",
        "!=" to "BangEq",
        "!" to "Bang",
        "<" to "Lt",
        "<=" to "LtEq",
        ">" to "Gt",
        ">=" to "GtEq",
        "&&" to "AmpAmp",
        "||" to "PipePipe",
        "&" to "Amp",
        "|" to "Pipe",
        "^" to "Caret",
        "~" to "Tilde",
        "?" to "Question",
        ":" to "Colon",
        "::" to "ColonColon",
        ";" to "Semicolon",
        "," to "Comma",
        "." to "Dot",
        ".." to "DotDot",
        "(" to "LParen",
        ")" to "RParen",
        "{" to "LBrace",
        "}" to "RBrace",
        "[" to "LBracket",
        "]" to "RBracket",
        "@" to "At",
        "#" to "Hash",
        "$" to "Dollar",
        "->" to "Arrow",
        "=>" to "FatArrow",
        "+=" to "PlusEq",
        "-=" to "MinusEq",
        "*=" to "StarEq",
        "/=" to "SlashEq",
    )

    private fun synthesizeTokenNameFromLiteral(literal: String, existingNames: Set<String>): String {
        SYMBOL_NAME_MAP[literal]?.let { mapped ->
            if (!existingNames.contains(mapped)) return mapped
            if (!existingNames.contains("${mapped}Token")) return "${mapped}Token"
        }
        val alphaNum = buildString {
            var capitalizeNext = true
            for (ch in literal) {
                if (ch.isLetterOrDigit()) {
                    append(if (capitalizeNext) ch.uppercaseChar() else ch)
                    capitalizeNext = false
                } else {
                    SYMBOL_NAME_MAP[ch.toString()]?.let { append(it) }
                    capitalizeNext = true
                }
            }
        }
        val base = if (alphaNum.isNotEmpty()) "${alphaNum}Kw" else "LiteralToken"
        var candidate = base
        var counter = 2
        while (existingNames.contains(candidate)) {
            candidate = "$base$counter"
            counter++
        }
        return candidate
    }

    // =========================================================================
    // Lexer & Recursive-Descent Cst Parser for `.cst` files
    // =========================================================================

    private data class RawLexToken(
        val schemaId: Int,
        val startOffset: Int,
        val endOffset: Int,
        val isTrivia: Boolean,
        val errorCode: String? = null,
        val errorMessage: String? = null,
    )

    private fun tokenize(source: String): List<RawLexToken> {
        val tokens = mutableListOf<RawLexToken>()
        var i = 0
        val n = source.length

        while (i < n) {
            val start = i
            val c = source[i]

            // 1. Whitespace
            if (c.isWhitespace()) {
                while (i < n && source[i].isWhitespace()) i++
                tokens.add(RawLexToken(CstSchemaDslTokenKinds.WHITESPACE, start, i, isTrivia = true))
                continue
            }

            // 2. Comments (`///`, `//`, `/** ... */`, `/* ... */`)
            if (c == '/' && i + 1 < n) {
                if (source[i + 1] == '/') {
                    val isDoc = i + 2 < n && source[i + 2] == '/'
                    i += if (isDoc) 3 else 2
                    while (i < n && source[i] != '\n' && source[i] != '\r') i++
                    val kind = if (isDoc) CstSchemaDslTokenKinds.DOC_COMMENT else CstSchemaDslTokenKinds.LINE_COMMENT
                    tokens.add(RawLexToken(kind, start, i, isTrivia = true))
                    continue
                } else if (source[i + 1] == '*') {
                    val isDoc = i + 2 < n && source[i + 2] == '*' && (i + 3 >= n || source[i + 3] != '/')
                    i += 2
                    while (i + 1 < n && !(source[i] == '*' && source[i + 1] == '/')) {
                        i++
                    }
                    val terminated = i + 1 < n && source[i] == '*' && source[i + 1] == '/'
                    if (terminated) i += 2 else i = n
                    val kind = if (isDoc) CstSchemaDslTokenKinds.DOC_COMMENT else CstSchemaDslTokenKinds.BLOCK_COMMENT
                    tokens.add(
                        RawLexToken(
                            schemaId = kind,
                            startOffset = start,
                            endOffset = i,
                            isTrivia = true,
                            errorCode = if (!terminated) "UNTERMINATED_BLOCK_COMMENT" else null,
                            errorMessage = if (!terminated) "Unterminated block comment" else null,
                        )
                    )
                    continue
                }
            }

            // 3. String or single-quoted literal (`"..."` or `'...'`)
            if (c == '"' || c == '\'') {
                val quote = c
                i++
                while (i < n && source[i] != quote && source[i] != '\n' && source[i] != '\r') {
                    if (source[i] == '\\' && i + 1 < n) i += 2 else i++
                }
                val terminated = i < n && source[i] == quote
                if (terminated) i++
                tokens.add(
                    RawLexToken(
                        schemaId = CstSchemaDslTokenKinds.STRING_LITERAL,
                        startOffset = start,
                        endOffset = i,
                        isTrivia = false,
                        errorCode = if (!terminated) "UNTERMINATED_STRING_LITERAL" else null,
                        errorMessage = if (!terminated) "Unterminated string literal" else null,
                    )
                )
                continue
            }

            // 4. Integer literal
            if (c.isDigit() || (c == '-' && i + 1 < n && source[i + 1].isDigit())) {
                if (c == '-') i++
                while (i < n && source[i].isDigit()) i++
                tokens.add(RawLexToken(CstSchemaDslTokenKinds.INT_LITERAL, start, i, isTrivia = false))
                continue
            }

            // 5. Identifier or Keyword
            if (c.isLetter() || c == '_') {
                while (i < n && (source[i].isLetterOrDigit() || source[i] == '_')) i++
                val word = source.substring(start, i)
                val schemaId = when (word) {
                    "schema" -> CstSchemaDslTokenKinds.SCHEMA_KW
                    "package" -> CstSchemaDslTokenKinds.PACKAGE_KW
                    "trivia" -> CstSchemaDslTokenKinds.TRIVIA_KW
                    "token" -> CstSchemaDslTokenKinds.TOKEN_KW
                    "open" -> CstSchemaDslTokenKinds.OPEN_KW
                    "category" -> CstSchemaDslTokenKinds.CATEGORY_KW
                    "node" -> CstSchemaDslTokenKinds.NODE_KW
                    else -> CstSchemaDslTokenKinds.IDENT
                }
                tokens.add(RawLexToken(schemaId, start, i, isTrivia = false))
                continue
            }

            // 6. Punctuation or unrecognized character
            i++
            val punctId = when (c) {
                ':' -> CstSchemaDslTokenKinds.COLON
                '=' -> CstSchemaDslTokenKinds.EQ
                '|' -> CstSchemaDslTokenKinds.PIPE
                '?' -> CstSchemaDslTokenKinds.QUESTION
                '*' -> CstSchemaDslTokenKinds.STAR
                '+' -> CstSchemaDslTokenKinds.PLUS
                '@' -> CstSchemaDslTokenKinds.AT
                '(' -> CstSchemaDslTokenKinds.L_PAREN
                ')' -> CstSchemaDslTokenKinds.R_PAREN
                '{' -> CstSchemaDslTokenKinds.L_BRACE
                '}' -> CstSchemaDslTokenKinds.R_BRACE
                ',' -> CstSchemaDslTokenKinds.COMMA
                '.' -> CstSchemaDslTokenKinds.DOT
                else -> CstSchemaDslTokenKinds.UNKNOWN
            }
            tokens.add(
                RawLexToken(
                    schemaId = punctId,
                    startOffset = start,
                    endOffset = i,
                    isTrivia = false,
                    errorCode = if (punctId == CstSchemaDslTokenKinds.UNKNOWN) "UNEXPECTED_CHARACTER" else null,
                    errorMessage = if (punctId == CstSchemaDslTokenKinds.UNKNOWN) "Unexpected character '$c'" else null,
                )
            )
        }

        return tokens
    }

    private class DslParser(
        private val source: String,
        private val tokens: List<RawLexToken>,
        private val builder: ConcreteSyntaxBuilder,
    ) {
        private var cursor: Int = 0

        private fun peekId(lookahead: Int = 0): Int {
            var idx = cursor
            var remaining = lookahead
            while (idx < tokens.size) {
                if (!tokens[idx].isTrivia) {
                    if (remaining == 0) return tokens[idx].schemaId
                    remaining--
                }
                idx++
            }
            return CstSchemaDslTokenKinds.UNKNOWN
        }

        private fun currentOffset(): Int {
            var idx = cursor
            while (idx < tokens.size) {
                if (!tokens[idx].isTrivia) return tokens[idx].startOffset
                idx++
            }
            return source.length
        }

        private fun isAtEnd(): Boolean {
            var idx = cursor
            while (idx < tokens.size) {
                if (!tokens[idx].isTrivia) return false
                idx++
            }
            return true
        }

        private fun isIdentOrKeyword(id: Int): Boolean =
            id == CstSchemaDslTokenKinds.IDENT ||
                id == CstSchemaDslTokenKinds.SCHEMA_KW ||
                id == CstSchemaDslTokenKinds.PACKAGE_KW ||
                id == CstSchemaDslTokenKinds.TRIVIA_KW ||
                id == CstSchemaDslTokenKinds.TOKEN_KW ||
                id == CstSchemaDslTokenKinds.OPEN_KW ||
                id == CstSchemaDslTokenKinds.CATEGORY_KW ||
                id == CstSchemaDslTokenKinds.NODE_KW

        private fun flushTrivia(parentNode: Int) {
            while (cursor < tokens.size && tokens[cursor].isTrivia) {
                val t = tokens[cursor++]
                val tokIdx = builder.addTrivia(t.schemaId, t.startOffset, t.endOffset, parentNode)
                if (t.errorCode != null && t.errorMessage != null) {
                    builder.reportDiagnostic(
                        nodeIndex = tokIdx,
                        startOffset = t.startOffset,
                        endOffset = t.endOffset,
                        code = t.errorCode,
                        message = t.errorMessage,
                    )
                }
            }
        }

        private fun consumeToken(parentNode: Int, slot: Int): Int {
            flushTrivia(parentNode)
            val t = tokens[cursor++]
            val tokIdx = builder.addToken(
                schemaTokenId = t.schemaId,
                startOffset = t.startOffset,
                endOffset = t.endOffset,
                parentNode = parentNode,
                slot = slot,
                isTrivia = false,
            )
            if (t.errorCode != null && t.errorMessage != null) {
                builder.reportDiagnostic(
                    nodeIndex = tokIdx,
                    startOffset = t.startOffset,
                    endOffset = t.endOffset,
                    code = t.errorCode,
                    message = t.errorMessage,
                )
            }
            return tokIdx
        }

        private fun consumeUnexpectedToken(
            parentNode: Int,
            defaultCode: String,
            defaultMessage: (String) -> String,
        ) {
            flushTrivia(parentNode)
            val badTok = tokens[cursor++]
            val errNode = builder.startErrorNode(badTok.startOffset)
            builder.addChild(parentNode, errNode)
            if (badTok.schemaId != CstSchemaDslTokenKinds.UNKNOWN) {
                builder.addToken(badTok.schemaId, badTok.startOffset, badTok.endOffset, errNode)
            }
            builder.finishNode(errNode, badTok.endOffset)
            val text = source.substring(badTok.startOffset, badTok.endOffset)
            builder.reportDiagnostic(
                nodeIndex = errNode,
                startOffset = badTok.startOffset,
                endOffset = badTok.endOffset,
                code = badTok.errorCode ?: defaultCode,
                message = badTok.errorMessage ?: defaultMessage(text),
            )
        }

        private fun consumeIf(expectedId: Int, parentNode: Int, slot: Int): Boolean {
            if (peekId(0) == expectedId) {
                consumeToken(parentNode, slot)
                return true
            }
            return false
        }

        private fun expectToken(expectedId: Int, parentNode: Int, slot: Int, description: String): Int {
            if (peekId(0) == expectedId) {
                return consumeToken(parentNode, slot)
            }
            val offset = currentOffset()
            return builder.addMissingToken(
                schemaTokenId = expectedId,
                offset = offset,
                parentNode = parentNode,
                slot = slot,
                diagnosticCode = "EXPECTED_${CstSchemaDslTokenKinds.getKindName(expectedId).uppercase()}",
                diagnosticMessage = "Expected $description",
            )
        }

        private fun consumeIdentOrKeywordAsIdent(parentNode: Int, slot: Int, description: String): Int {
            if (isIdentOrKeyword(peekId(0))) {
                flushTrivia(parentNode)
                val t = tokens[cursor++]
                val tokIdx = builder.addToken(
                    schemaTokenId = CstSchemaDslTokenKinds.IDENT,
                    startOffset = t.startOffset,
                    endOffset = t.endOffset,
                    parentNode = parentNode,
                    slot = slot,
                    isTrivia = false,
                )
                if (t.errorCode != null && t.errorMessage != null) {
                    builder.reportDiagnostic(
                        nodeIndex = tokIdx,
                        startOffset = t.startOffset,
                        endOffset = t.endOffset,
                        code = t.errorCode,
                        message = t.errorMessage,
                    )
                }
                return tokIdx
            }
            return expectToken(CstSchemaDslTokenKinds.IDENT, parentNode, slot, description)
        }

        fun parseSchemaFile(): Int {
            val fileNode = builder.startNode(CstSchemaDslNodeKinds.SCHEMA_FILE, 0)
            flushTrivia(fileNode)

            if (peekId(0) == CstSchemaDslTokenKinds.SCHEMA_KW) {
                parseSchemaHeader(fileNode)
            }
            flushTrivia(fileNode)
            if (peekId(0) == CstSchemaDslTokenKinds.PACKAGE_KW) {
                parsePackageHeader(fileNode)
            }

            while (!isAtEnd()) {
                flushTrivia(fileNode)
                if (isAtEnd()) break
                when (peekId(0)) {
                    CstSchemaDslTokenKinds.TRIVIA_KW -> parseTokenDecl(fileNode)
                    CstSchemaDslTokenKinds.TOKEN_KW -> {
                        if (peekId(1) == CstSchemaDslTokenKinds.CATEGORY_KW) {
                            parseTokenCategoryDecl(fileNode)
                        } else {
                            parseTokenDecl(fileNode)
                        }
                    }
                    CstSchemaDslTokenKinds.OPEN_KW -> {
                        if (peekId(1) == CstSchemaDslTokenKinds.TOKEN_KW) {
                            parseTokenCategoryDecl(fileNode)
                        } else {
                            parseCategoryDecl(fileNode)
                        }
                    }
                    CstSchemaDslTokenKinds.CATEGORY_KW -> parseCategoryDecl(fileNode)
                    CstSchemaDslTokenKinds.NODE_KW -> parseNodeDecl(fileNode)
                    else -> {
                        consumeUnexpectedToken(fileNode, "UNEXPECTED_DECLARATION_TOKEN") {
                            "Unexpected token '$it' at top level"
                        }
                    }
                }
            }

            flushTrivia(fileNode)
            builder.finishNode(fileNode, source.length)
            return fileNode
        }

        private fun parseSchemaHeader(parent: Int) {
            builder.addSchemaHeader(parentNode = parent, slot = SchemaFileSyntax.SLOT_SCHEMA_HEADER) { hdr ->
                expectToken(CstSchemaDslTokenKinds.SCHEMA_KW, hdr, SchemaHeaderSyntax.SLOT_SCHEMA_KW, "'schema'")
                consumeIdentOrKeywordAsIdent(hdr, SchemaHeaderSyntax.SLOT_NAME, "schema name")
            }
        }

        private fun parsePackageHeader(parent: Int) {
            builder.addPackageHeader(parentNode = parent, slot = SchemaFileSyntax.SLOT_PACKAGE_HEADER) { hdr ->
                expectToken(CstSchemaDslTokenKinds.PACKAGE_KW, hdr, PackageHeaderSyntax.SLOT_PACKAGE_KW, "'package'")
                builder.addQualifiedName(parentNode = hdr, slot = PackageHeaderSyntax.SLOT_NAME) { qn ->
                    consumeIdentOrKeywordAsIdent(qn, QualifiedNameSyntax.SLOT_PARTS, "package segment")
                    while (peekId(0) == CstSchemaDslTokenKinds.DOT) {
                        consumeToken(qn, QualifiedNameSyntax.SLOT_DOTS)
                        consumeIdentOrKeywordAsIdent(qn, QualifiedNameSyntax.SLOT_PARTS, "package segment")
                    }
                }
            }
        }

        private fun parseTokenDecl(parent: Int) {
            builder.addTokenDecl(parentNode = parent, slot = SchemaFileSyntax.SLOT_DECLARATIONS) { td ->
                if (peekId(0) == CstSchemaDslTokenKinds.TRIVIA_KW) {
                    consumeToken(td, TokenDeclSyntax.SLOT_TRIVIA_KW)
                }
                if (peekId(0) == CstSchemaDslTokenKinds.TOKEN_KW) {
                    consumeToken(td, TokenDeclSyntax.SLOT_TOKEN_KW)
                }
                consumeIdentOrKeywordAsIdent(td, TokenDeclSyntax.SLOT_NAME, "token name")
                if (peekId(0) == CstSchemaDslTokenKinds.COLON) {
                    parseCategoryClause(td, TokenDeclSyntax.SLOT_CATEGORIES)
                }
                if (peekId(0) == CstSchemaDslTokenKinds.EQ) {
                    consumeToken(td, TokenDeclSyntax.SLOT_EQ)
                    expectToken(CstSchemaDslTokenKinds.STRING_LITERAL, td, TokenDeclSyntax.SLOT_FIXED_TEXT, "token literal string")
                }
                while (peekId(0) == CstSchemaDslTokenKinds.AT) {
                    parseAnnotation(td, TokenDeclSyntax.SLOT_ANNOTATIONS)
                }
            }
        }

        private fun parseTokenCategoryDecl(parent: Int) {
            builder.addTokenCategoryDecl(parentNode = parent, slot = SchemaFileSyntax.SLOT_DECLARATIONS) { tcd ->
                if (peekId(0) == CstSchemaDslTokenKinds.OPEN_KW) {
                    consumeToken(tcd, TokenCategoryDeclSyntax.SLOT_OPEN_KW)
                }
                expectToken(CstSchemaDslTokenKinds.TOKEN_KW, tcd, TokenCategoryDeclSyntax.SLOT_TOKEN_KW, "'token'")
                expectToken(CstSchemaDslTokenKinds.CATEGORY_KW, tcd, TokenCategoryDeclSyntax.SLOT_CATEGORY_KW, "'category'")
                consumeIdentOrKeywordAsIdent(tcd, TokenCategoryDeclSyntax.SLOT_NAME, "token category name")
                if (peekId(0) == CstSchemaDslTokenKinds.COLON) {
                    parseCategoryClause(tcd, TokenCategoryDeclSyntax.SLOT_SUPER_CATEGORIES)
                }
                if (peekId(0) == CstSchemaDslTokenKinds.EQ) {
                    consumeToken(tcd, TokenCategoryDeclSyntax.SLOT_EQ)
                    consumeIf(CstSchemaDslTokenKinds.PIPE, tcd, TokenCategoryDeclSyntax.SLOT_PIPES)
                    parseTokenMemberRef(tcd, TokenCategoryDeclSyntax.SLOT_MEMBERS)
                    while (peekId(0) == CstSchemaDslTokenKinds.PIPE) {
                        consumeToken(tcd, TokenCategoryDeclSyntax.SLOT_PIPES)
                        parseTokenMemberRef(tcd, TokenCategoryDeclSyntax.SLOT_MEMBERS)
                    }
                }
            }
        }

        private fun parseTokenMemberRef(parent: Int, slot: Int) {
            builder.addTokenMemberRef(parentNode = parent, slot = slot) { mem ->
                if (peekId(0) == CstSchemaDslTokenKinds.STRING_LITERAL) {
                    consumeToken(mem, TokenMemberRefSyntax.SLOT_LITERAL)
                } else {
                    consumeIdentOrKeywordAsIdent(mem, TokenMemberRefSyntax.SLOT_NAME, "token name or literal")
                }
            }
        }

        private fun parseCategoryDecl(parent: Int) {
            builder.addCategoryDecl(parentNode = parent, slot = SchemaFileSyntax.SLOT_DECLARATIONS) { cd ->
                if (peekId(0) == CstSchemaDslTokenKinds.OPEN_KW) {
                    consumeToken(cd, CategoryDeclSyntax.SLOT_OPEN_KW)
                }
                expectToken(CstSchemaDslTokenKinds.CATEGORY_KW, cd, CategoryDeclSyntax.SLOT_CATEGORY_KW, "'category'")
                consumeIdentOrKeywordAsIdent(cd, CategoryDeclSyntax.SLOT_NAME, "category name")
                if (peekId(0) == CstSchemaDslTokenKinds.COLON) {
                    parseCategoryClause(cd, CategoryDeclSyntax.SLOT_SUPER_CATEGORIES)
                }
                if (peekId(0) == CstSchemaDslTokenKinds.EQ) {
                    consumeToken(cd, CategoryDeclSyntax.SLOT_EQ)
                    consumeIf(CstSchemaDslTokenKinds.PIPE, cd, CategoryDeclSyntax.SLOT_PIPES)
                    consumeIdentOrKeywordAsIdent(cd, CategoryDeclSyntax.SLOT_MEMBERS, "category member name")
                    while (peekId(0) == CstSchemaDslTokenKinds.PIPE) {
                        consumeToken(cd, CategoryDeclSyntax.SLOT_PIPES)
                        consumeIdentOrKeywordAsIdent(cd, CategoryDeclSyntax.SLOT_MEMBERS, "category member name")
                    }
                }
            }
        }

        private fun parseNodeDecl(parent: Int) {
            builder.addNodeDecl(parentNode = parent, slot = SchemaFileSyntax.SLOT_DECLARATIONS) { nd ->
                expectToken(CstSchemaDslTokenKinds.NODE_KW, nd, NodeDeclSyntax.SLOT_NODE_KW, "'node'")
                consumeIdentOrKeywordAsIdent(nd, NodeDeclSyntax.SLOT_NAME, "node name")
                if (peekId(0) == CstSchemaDslTokenKinds.COLON) {
                    parseCategoryClause(nd, NodeDeclSyntax.SLOT_CATEGORIES)
                }
                while (peekId(0) == CstSchemaDslTokenKinds.AT) {
                    parseAnnotation(nd, NodeDeclSyntax.SLOT_ANNOTATIONS)
                }
                if (peekId(0) == CstSchemaDslTokenKinds.L_BRACE) {
                    consumeToken(nd, NodeDeclSyntax.SLOT_L_BRACE)
                    while (!isAtEnd() && peekId(0) != CstSchemaDslTokenKinds.R_BRACE) {
                        flushTrivia(nd)
                        if (peekId(0) == CstSchemaDslTokenKinds.R_BRACE || isAtEnd()) break
                        if (isIdentOrKeyword(peekId(0)) && peekId(1) == CstSchemaDslTokenKinds.COLON) {
                            parseFieldDecl(nd)
                        } else {
                            // Error recovery inside node body
                            consumeUnexpectedToken(nd, "INVALID_FIELD_DECL") {
                                "Expected field declaration 'name: Type'"
                            }
                        }
                    }
                    expectToken(CstSchemaDslTokenKinds.R_BRACE, nd, NodeDeclSyntax.SLOT_R_BRACE, "'}'")
                }
            }
        }

        private fun parseCategoryClause(parent: Int, slot: Int) {
            builder.addCategoryClause(parentNode = parent, slot = slot) { cc ->
                expectToken(CstSchemaDslTokenKinds.COLON, cc, CategoryClauseSyntax.SLOT_COLON, "':'")
                consumeIdentOrKeywordAsIdent(cc, CategoryClauseSyntax.SLOT_NAMES, "category name")
                while (peekId(0) == CstSchemaDslTokenKinds.COMMA) {
                    consumeToken(cc, CategoryClauseSyntax.SLOT_COMMAS)
                    consumeIdentOrKeywordAsIdent(cc, CategoryClauseSyntax.SLOT_NAMES, "category name")
                }
            }
        }

        private fun parseFieldDecl(parent: Int) {
            builder.addFieldDecl(parentNode = parent, slot = NodeDeclSyntax.SLOT_FIELDS) { fd ->
                consumeIdentOrKeywordAsIdent(fd, FieldDeclSyntax.SLOT_NAME, "field name")
                expectToken(CstSchemaDslTokenKinds.COLON, fd, FieldDeclSyntax.SLOT_COLON, "':'")
                parseTypeExpr(fd, FieldDeclSyntax.SLOT_TYPE_EXPR)
                val q = peekId(0)
                if (q == CstSchemaDslTokenKinds.QUESTION || q == CstSchemaDslTokenKinds.STAR || q == CstSchemaDslTokenKinds.PLUS) {
                    consumeToken(fd, FieldDeclSyntax.SLOT_QUANTIFIER)
                }
                while (peekId(0) == CstSchemaDslTokenKinds.AT) {
                    parseAnnotation(fd, FieldDeclSyntax.SLOT_ANNOTATIONS)
                }
                // Allow optional trailing comma between fields
                if (peekId(0) == CstSchemaDslTokenKinds.COMMA) {
                    flushTrivia(fd)
                    val commaTok = tokens[cursor++]
                    builder.addToken(commaTok.schemaId, commaTok.startOffset, commaTok.endOffset, fd)
                }
            }
        }

        private fun parseTypeExpr(parent: Int, slot: Int) {
            when (peekId(0)) {
                CstSchemaDslTokenKinds.STRING_LITERAL -> {
                    builder.addLiteralTokenTypeExpr(parentNode = parent, slot = slot) { lt ->
                        consumeToken(lt, LiteralTokenTypeExprSyntax.SLOT_LITERAL)
                    }
                }
                CstSchemaDslTokenKinds.L_PAREN -> {
                    builder.addInlineUnionTypeExpr(parentNode = parent, slot = slot) { iu ->
                        expectToken(CstSchemaDslTokenKinds.L_PAREN, iu, InlineUnionTypeExprSyntax.SLOT_L_PAREN, "'('")
                        consumeIf(CstSchemaDslTokenKinds.PIPE, iu, InlineUnionTypeExprSyntax.SLOT_PIPES)
                        parseTokenMemberRef(iu, InlineUnionTypeExprSyntax.SLOT_MEMBERS)
                        while (peekId(0) == CstSchemaDslTokenKinds.PIPE) {
                            consumeToken(iu, InlineUnionTypeExprSyntax.SLOT_PIPES)
                            parseTokenMemberRef(iu, InlineUnionTypeExprSyntax.SLOT_MEMBERS)
                        }
                        expectToken(CstSchemaDslTokenKinds.R_PAREN, iu, InlineUnionTypeExprSyntax.SLOT_R_PAREN, "')'")
                    }
                }
                else -> {
                    builder.addNamedTypeExpr(parentNode = parent, slot = slot) { nt ->
                        consumeIdentOrKeywordAsIdent(nt, NamedTypeExprSyntax.SLOT_NAME, "type name")
                    }
                }
            }
        }

        private fun parseAnnotation(parent: Int, slot: Int) {
            builder.addAnnotation(parentNode = parent, slot = slot) { ann ->
                expectToken(CstSchemaDslTokenKinds.AT, ann, AnnotationSyntax.SLOT_AT, "'@'")
                consumeIdentOrKeywordAsIdent(ann, AnnotationSyntax.SLOT_NAME, "annotation name")
                if (peekId(0) == CstSchemaDslTokenKinds.L_PAREN) {
                    consumeToken(ann, AnnotationSyntax.SLOT_L_PAREN)
                    expectToken(CstSchemaDslTokenKinds.INT_LITERAL, ann, AnnotationSyntax.SLOT_ARG, "integer argument")
                    expectToken(CstSchemaDslTokenKinds.R_PAREN, ann, AnnotationSyntax.SLOT_R_PAREN, "')'")
                }
            }
        }
    }
}
