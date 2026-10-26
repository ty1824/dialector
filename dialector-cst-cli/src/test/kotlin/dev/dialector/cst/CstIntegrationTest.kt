package dev.dialector.cst

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import dev.dialector.cst.generator.CstCodeGenerator
import dev.dialector.cst.generator.CstKotlinPoetGenerator
import dev.dialector.cst.schema.CategorySchema
import dev.dialector.cst.schema.CstSchema
import dev.dialector.cst.schema.FieldSchema
import dev.dialector.cst.schema.FieldType
import dev.dialector.cst.schema.NodeSchema
import dev.dialector.cst.schema.TokenSchema
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class CstIntegrationTest {

    @Test
    fun `generated cst code compiles and executes successfully`() {
        val schema = CstSchema(
            name = "ToyLang",
            packageName = "dev.dialector.test.generated",
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
                    name = "NumberLiteral",
                    id = 1,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("value", FieldType.ChildToken("Number"), slot = 0),
                    ),
                ),
                NodeSchema(
                    name = "BinaryExpression",
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

        val generator = CstCodeGenerator()
        val result = generator.generate(schema)

        val sources = result.files.map { file ->
            SourceFile.kotlin(file.fileName, file.content)
        }

        // Also add a test user file using context parameters
        val testUserSource = SourceFile.kotlin(
            "TestUsage.kt",
            """
            package dev.dialector.test.generated

            import dev.dialector.cst.*

            object TestRunner {
                fun runTest(): String {
                    val tree = buildCst("10 + 20") {
                        val binary = addBinaryExpression(0, 7)
                        val leftNum = addNumberLiteral(0, 2, parentNode = binary.nodeIndex, slot = 0)
                        val leftTok = addNumberToken(0, 2, parentNode = leftNum.nodeIndex, slot = 0)

                        val plusTok = addPlusToken(3, 4, parentNode = binary.nodeIndex, slot = 1)

                        val rightNum = addNumberLiteral(5, 7, parentNode = binary.nodeIndex, slot = 2)
                        val rightTok = addNumberToken(5, 7, parentNode = rightNum.nodeIndex, slot = 0)
                    }

                    val binaryHandle = BinaryExpressionHandle(0)
                    with(tree) {
                        val left = binaryHandle.left.asNumberLiteral
                        val right = binaryHandle.right.asNumberLiteral
                        val op = binaryHandle.operator
                        val opText = op.asNode.text
                        return left.nodeIndex.toString() + ":" + op.tokenIndex.toString() + ":" + right.nodeIndex.toString() + ":" + opText
                    }
                }
            }
            """.trimIndent()
        )

        val compilationResult = KotlinCompilation().apply {
            this.sources = sources + testUserSource
            this.jvmTarget = "17"
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode, "Compilation messages: ${compilationResult.messages}")

        // Execute via reflection
        val runnerClass = compilationResult.classLoader.loadClass("dev.dialector.test.generated.TestRunner")
        val runnerObject = runnerClass.getField("INSTANCE").get(null)
        val method = runnerClass.getMethod("runTest")
        val output = method.invoke(runnerObject) as String

        assertEquals("1:3:4:+", output)
    }

    @Test
    fun `complex schema with optional slots, lists, and deep hierarchies compiles and executes`() {
        val schema = CstSchema(
            name = "FullLang",
            packageName = "dev.dialector.test.full",
            tokens = listOf(
                TokenSchema("IfKeyword", id = 1),
                TokenSchema("ElseKeyword", id = 2),
                TokenSchema("Identifier", id = 3),
                TokenSchema("Semicolon", id = 4),
            ),
            categories = listOf(
                CategorySchema("Statement"),
                CategorySchema("Expression"),
            ),
            nodes = listOf(
                NodeSchema(
                    name = "IfStatement",
                    id = 1,
                    categories = listOf("Statement"),
                    fields = listOf(
                        FieldSchema("ifToken", FieldType.ChildToken("IfKeyword"), slot = 0),
                        FieldSchema("condition", FieldType.ChildNode("Expression"), slot = 1),
                        FieldSchema("thenBranch", FieldType.ChildNode("Statement"), slot = 2),
                        FieldSchema("elseBranch", FieldType.ChildNode("Statement", isOptional = true), slot = 3),
                    ),
                ),
                NodeSchema(
                    name = "VarDecl",
                    id = 2,
                    categories = listOf("Statement"),
                    fields = listOf(
                        FieldSchema("name", FieldType.ChildToken("Identifier"), slot = 0),
                        FieldSchema("initExpr", FieldType.ChildNode("Expression", isOptional = true), slot = 1),
                        FieldSchema("semicolon", FieldType.ChildToken("Semicolon", isOptional = true), slot = 2),
                    ),
                ),
                NodeSchema(
                    name = "BlockStatement",
                    id = 3,
                    categories = listOf("Statement"),
                    fields = listOf(
                        FieldSchema("statements", FieldType.ChildNodeList("Statement"), slot = 0),
                    ),
                ),
                NodeSchema(
                    name = "VarRef",
                    id = 4,
                    categories = listOf("Expression"),
                    fields = listOf(
                        FieldSchema("identifier", FieldType.ChildToken("Identifier"), slot = 0),
                    ),
                ),
            ),
        ).validate()

        val result = CstCodeGenerator().generate(schema)
        val sources = result.files.map { SourceFile.kotlin(it.fileName, it.content) }

        val usageSource = SourceFile.kotlin(
            "FullLangUsage.kt",
            """
            package dev.dialector.test.full

            import dev.dialector.cst.*

            object FullLangRunner {
                fun testOptionalPresentAndAbsent(): Pair<Boolean, Boolean> {
                    val tree = buildCst("if x foo else bar") {
                        val ifNode = addIfStatement(0, 17)
                        val ifTok = addIfKeywordToken(0, 2, parentNode = ifNode.nodeIndex, slot = 0)
                        val cond = addVarRef(3, 4, parentNode = ifNode.nodeIndex, slot = 1)
                        val thenNode = addBlockStatement(5, 8, parentNode = ifNode.nodeIndex, slot = 2)
                        val elseNode = addBlockStatement(14, 17, parentNode = ifNode.nodeIndex, slot = 3)
                    }

                    val ifHandle = IfStatementHandle(0)
                    val hasElse = with(tree) { ifHandle.elseBranch.isValid }

                    val treeWithoutElse = buildCst("if x foo") {
                        val ifNode = addIfStatement(0, 8)
                        val ifTok = addIfKeywordToken(0, 2, parentNode = ifNode.nodeIndex, slot = 0)
                        val cond = addVarRef(3, 4, parentNode = ifNode.nodeIndex, slot = 1)
                        val thenNode = addBlockStatement(5, 8, parentNode = ifNode.nodeIndex, slot = 2)
                    }
                    val ifHandle2 = IfStatementHandle(0)
                    val hasElse2 = with(treeWithoutElse) { ifHandle2.elseBranch.isValid }

                    return Pair(hasElse, hasElse2)
                }

                fun testListChildren(): Int {
                    val tree = buildCst("{ a; b; c; }") {
                        val block = addBlockStatement(0, 12)
                        val s1 = addVarDecl(2, 4, parentNode = block.nodeIndex, slot = 0)
                        val s2 = addVarDecl(5, 7, parentNode = block.nodeIndex, slot = 0)
                        val s3 = addVarDecl(8, 10, parentNode = block.nodeIndex, slot = 0)
                    }

                    val blockHandle = BlockStatementHandle(0)
                    with(tree) {
                        return blockHandle.statements.size
                    }
                }

                fun testResilience(): Boolean {
                    val tree = buildCst("if") {
                        val ifNode = addIfStatement(0, 2)
                        addIfKeywordToken(0, 2, parentNode = ifNode.nodeIndex, slot = 0)
                        addMissingVarRef(offset = 2, parentNode = ifNode.nodeIndex, slot = 1, diagnosticMessage = "Missing condition")
                    }
                    val ifHandle = IfStatementHandle(0)
                    with(tree) {
                        val cond = ifHandle.condition
                        return tree.hasErrors && cond.isValid && cond.asNode.isMissing && cond.asNode.length == 0
                    }
                }
            }
            """.trimIndent()
        )

        val compilationResult = KotlinCompilation().apply {
            this.sources = sources + usageSource
            this.jvmTarget = "17"
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, compilationResult.exitCode, "Compilation failed: ${compilationResult.messages}")

        val runnerClass = compilationResult.classLoader.loadClass("dev.dialector.test.full.FullLangRunner")
        val runnerObject = runnerClass.getField("INSTANCE").get(null)

        val testOptionalMethod = runnerClass.getMethod("testOptionalPresentAndAbsent")
        @Suppress("UNCHECKED_CAST")
        val optionalResult = testOptionalMethod.invoke(runnerObject) as Pair<Boolean, Boolean>
        assertEquals(Pair(true, false), optionalResult)

        val testListMethod = runnerClass.getMethod("testListChildren")
        val listResult = testListMethod.invoke(runnerObject) as Int
        assertEquals(3, listResult)

        val testResilienceMethod = runnerClass.getMethod("testResilience")
        val resilienceResult = testResilienceMethod.invoke(runnerObject) as Boolean
        assertTrue(resilienceResult)
    }

    @Test
    fun `file writer and kotlin poet generator produce expected artifacts`() {
        val schema = CstSchema(
            name = "DiskLang",
            packageName = "dev.dialector.disk",
            tokens = listOf(
                TokenSchema("TokenA", id = 1),
            ),
            nodes = listOf(
                NodeSchema(
                    name = "NodeA",
                    id = 1,
                    fields = listOf(
                        FieldSchema("tok", FieldType.ChildToken("TokenA"), slot = 0),
                    ),
                ),
            ),
        ).validate()

        val result = CstCodeGenerator().generate(schema)
        val tempDir = createTempDirectory("cst_test").toFile()
        try {
            val writtenFiles = result.writeTo(tempDir)
            assertEquals(3, writtenFiles.size)
            for (f in writtenFiles) {
                assertTrue(f.exists())
                assertTrue(f.readText().isNotBlank())
            }

            // Test KotlinPoet generator
            val poetGen = CstKotlinPoetGenerator()
            val fileSpecs = poetGen.generateFileSpecs(schema)
            assertEquals(3, fileSpecs.size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `self-defining CstSchemaLanguage cst file parses, validates, and generates working CstSchemaDsl handles`() {
        val cstFile = java.io.File("src/main/kotlin/dev/dialector/cst/schema/dsl/CstSchemaLanguage.cst")
        assertTrue(cstFile.isFile, "Expected CstSchemaLanguage.cst at ${cstFile.absolutePath}")

        val dslSource = cstFile.readText()
        val parsedSchema = dev.dialector.cst.schema.dsl.CstSchemaParser.parse(dslSource).validate()
        val selfHostedSchema = dev.dialector.cst.schema.dsl.CstSchemaParser.selfHostedSchema()

        // 1. Verify CstSchemaLanguage.cst matches CstSchemaParser.selfHostedSchema()
        assertEquals(selfHostedSchema, parsedSchema)
        assertEquals("CstSchemaDsl", parsedSchema.name)
        assertEquals("dev.dialector.cst.schema.dsl", parsedSchema.packageName)
        assertEquals(15, parsedSchema.nodes.size)
        assertEquals(2, parsedSchema.categories.size)
        assertEquals(27, parsedSchema.tokens.size)
        assertEquals(1, parsedSchema.tokenCategories.size)

        // 2. Generate code from CstSchemaLanguage.cst and verify it matches the checked-in CstSchemaDsl* structure
        val result = CstCodeGenerator().generate(parsedSchema)
        assertEquals(3, result.files.size)

        val kindsFile = result.files.first { it.fileName == "CstSchemaDslKinds.kt" }
        val handlesFile = result.files.first { it.fileName == "CstSchemaDslHandles.kt" }
        val extensionsFile = result.files.first { it.fileName == "CstSchemaDslExtensions.kt" }

        // Verify the checked-in CstSchemaDsl* files match the generator output byte-for-byte
        val dslDir = java.io.File("src/main/kotlin/dev/dialector/cst/schema/dsl")
        assertEquals(java.io.File(dslDir, "CstSchemaDslKinds.kt").readText(), kindsFile.content)
        assertEquals(java.io.File(dslDir, "CstSchemaDslHandles.kt").readText(), handlesFile.content)
        assertEquals(java.io.File(dslDir, "CstSchemaDslExtensions.kt").readText(), extensionsFile.content)

        for (node in parsedSchema.nodes) {
            assertTrue(
                kindsFile.content.contains("const val ") && kindsFile.content.contains("\"${node.name}\""),
                "Missing node kind for ${node.name}",
            )
            assertTrue(
                handlesFile.content.contains("value class ${node.name}Handle("),
                "Missing node handle for ${node.name}",
            )
            assertTrue(
                extensionsFile.content.contains("fun ConcreteSyntaxBuilder.add${node.name}("),
                "Missing builder extension for ${node.name}",
            )
        }

        // 3. Compile the freshly generated CstSchemaDsl* files in an isolated test package alongside a consumer
        val isolatedSchema = parsedSchema.copy(packageName = "dev.dialector.cst.schema.dsl.verified")
        val isolatedResult = CstCodeGenerator().generate(isolatedSchema)
        val generatedSources = isolatedResult.files.map { SourceFile.kotlin(it.fileName, it.content) }

        val verificationUsage = SourceFile.kotlin(
            "SelfHostedVerification.kt",
            """
            package dev.dialector.cst.schema.dsl.verified

            import dev.dialector.cst.ConcreteSyntaxTree

            object SelfHostedVerifier {
                fun verifyTree(cst: ConcreteSyntaxTree): String = with(cst) {
                    val rootFile = cst.root.asSchemaFileOrNone
                    if (rootFile.isNone) return "INVALID_ROOT"
                    val schemaName = rootFile.schemaHeader.name.asNode.text
                    var nodeDeclCount = 0
                    var tokenDeclCount = 0
                    rootFile.forEachDeclarations { decl ->
                        if (decl.isNodeDecl) nodeDeclCount++
                        if (decl.isTokenDecl) tokenDeclCount++
                    }
                    "${'$'}schemaName:${'$'}nodeDeclCount:${'$'}tokenDeclCount"
                }
            }
            """.trimIndent(),
        )

        val compilationResult = KotlinCompilation().apply {
            this.sources = generatedSources + verificationUsage
            this.jvmTarget = "17"
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertEquals(
            KotlinCompilation.ExitCode.OK,
            compilationResult.exitCode,
            "Freshly generated CstSchemaDsl* failed to compile:\n${compilationResult.messages}",
        )

        val flatCst = dev.dialector.cst.schema.dsl.CstSchemaParser.parseToFlatCst(dslSource)
        val verifierClass = compilationResult.classLoader.loadClass(
            "dev.dialector.cst.schema.dsl.verified.SelfHostedVerifier"
        )
        val verifierInstance = verifierClass.getField("INSTANCE").get(null)
        val verifyMethod = verifierClass.getMethod("verifyTree", ConcreteSyntaxTree::class.java)
        val summary = verifyMethod.invoke(verifierInstance, flatCst) as String

        assertEquals("CstSchemaDsl:15:27", summary)
    }
}
