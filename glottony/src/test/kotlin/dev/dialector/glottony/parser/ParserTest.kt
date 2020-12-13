package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.File
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.StringLiteral
import dev.dialector.glottony.ast.StringType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun basicParserTest() {
        val ast: File = GlottonyParser.parseString("""fun foo(): string = "hello"""")

        assertEquals(1, ast.contents.size)
        assert(ast.contents[0] is FunctionDeclaration)
        val decl = ast.contents[0] as FunctionDeclaration
        assertEquals("foo", decl.name )
        assertTrue(decl.type is StringType, "Expected string but was ${decl.type}")
        assertTrue(decl.body is StringLiteral, "Expected string literal but was ${decl.body}")
    }
}