package dev.dialector.glottony.parser

import dev.dialector.glottony.ast.File
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.StringType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun basicParserTest() {
        val ast: File = GlottonyParser.parseFile("""fun foo(): string = "hello"""")

        assertEquals(1, ast.contents.size)
        assert(ast.contents[0] is FunctionDeclaration)
        val decl = ast.contents[0] as FunctionDeclaration
        assertEquals("foo", decl.name )
        assertTrue(decl.type is StringType, "Expected string but was ${decl.type}")
    }
}