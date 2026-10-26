package dev.dialector.cst.cli

import com.github.ajalt.clikt.testing.test
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DialectorCstCliTest {
    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("dialector-cst-cli-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun writeCstFile(relativePath: String, content: String): File {
        val file = File(tempDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content.trimIndent())
        return file
    }

    @Test
    fun `generate command produces Kinds, Handles, and Extensions files from single cst file`() {
        val cstFile = writeCstFile(
            "Calc.cst",
            """
            schema Calculator
            package dev.dialector.calc

            trivia Whitespace
            token Number
            token Plus = "+"
            token Minus = "-"

            token category BinaryOp = '+' | '-'

            category Expr = NumberExpr | BinaryExpr

            node NumberExpr {
                value: Number
            }

            node BinaryExpr {
                left: Expr
                op: BinaryOp
                right: Expr
            }
            """,
        )

        val outDir = File(tempDir, "out")
        val cli = DialectorCstCli()
        val result = cli.test(listOf("generate", "-o", outDir.absolutePath, cstFile.absolutePath))

        assertEquals(0, result.statusCode, "CLI output:\n${result.output}")
        assertTrue(result.output.contains("Generated 3 file(s) for schema 'Calculator'"))

        val pkgDir = File(outDir, "dev/dialector/calc")
        assertTrue(File(pkgDir, "CalculatorKinds.kt").isFile)
        assertTrue(File(pkgDir, "CalculatorHandles.kt").isFile)
        assertTrue(File(pkgDir, "CalculatorExtensions.kt").isFile)

        val handlesContent = File(pkgDir, "CalculatorHandles.kt").readText()
        assertTrue(handlesContent.contains("value class BinaryExprHandle"))
        assertTrue(handlesContent.contains("value class BinaryOpTokenHandle"))
    }

    @Test
    fun `generate command discovers cst files recursively in directory and respects overrides and flags`() {
        val schemasDir = File(tempDir, "schemas")
        writeCstFile(
            "schemas/sub/Mini.cst",
            """
            token Ident
            node Root {
                name: Ident
            }
            """,
        )

        val outDir = File(tempDir, "out-custom")
        val staleFile = File(outDir, "stale.txt").apply {
            parentFile.mkdirs()
            writeText("stale")
        }

        val cli = DialectorCstCli()
        val result = cli.test(
            listOf(
                "generate",
                "--output", outDir.absolutePath,
                "--package", "com.example.custom",
                "--schema-name", "CustomLang",
                "--token-handles=false",
                "--category-handles=false",
                "--debug-helpers=false",
                "--builder-extensions=false",
                "--navigators=false",
                "--clean",
                schemasDir.absolutePath,
            )
        )

        assertEquals(0, result.statusCode, "CLI output:\n${result.output}")
        assertFalse(staleFile.exists(), "--clean should have removed stale files")

        val pkgDir = File(outDir, "com/example/custom")
        assertTrue(File(pkgDir, "CustomLangKinds.kt").isFile)
        assertTrue(File(pkgDir, "CustomLangHandles.kt").isFile)
        // With navigators, builder-extensions, and debug-helpers all false, Extensions file is omitted
        assertFalse(File(pkgDir, "CustomLangExtensions.kt").exists())

        val handlesContent = File(pkgDir, "CustomLangHandles.kt").readText()
        assertTrue(handlesContent.contains("value class RootHandle"))
        assertFalse(handlesContent.contains("IdentTokenHandle"), "Token handles were disabled")
    }

    @Test
    fun `validate command succeeds on valid schema and fails on invalid schema`() {
        val validFile = writeCstFile(
            "Valid.cst",
            """
            schema ValidLang
            package dev.dialector.valid

            token Ident
            node Greeting {
                name: Ident
            }
            """,
        )

        val cli = DialectorCstCli()
        val validResult = cli.test(listOf("validate", validFile.absolutePath))
        assertEquals(0, validResult.statusCode, "CLI output:\n${validResult.output}")
        assertTrue(validResult.output.contains("Valid schema 'ValidLang' (dev.dialector.valid)"))

        val invalidFile = writeCstFile(
            "Invalid.cst",
            """
            schema BrokenLang
            package dev.dialector.broken

            node BadNode {
                missingRef: UnknownTargetType
            }
            """,
        )

        val invalidResult = cli.test(listOf("validate", invalidFile.absolutePath))
        assertEquals(1, invalidResult.statusCode)
        assertTrue(invalidResult.stderr.contains("UnknownTargetType"))
    }

    @Test
    fun `commands fail with non-zero exit code when directory has no cst files`() {
        val emptyDir = File(tempDir, "empty").apply { mkdirs() }
        val cli = DialectorCstCli()

        val genResult = cli.test(listOf("generate", emptyDir.absolutePath))
        assertEquals(1, genResult.statusCode)
        assertTrue(genResult.stderr.contains("No .cst schema files found"))

        val valResult = cli.test(listOf("validate", emptyDir.absolutePath))
        assertEquals(1, valResult.statusCode)
        assertTrue(valResult.stderr.contains("No .cst schema files found"))
    }
}
