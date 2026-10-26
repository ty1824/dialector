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
                CategorySchema("Expression"),
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
                    CategorySchema("A", superCategories = listOf("B")),
                    CategorySchema("B", superCategories = listOf("A")),
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
                CategorySchema("BaseCategory"),
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
}
