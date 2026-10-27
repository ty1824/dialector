package dev.dialector.cst.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CstSchemaTest {

    @Test
    fun `schema model holds valid tokens, categories, and nodes`() {
        val schema = CstSchema(
            name = "MiniLang",
            packageName = "test.mini",
            tokens = listOf(
                TokenSchema("Number", id = 1),
                TokenSchema("Plus", id = 2, fixedText = "+"),
                TokenSchema("Identifier", id = 3),
            ),
            categories = listOf(
                CategorySchema("Expression", isOpen = true),
            ),
            nodes = listOf(
                NodeSchema(
                    name = "LiteralExpr",
                    id = 1,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("value", FieldType.ChildToken("Number"), slot = 0),
                    ),
                ),
                NodeSchema(
                    name = "BinaryExpr",
                    id = 2,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("left", FieldType.ChildNode("Expression"), slot = 0),
                        FieldSchema("operator", FieldType.ChildToken("Plus"), slot = 1),
                        FieldSchema("right", FieldType.ChildNode("Expression"), slot = 2),
                    ),
                ),
                NodeSchema(
                    name = "Block",
                    id = 3,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("statements", FieldType.ChildNodeList("Expression"), slot = 0),
                    ),
                ),
            ),
        ).validate()

        assertEquals("MiniLang", schema.name)
        assertEquals("test.mini", schema.packageName)
        assertEquals(3, schema.tokens.size)
        assertEquals(1, schema.categories.size)
        assertEquals(3, schema.nodes.size)

        val plusToken = schema.tokens.find { it.name == "Plus" }!!
        assertEquals("+", plusToken.fixedText)

        val binary = schema.nodes.find { it.name == "BinaryExpr" }!!
        assertEquals(3, binary.fields.size)
        assertEquals(0, binary.fields[0].slot)
        assertEquals(1, binary.fields[1].slot)
        assertEquals(2, binary.fields[2].slot)
        assertEquals(listOf("Expression"), binary.categories)
    }

    @Test
    fun `validation catches duplicate token and node names`() {
        val ex1 = assertFailsWith<CstSchemaException> {
            val s = CstSchema(
                name = "Test",
                packageName = "test",
                tokens = listOf(
                    TokenSchema("Tok", id = 1),
                    TokenSchema("Tok", id = 2),
                ),
            )
            s.validate()
        }
        assertTrue(ex1.errors.any { it.contains("Duplicate token name") })

        val ex2 = assertFailsWith<CstSchemaException> {
            val s = CstSchema(
                name = "Test",
                packageName = "test",
                nodes = listOf(
                    NodeSchema("NodeA", id = 1),
                    NodeSchema("NodeA", id = 2),
                ),
            )
            s.validate()
        }
        assertTrue(ex2.errors.any { it.contains("Duplicate node name") })
    }

    @Test
    fun `validation catches cyclic inheritance in categories`() {
        val ex = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "Test",
                packageName = "test",
                categories = listOf(
                    CategorySchema("A", superCategories = listOf("B"), isOpen = true),
                    CategorySchema("B", superCategories = listOf("A"), isOpen = true),
                ),
            ).validate()
        }
        assertTrue(ex.errors.any { it.contains("Cyclic inheritance detected in categories") })
    }

    @Test
    fun `validation catches unknown types in fields and categories`() {
        val ex = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "Test",
                packageName = "test",
                nodes = listOf(
                    NodeSchema(
                        name = "A",
                        id = 1,
                        categories = listOf("NonExistentCategory"),
                        fields = listOf(
                            FieldSchema("foo", FieldType.ChildNode("NonExistentChild"), slot = 0),
                            FieldSchema("bar", FieldType.ChildToken("NonExistentToken"), slot = 1),
                        ),
                    ),
                ),
            ).validate()
        }
        assertTrue(ex.errors.any { it.contains("references unknown category") })
        assertTrue(ex.errors.any { it.contains("references unknown node or category type") })
        assertTrue(ex.errors.any { it.contains("references unknown token type") })
    }

    @Test
    fun `schema handles trivia tokens and fixed text`() {
        val schema = CstSchema(
            name = "TriviaLang",
            packageName = "test.trivia",
            tokens = listOf(
                TokenSchema("Whitespace", id = 1, isTrivia = true),
                TokenSchema("Plus", id = 2, fixedText = "+"),
                TokenSchema("Identifier", id = 3),
            ),
            categories = listOf(
                CategorySchema("BaseCategory", isOpen = true),
            ),
            nodes = listOf(
                NodeSchema(
                    name = "DerivedNode",
                    id = 1,
                    categories = listOf("BaseCategory"),
                    fields = listOf(
                        FieldSchema("op", FieldType.ChildToken("Plus"), slot = 0),
                        FieldSchema("sub", FieldType.ChildNode("BaseCategory"), slot = 1),
                    ),
                ),
            ),
        ).validate()

        val wsToken = schema.tokens.find { it.name == "Whitespace" }!!
        assertTrue(wsToken.isTrivia)
        assertTrue(wsToken.runtimeKind < 0)

        val plusToken = schema.tokens.find { it.name == "Plus" }!!
        assertEquals("+", plusToken.fixedText)
        assertEquals(false, plusToken.isTrivia)
        assertTrue(plusToken.runtimeKind < 0)

        val idToken = schema.tokens.find { it.name == "Identifier" }!!
        assertEquals(false, idToken.isTrivia)
        assertEquals(null, idToken.fixedText)
        assertTrue(idToken.runtimeKind < 0)

        val derivedNode = schema.nodes.find { it.name == "DerivedNode" }!!
        assertEquals(2, derivedNode.fields.size)
        assertEquals(0, derivedNode.fields[0].slot)
        assertEquals(1, derivedNode.fields[1].slot)
    }

    @Test
    fun `dsl parser supports open categories for extension and closed categories for unions`() {
        val validSource = """
            schema OpenClosedLang
            package test.openclosed

            token Ident
            token Plus : CustomOp = "+"

            open token category CustomOp
            token category BinaryOp = '+' | Ident

            open category Statement
            category Expr : Statement = VarExpr | AddExpr

            node VarExpr {
                name: Ident
            }

            node AddExpr {
                left: Expr
                op: BinaryOp
                right: Expr
            }

            node ExprStmt : Statement {
                expr: Expr
            }
        """.trimIndent()

        val schema = dev.dialector.cst.schema.dsl.CstSchemaParser.parse(validSource)
        val stmtCat = schema.categories.first { it.name == "Statement" }
        val exprCat = schema.categories.first { it.name == "Expr" }
        assertTrue(stmtCat.isOpen)
        assertEquals(false, exprCat.isOpen)
        assertEquals(listOf("Statement"), exprCat.superCategories)
        assertEquals(
            setOf("VarExpr", "AddExpr", "ExprStmt"),
            schema.getCategoryNodes("Statement").map { it.name }.toSet(),
        )

        // 1. Extending a closed category via ': Expr' should fail
        val exClosedExtend = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema Bad1
                package test.bad
                token Ident
                category Expr = VarExpr
                node VarExpr : Expr {
                    name: Ident
                }
                """.trimIndent()
            )
        }
        assertTrue(exClosedExtend.errors.any { it.contains("cannot extend closed category 'Expr'") })

        // 2. Open category with '= ...' union should fail
        val exOpenWithUnion = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema Bad2
                package test.bad
                token Ident
                open category Expr = VarExpr
                node VarExpr {
                    name: Ident
                }
                """.trimIndent()
            )
        }
        assertTrue(exOpenWithUnion.errors.any { it.contains("Open category 'Expr' cannot define a '= ...' member union") })

        // 3. Closed category without '= ...' union should fail
        val exClosedWithoutUnion = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema Bad3
                package test.bad
                category EmptyClosed
                """.trimIndent()
            )
        }
        assertTrue(exClosedWithoutUnion.errors.any { it.contains("Closed category 'EmptyClosed' must define members with '= ...' or be declared 'open'") })
    }

    @Test
    fun `programmatic CstSchema validate enforces open and closed category rules`() {
        // 1. Extending a closed category via NodeSchema.categories should fail
        val exNodeExtendsClosed = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "ProgBad1",
                packageName = "test.prog",
                categories = listOf(
                    CategorySchema("Expr", isOpen = false, members = listOf("VarExpr")),
                ),
                nodes = listOf(
                    NodeSchema("VarExpr", id = 1),
                    NodeSchema("OtherExpr", id = 2, categories = listOf("Expr")),
                ),
            ).validate()
        }
        assertTrue(exNodeExtendsClosed.errors.any { it.contains("Node 'OtherExpr' cannot extend closed category 'Expr'") })

        // 2. Extending a closed category via CategorySchema.superCategories should fail
        val exCatExtendsClosed = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "ProgBad2",
                packageName = "test.prog",
                categories = listOf(
                    CategorySchema("Base", isOpen = false, members = listOf("NodeA")),
                    CategorySchema("Sub", isOpen = true, superCategories = listOf("Base")),
                ),
                nodes = listOf(
                    NodeSchema("NodeA", id = 1),
                ),
            ).validate()
        }
        assertTrue(exCatExtendsClosed.errors.any { it.contains("Category 'Sub' cannot extend closed category 'Base'") })

        // 3. Open category defining members should fail
        val exOpenWithMembers = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "ProgBad3",
                packageName = "test.prog",
                categories = listOf(
                    CategorySchema("Expr", isOpen = true, members = listOf("NodeA")),
                ),
                nodes = listOf(
                    NodeSchema("NodeA", id = 1),
                ),
            ).validate()
        }
        assertTrue(exOpenWithMembers.errors.any { it.contains("Open category 'Expr' cannot define a '= ...' member union") })

        // 4. Closed category with empty members should fail
        val exClosedEmpty = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "ProgBad4",
                packageName = "test.prog",
                categories = listOf(
                    CategorySchema("EmptyClosed", isOpen = false, members = emptyList()),
                ),
            ).validate()
        }
        assertTrue(exClosedEmpty.errors.any { it.contains("Closed category 'EmptyClosed' must define members with '= ...' or be declared 'open'") })

        // 5. Extending a closed token category should fail
        val exTokenExtendsClosed = assertFailsWith<CstSchemaException> {
            CstSchema(
                name = "ProgBad5",
                packageName = "test.prog",
                tokens = listOf(
                    TokenSchema("Plus", id = 1, fixedText = "+"),
                    TokenSchema("Minus", id = 2, fixedText = "-", categories = listOf("BinOp")),
                ),
                tokenCategories = listOf(
                    TokenCategorySchema("BinOp", isOpen = false, members = listOf("Plus")),
                ),
            ).validate()
        }
        assertTrue(exTokenExtendsClosed.errors.any { it.contains("Token 'Minus' cannot extend closed token category 'BinOp'") })
    }

    @Test
    fun `dsl parser preserves star vs plus list quantifier in isNonEmpty`() {
        val schema = dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
            """
            schema QuantLang
            package test.quant

            token Ident
            node Item {
                name: Ident
            }
            node Container {
                zeroOrMoreNodes: Item*
                oneOrMoreNodes: Item+
                zeroOrMoreTokens: Ident*
                oneOrMoreTokens: Ident+
            }
            """.trimIndent()
        )

        val container = schema.nodes.first { it.name == "Container" }
        val fieldsByName = container.fields.associateBy { it.name }

        assertEquals(FieldType.ChildNodeList("Item", isNonEmpty = false), fieldsByName.getValue("zeroOrMoreNodes").type)
        assertEquals(FieldType.ChildNodeList("Item", isNonEmpty = true), fieldsByName.getValue("oneOrMoreNodes").type)
        assertEquals(FieldType.ChildTokenList("Ident", isNonEmpty = false), fieldsByName.getValue("zeroOrMoreTokens").type)
        assertEquals(FieldType.ChildTokenList("Ident", isNonEmpty = true), fieldsByName.getValue("oneOrMoreTokens").type)
    }

    @Test
    fun `dsl parser collects multi-line triple-slash doc comments and stops at blank lines`() {
        val schema = dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
            """
            schema DocLang
            package test.doc

            /// Detached comment separated by a blank line

            /// First line of token doc.
            /// Second line of token doc.
            token Ident

            /// First line of node doc.
            /// Second line of node doc.
            /// Third line of node doc.
            node Greeting {
                /// Field line 1.
                /// Field line 2.
                name: Ident
            }
            """.trimIndent()
        )

        val identToken = schema.tokens.first { it.name == "Ident" }
        assertEquals("First line of token doc.\nSecond line of token doc.", identToken.doc)

        val greetingNode = schema.nodes.first { it.name == "Greeting" }
        assertEquals("First line of node doc.\nSecond line of node doc.\nThird line of node doc.", greetingNode.doc)
        assertEquals("Field line 1.\nField line 2.", greetingNode.fields.first().doc)
    }

    @Test
    fun `dsl parser reports diagnostics for unrecognized characters, unterminated strings, and unterminated block comments`() {
        // 1. Unrecognized character (`~` or `;`)
        val exBadChar = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema BadChar
                package test.bad
                token ~Ident
                """.trimIndent()
            )
        }
        assertTrue(exBadChar.errors.any { it.contains("UNEXPECTED_CHARACTER") && it.contains("~") })

        // 2. Unterminated string literal
        val exUnterminatedStr = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema BadStr
                package test.bad
                token Plus = "+
                """.trimIndent()
            )
        }
        assertTrue(exUnterminatedStr.errors.any { it.contains("UNTERMINATED_STRING_LITERAL") })

        // 3. Unterminated block comment
        val exUnterminatedComment = assertFailsWith<CstSchemaException> {
            dev.dialector.cst.schema.dsl.CstSchemaParser.parse(
                """
                schema BadComment
                package test.bad
                /* unterminated comment
                token Ident
                """.trimIndent()
            )
        }
        assertTrue(exUnterminatedComment.errors.any { it.contains("UNTERMINATED_BLOCK_COMMENT") })
    }
}
