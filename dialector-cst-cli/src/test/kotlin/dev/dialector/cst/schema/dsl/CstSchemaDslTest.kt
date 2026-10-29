package dev.dialector.cst.schema.dsl

import dev.dialector.cst.Document
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslCategories
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslNodeKinds
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslSchema
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslTokenCategories
import dev.dialector.cst.schema.dsl.gen.CstSchemaDslTokenKinds
import dev.dialector.cst.schema.dsl.gen.DeclarationSyntax
import dev.dialector.cst.schema.dsl.gen.FieldDeclSyntax
import dev.dialector.cst.schema.dsl.gen.IdentToken
import dev.dialector.cst.schema.dsl.gen.NamedTypeExprSyntax
import dev.dialector.cst.schema.dsl.gen.NodeDeclSyntax
import dev.dialector.cst.schema.dsl.gen.PackageHeaderSyntax
import dev.dialector.cst.schema.dsl.gen.QualifiedNameSyntax
import dev.dialector.cst.schema.dsl.gen.SchemaFileSyntax
import dev.dialector.cst.schema.dsl.gen.SchemaHeaderSyntax
import dev.dialector.cst.schema.dsl.gen.TypeExprSyntax
import dev.dialector.cst.schema.dsl.gen.addColonToken
import dev.dialector.cst.schema.dsl.gen.addDotToken
import dev.dialector.cst.schema.dsl.gen.addFieldDecl
import dev.dialector.cst.schema.dsl.gen.addIdentToken
import dev.dialector.cst.schema.dsl.gen.addLBraceToken
import dev.dialector.cst.schema.dsl.gen.addMissingSchemaHeader
import dev.dialector.cst.schema.dsl.gen.addNamedTypeExpr
import dev.dialector.cst.schema.dsl.gen.addNodeDecl
import dev.dialector.cst.schema.dsl.gen.addNodeKwToken
import dev.dialector.cst.schema.dsl.gen.addPackageHeader
import dev.dialector.cst.schema.dsl.gen.addPackageKwToken
import dev.dialector.cst.schema.dsl.gen.addQualifiedName
import dev.dialector.cst.schema.dsl.gen.addRBraceToken
import dev.dialector.cst.schema.dsl.gen.addSchemaFile
import dev.dialector.cst.schema.dsl.gen.addSchemaHeader
import dev.dialector.cst.schema.dsl.gen.addSchemaKwToken
import dev.dialector.cst.schema.dsl.gen.addUnavailableDeclaration
import dev.dialector.cst.schema.dsl.gen.buildCstSchemaDslCst
import dev.dialector.cst.schema.dsl.gen.rootCstSchemaDslNode
import dev.dialector.cst.schema.dsl.gen.toCstSchemaDslDebugString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CstSchemaDslTest {

    @Test
    fun `node kinds provide correct kind names for known and unknown kinds`() {
        assertEquals("ErrorNode", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.ERROR))
        assertEquals("SchemaFile", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.SCHEMA_FILE))
        assertEquals("SchemaHeader", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.SCHEMA_HEADER))
        assertEquals("PackageHeader", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.PACKAGE_HEADER))
        assertEquals("QualifiedName", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.QUALIFIED_NAME))
        assertEquals("TokenDecl", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.TOKEN_DECL))
        assertEquals("TokenCategoryDecl", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL))
        assertEquals("TokenMemberRef", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.TOKEN_MEMBER_REF))
        assertEquals("CategoryDecl", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.CATEGORY_DECL))
        assertEquals("NodeDecl", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.NODE_DECL))
        assertEquals("CategoryClause", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.CATEGORY_CLAUSE))
        assertEquals("FieldDecl", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.FIELD_DECL))
        assertEquals("NamedTypeExpr", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.NAMED_TYPE_EXPR))
        assertEquals("LiteralTokenTypeExpr", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR))
        assertEquals("InlineUnionTypeExpr", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR))
        assertEquals("Annotation", CstSchemaDslNodeKinds.getKindName(CstSchemaDslNodeKinds.ANNOTATION))
        assertEquals("UnknownNodeKind(999)", CstSchemaDslNodeKinds.getKindName(999))
    }

    @Test
    fun `token kinds provide correct names, fixed text, and recognition for schema IDs and runtime kinds`() {
        // Schema IDs (positive)
        assertEquals("Whitespace", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.WHITESPACE))
        assertEquals("Ident", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.IDENT))
        assertEquals("StringLiteral", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.STRING_LITERAL))
        assertEquals("SchemaKw", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.SCHEMA_KW))
        assertEquals("NodeKw", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.NODE_KW))
        assertEquals("Colon", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.COLON))
        assertEquals("Eq", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.EQ))
        assertEquals("Pipe", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.PIPE))
        assertEquals("LBrace", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.L_BRACE))
        assertEquals("RBrace", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.R_BRACE))

        // Runtime kinds (negative)
        assertEquals("Whitespace", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.KIND_WHITESPACE))
        assertEquals("Ident", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.KIND_IDENT))
        assertEquals("SchemaKw", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.KIND_SCHEMA_KW))
        assertEquals("Colon", CstSchemaDslTokenKinds.getKindName(CstSchemaDslTokenKinds.KIND_COLON))

        // Unknown
        assertEquals("UnknownTokenKind(999)", CstSchemaDslTokenKinds.getKindName(999))

        // Fixed token text
        assertEquals("schema", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.SCHEMA_KW))
        assertEquals("package", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.PACKAGE_KW))
        assertEquals("trivia", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.TRIVIA_KW))
        assertEquals("token", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.TOKEN_KW))
        assertEquals("open", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.OPEN_KW))
        assertEquals("category", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.CATEGORY_KW))
        assertEquals("node", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.NODE_KW))
        assertEquals(":", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.COLON))
        assertEquals("=", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.EQ))
        assertEquals("|", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.PIPE))
        assertEquals("?", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.QUESTION))
        assertEquals("*", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.STAR))
        assertEquals("+", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.PLUS))
        assertEquals("@", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.AT))
        assertEquals("(", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.L_PAREN))
        assertEquals(")", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.R_PAREN))
        assertEquals("{", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.L_BRACE))
        assertEquals("}", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.R_BRACE))
        assertEquals(",", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.COMMA))
        assertEquals(".", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.DOT))

        // Runtime kinds of fixed tokens
        assertEquals("schema", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.KIND_SCHEMA_KW))
        assertEquals(":", CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.KIND_COLON))

        // Non-fixed tokens return null
        assertNull(CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.IDENT))
        assertNull(CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.STRING_LITERAL))
        assertNull(CstSchemaDslTokenKinds.getTokenText(CstSchemaDslTokenKinds.INT_LITERAL))

        // Known token checks
        assertTrue(CstSchemaDslTokenKinds.isKnownToken(CstSchemaDslTokenKinds.IDENT))
        assertTrue(CstSchemaDslTokenKinds.isKnownToken(CstSchemaDslTokenKinds.KIND_IDENT))
        assertTrue(CstSchemaDslTokenKinds.isKnownToken(CstSchemaDslTokenKinds.SCHEMA_KW))
        assertFalse(CstSchemaDslTokenKinds.isKnownToken(999))
        assertFalse(CstSchemaDslTokenKinds.isKnownToken(0))
    }

    @Test
    fun `category and token category membership sets and functions match definitions`() {
        // Node categories
        assertTrue(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.TOKEN_DECL))
        assertTrue(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.TOKEN_CATEGORY_DECL))
        assertTrue(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.CATEGORY_DECL))
        assertTrue(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.NODE_DECL))
        assertFalse(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.FIELD_DECL))
        assertFalse(CstSchemaDslCategories.isDeclaration(CstSchemaDslNodeKinds.SCHEMA_FILE))

        assertTrue(CstSchemaDslCategories.isTypeExpr(CstSchemaDslNodeKinds.NAMED_TYPE_EXPR))
        assertTrue(CstSchemaDslCategories.isTypeExpr(CstSchemaDslNodeKinds.LITERAL_TOKEN_TYPE_EXPR))
        assertTrue(CstSchemaDslCategories.isTypeExpr(CstSchemaDslNodeKinds.INLINE_UNION_TYPE_EXPR))
        assertFalse(CstSchemaDslCategories.isTypeExpr(CstSchemaDslNodeKinds.NODE_DECL))

        // Token categories
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.QUESTION))
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.STAR))
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.PLUS))
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.KIND_QUESTION))
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.KIND_STAR))
        assertTrue(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.KIND_PLUS))
        assertFalse(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.COLON))
        assertFalse(CstSchemaDslTokenCategories.isQuantifierOp(CstSchemaDslTokenKinds.IDENT))
    }

    @Test
    fun `schema descriptor holds complete metadata for CstSchemaDsl`() {
        val schema = CstSchemaDslSchema
        assertEquals("CstSchemaDsl", schema.name)
        assertEquals(15, schema.nodes.size)
        assertEquals(27, schema.tokens.size)
        assertEquals(2, schema.categories.size)
        assertEquals(1, schema.tokenCategories.size)

        assertNotNull(schema.getNodeDescriptor(CstSchemaDslNodeKinds.SCHEMA_FILE))
        assertNotNull(schema.getNodeDescriptor(CstSchemaDslNodeKinds.NODE_DECL))
        assertNotNull(schema.getTokenDescriptor(CstSchemaDslTokenKinds.SCHEMA_KW))
        assertNotNull(schema.getCategoryDescriptor("Declaration"))
        assertNotNull(schema.getTokenCategoryDescriptor("QuantifierOp"))
    }

    @Test
    fun `syntax handles support sentinel states and string representations`() {
        val declNone = DeclarationSyntax.NONE
        assertTrue(declNone.isNone)
        assertFalse(declNone.isValid)
        assertEquals(-1, declNone.nodeIndex)
        assertEquals("DeclarationSyntax.NONE", declNone.toString())

        val typeNone = TypeExprSyntax.NONE
        assertTrue(typeNone.isNone)
        assertFalse(typeNone.isValid)
        assertEquals("TypeExprSyntax.NONE", typeNone.toString())

        val fileNone = SchemaFileSyntax.NONE
        assertTrue(fileNone.isNone)
        assertFalse(fileNone.isValid)
        assertEquals("SchemaFileSyntax.NONE", fileNone.toString())

        val nodeNone = NodeDeclSyntax.NONE
        assertTrue(nodeNone.isNone)
        assertFalse(nodeNone.isValid)
        assertEquals("NodeDeclSyntax.NONE", nodeNone.toString())

        val identNone = IdentToken.NONE
        assertTrue(identNone.isNone)
        assertFalse(identNone.isValid)
        assertEquals("IdentToken.NONE", identNone.toString())
    }

    @Test
    fun `buildCstSchemaDslCst constructs well-formed CST with typed navigation`() {
        val source = "schema MiniLang package test.mini node MyNode { item: String }"
        val doc = Document.of(source)

        val hKwStart = source.indexOf("schema")
        val hKwEnd = hKwStart + "schema".length
        val hNameStart = source.indexOf("MiniLang")
        val hNameEnd = hNameStart + "MiniLang".length

        val pKwStart = source.indexOf("package")
        val pKwEnd = pKwStart + "package".length
        val q1Start = source.indexOf("test")
        val q1End = q1Start + "test".length
        val dotStart = source.indexOf(".")
        val dotEnd = dotStart + 1
        val q2Start = source.indexOf("mini")
        val q2End = q2Start + "mini".length

        val nKwStart = source.indexOf("node")
        val nKwEnd = nKwStart + "node".length
        val nNameStart = source.indexOf("MyNode")
        val nNameEnd = nNameStart + "MyNode".length
        val lBrStart = source.indexOf("{")
        val lBrEnd = lBrStart + 1
        val fNameStart = source.indexOf("item")
        val fNameEnd = fNameStart + "item".length
        val colStart = source.indexOf(":")
        val colEnd = colStart + 1
        val tNameStart = source.indexOf("String")
        val tNameEnd = tNameStart + "String".length
        val rBrStart = source.indexOf("}")
        val rBrEnd = rBrStart + 1

        val tree = buildCstSchemaDslCst(doc) {
            addSchemaFile(0, source.length) { fileIdx ->
                addSchemaHeader(
                    hKwStart,
                    hNameEnd,
                    parentNode = fileIdx,
                    slot = SchemaFileSyntax.SLOT_SCHEMA_HEADER
                ) { hIdx ->
                    addSchemaKwToken(hKwStart, hKwEnd, parentNode = hIdx, slot = SchemaHeaderSyntax.SLOT_SCHEMA_KW)
                    addIdentToken(hNameStart, hNameEnd, parentNode = hIdx, slot = SchemaHeaderSyntax.SLOT_NAME)
                }
                addPackageHeader(
                    pKwStart,
                    q2End,
                    parentNode = fileIdx,
                    slot = SchemaFileSyntax.SLOT_PACKAGE_HEADER
                ) { pIdx ->
                    addPackageKwToken(pKwStart, pKwEnd, parentNode = pIdx, slot = PackageHeaderSyntax.SLOT_PACKAGE_KW)
                    addQualifiedName(q1Start, q2End, parentNode = pIdx, slot = PackageHeaderSyntax.SLOT_NAME) { qIdx ->
                        addIdentToken(q1Start, q1End, parentNode = qIdx, slot = QualifiedNameSyntax.SLOT_PARTS)
                        addDotToken(dotStart, dotEnd, parentNode = qIdx, slot = QualifiedNameSyntax.SLOT_DOTS)
                        addIdentToken(q2Start, q2End, parentNode = qIdx, slot = QualifiedNameSyntax.SLOT_PARTS)
                    }
                }
                addNodeDecl(nKwStart, rBrEnd, parentNode = fileIdx, slot = SchemaFileSyntax.SLOT_DECLARATIONS) { nIdx ->
                    addNodeKwToken(nKwStart, nKwEnd, parentNode = nIdx, slot = NodeDeclSyntax.SLOT_NODE_KW)
                    addIdentToken(nNameStart, nNameEnd, parentNode = nIdx, slot = NodeDeclSyntax.SLOT_NAME)
                    addLBraceToken(lBrStart, lBrEnd, parentNode = nIdx, slot = NodeDeclSyntax.SLOT_L_BRACE)
                    addFieldDecl(fNameStart, tNameEnd, parentNode = nIdx, slot = NodeDeclSyntax.SLOT_FIELDS) { fIdx ->
                        addIdentToken(fNameStart, fNameEnd, parentNode = fIdx, slot = FieldDeclSyntax.SLOT_NAME)
                        addColonToken(colStart, colEnd, parentNode = fIdx, slot = FieldDeclSyntax.SLOT_COLON)
                        addNamedTypeExpr(
                            tNameStart,
                            tNameEnd,
                            parentNode = fIdx,
                            slot = FieldDeclSyntax.SLOT_TYPE_EXPR
                        ) { tIdx ->
                            addIdentToken(tNameStart, tNameEnd, parentNode = tIdx, slot = NamedTypeExprSyntax.SLOT_NAME)
                        }
                    }
                    addRBraceToken(rBrStart, rBrEnd, parentNode = nIdx, slot = NodeDeclSyntax.SLOT_R_BRACE)
                }
            }
        }

        assertEquals(CstSchemaDslSchema, tree.schema)
        val rootNode = tree.rootCstSchemaDslNode
        assertTrue(rootNode.isValid)

        with(tree) {
            assertEquals(CstSchemaDslNodeKinds.SCHEMA_FILE, rootNode.kind)
            val schemaFile = SchemaFileSyntax(rootNode.nodeIndex)
            assertTrue(schemaFile.isValid)

            // Header
            val header = schemaFile.schemaHeader
            assertTrue(header.isValid)
            assertEquals("schema", header.schemaKw.asToken.text)
            assertEquals("MiniLang", header.name.asToken.text)

            // Package
            val pkg = schemaFile.packageHeader
            assertTrue(pkg.isValid)
            assertEquals("package", pkg.packageKw.asToken.text)
            val qName = pkg.name
            assertTrue(qName.isValid)
            assertEquals(2, qName.parts.size)
            assertEquals("test", qName.parts[0].asToken.text)
            assertEquals("mini", qName.parts[1].asToken.text)

            // Declarations
            val decls = schemaFile.declarations
            assertEquals(1, decls.size)
            val decl = decls[0]
            assertTrue(decl.isValid)
            assertTrue(decl.isNodeDecl)
            assertFalse(decl.isTokenDecl)
            assertFalse(decl.isCategoryDecl)
            assertFalse(decl.isTokenCategoryDecl)

            val nodeDecl = decl.asNodeDecl
            assertTrue(nodeDecl.isValid)
            assertEquals("node", nodeDecl.nodeKw.asToken.text)
            assertEquals("MyNode", nodeDecl.name.asToken.text)

            val fields = nodeDecl.fields
            assertEquals(1, fields.size)
            val field = fields[0]
            assertTrue(field.isValid)
            assertEquals("item", field.name.asToken.text)
            assertEquals(":", field.colon.asToken.text)

            val typeExpr = field.typeExpr
            assertTrue(typeExpr.isValid)
            assertTrue(typeExpr.isNamedTypeExpr)
            assertFalse(typeExpr.isLiteralTokenTypeExpr)
            assertFalse(typeExpr.isInlineUnionTypeExpr)
            assertEquals("String", typeExpr.asNamedTypeExpr.name.asToken.text)
        }

        val debugString = tree.toCstSchemaDslDebugString()
        assertTrue(debugString.contains("SchemaFile"))
        assertTrue(debugString.contains("SchemaHeader"))
        assertTrue(debugString.contains("PackageHeader"))
        assertTrue(debugString.contains("NodeDecl"))
        assertTrue(debugString.contains("FieldDecl"))
        assertTrue(debugString.contains("NamedTypeExpr"))
    }

    @Test
    fun `builders support missing and unavailable node recovery`() {
        val tree = buildCstSchemaDslCst {
            addSchemaFile(0, 0) { fileIdx ->
                addMissingSchemaHeader(
                    offset = 0,
                    parentNode = fileIdx,
                    slot = SchemaFileSyntax.SLOT_SCHEMA_HEADER,
                    diagnosticCode = "MISSING_HEADER",
                    diagnosticMessage = "Expected schema header",
                )
                addUnavailableDeclaration(
                    offset = 0,
                    parentNode = fileIdx,
                    slot = SchemaFileSyntax.SLOT_DECLARATIONS,
                    diagnosticCode = "UNAVAILABLE_DECL",
                    diagnosticMessage = "Could not parse declaration",
                )
            }
        }

        assertTrue(tree.hasErrors)
        assertEquals(2, tree.diagnostics.size)

        with(tree) {
            val file = SchemaFileSyntax(tree.root.nodeIndex)
            val header = file.schemaHeader
            assertTrue(header.isValid)
            assertTrue(header.asNode.isMissing)

            val decl = file.declarations[0]
            assertTrue(decl.isValid)
            assertTrue(decl.asNode.isError)
        }
    }

    @Test
    fun `CstSchemaParser parseToCst produces lossless CST and navigates rich structures`() {
        val source = """
            schema FullTest
            package dev.dialector.test

            // Line comment
            trivia Whitespace
            token Ident
            token Plus = "+"

            open token category Op = '+'
            open category Expr

            /// Documentation for LiteralExpr node
            node LiteralExpr : Expr {
                val: Ident @slot(0)
            }

            node BinaryExpr : Expr {
                left: Expr
                op: Op
                right: Expr
            }
        """.trimIndent()

        val tree = CstSchemaParser.parseToCst(source)
        assertEquals(source, tree.document.text)
        assertFalse(tree.hasErrors)

        with(tree) {
            val root = SchemaFileSyntax(tree.root.nodeIndex)
            assertTrue(root.isValid)

            // Header verification
            assertEquals("FullTest", root.schemaHeader.name.asToken.text)
            val pkgParts = mutableListOf<String>()
            root.packageHeader.name.parts.forEach { pkgParts.add(it.asToken.text) }
            assertEquals(listOf("dev", "dialector", "test"), pkgParts)
            assertEquals("dev.dialector.test", root.packageHeader.name.asNode.text.trim())

            // Declarations verification
            val decls = root.declarations
            assertEquals(7, decls.size) // Whitespace, Ident, Plus, Op, Expr, LiteralExpr, BinaryExpr

            // Check trivia token decl
            val wsDecl = decls[0].asTokenDeclOrNone
            assertTrue(wsDecl.isValid)
            assertTrue(wsDecl.triviaKw.isValid)
            assertEquals("Whitespace", wsDecl.name.asToken.text)

            // Check token decl with fixed text
            val plusDecl = decls[2].asTokenDeclOrNone
            assertTrue(plusDecl.isValid)
            assertEquals("Plus", plusDecl.name.asToken.text)
            assertEquals("\"+\"", plusDecl.fixedText.asToken.text)

            // Check token category decl
            val opDecl = decls[3].asTokenCategoryDeclOrNone
            assertTrue(opDecl.isValid)
            assertTrue(opDecl.openKw.isValid)
            assertEquals("Op", opDecl.name.asToken.text)
            assertEquals(1, opDecl.members.size)

            // Check category decl
            val exprCatDecl = decls[4].asCategoryDeclOrNone
            assertTrue(exprCatDecl.isValid)
            assertTrue(exprCatDecl.openKw.isValid)
            assertEquals("Expr", exprCatDecl.name.asToken.text)

            // Check LiteralExpr node decl with category clause and annotation
            val litNode = decls[5].asNodeDeclOrNone
            assertTrue(litNode.isValid)
            assertEquals("LiteralExpr", litNode.name.asToken.text)
            assertTrue(litNode.categories.isValid)
            assertEquals("Expr", litNode.categories.names[0].asToken.text)

            val litField = litNode.fields[0]
            assertTrue(litField.isValid)
            assertEquals("val", litField.name.asToken.text)
            assertEquals(1, litField.annotations.size)
            val ann = litField.annotations[0]
            assertEquals("slot", ann.name.asToken.text)
            assertEquals("0", ann.arg.asToken.text)

            // Check BinaryExpr node decl
            val binNode = decls[6].asNodeDeclOrNone
            assertTrue(binNode.isValid)
            assertEquals("BinaryExpr", binNode.name.asToken.text)
            assertEquals(3, binNode.fields.size)
            assertEquals("left", binNode.fields[0].name.asToken.text)
            assertEquals("op", binNode.fields[1].name.asToken.text)
            assertEquals("right", binNode.fields[2].name.asToken.text)
        }
    }

    @Test
    fun `CstSchemaParser parseToCst parses inline union type expressions`() {
        val source = """
            schema InlineUnion
            package test.union

            token A = "a"
            token B = "b"

            node Choice {
                pick: ('a' | 'b')
            }
        """.trimIndent()

        val tree = CstSchemaParser.parseToCst(source)
        assertFalse(tree.hasErrors)

        with(tree) {
            val root = SchemaFileSyntax(tree.root.nodeIndex)
            val choiceNode = root.declarations[2].asNodeDecl
            val field = choiceNode.fields[0]
            val typeExpr = field.typeExpr

            assertTrue(typeExpr.isInlineUnionTypeExpr)
            val unionExpr = typeExpr.asInlineUnionTypeExpr
            assertEquals(2, unionExpr.members.size)
            assertEquals("'a'", unionExpr.members[0].literal.asToken.text)
            assertEquals("'b'", unionExpr.members[1].literal.asToken.text)
        }
    }

    @Test
    fun `CstSchemaParser parseToCst records diagnostics on syntax errors`() {
        val badSource = """
            schema Broken
            package test.broken

            node {
                : NonExistent
            }
        """.trimIndent()

        val tree = CstSchemaParser.parseToCst(badSource)
        assertTrue(tree.hasErrors)
        assertTrue(tree.diagnostics.isNotEmpty())
    }

    @Test
    fun `CstSchemaParser selfHostedSchema parses cleanly and reflects CstSchemaDsl specification`() {
        val schema = CstSchemaParser.selfHostedSchema()
        assertEquals("CstSchemaDsl", schema.name)
        assertEquals("dev.dialector.cst.schema.dsl.gen", schema.packageName)

        // Verifies the schema is valid according to schema validation rules
        schema.validate()

        // Verify key tokens exist
        assertTrue(schema.tokens.any { it.name == "SchemaKw" && it.fixedText == "schema" })
        assertTrue(schema.tokens.any { it.name == "NodeKw" && it.fixedText == "node" })
        assertTrue(schema.tokens.any { it.name == "Ident" })

        // Verify categories exist
        assertTrue(schema.categories.any { it.name == "Declaration" && it.isOpen })
        assertTrue(schema.categories.any { it.name == "TypeExpr" && !it.isOpen })

        // Verify token categories exist
        assertTrue(schema.tokenCategories.any { it.name == "QuantifierOp" })

        // Verify nodes exist
        assertTrue(schema.nodes.any { it.name == "SchemaFile" })
        assertTrue(schema.nodes.any { it.name == "NodeDecl" })
        assertTrue(schema.nodes.any { it.name == "FieldDecl" })
        assertTrue(schema.nodes.any { it.name == "Annotation" })
    }
}
