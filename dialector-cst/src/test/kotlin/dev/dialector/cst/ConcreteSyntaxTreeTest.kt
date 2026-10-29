package dev.dialector.cst

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ConcreteSyntaxTreeTest {

    @Test
    fun `empty tree should report zero nodes and valid invariants`() {
        val empty = ConcreteSyntaxTree.empty()
        assertEquals(0, empty.nodeCount)
        assertEquals(NULL_NODE_INDEX, empty.getRootNodeIndex())
        assertEquals(CstNode.NONE, empty.root)
        assertFalse(empty.root.isValid)
        assertTrue(empty.root.isNone)
        assertFalse(empty.hasErrors)
        assertTrue(empty.diagnostics.isEmpty())
    }

    @Test
    fun `builder creates flat cst with valid primitive arrays, slots, and hierarchy`() {
        val source = "val x = 42 + 1"
        val tree = buildCst(source) {
            val root = startNode(kind = 100, startOffset = 0) // VarDecl

            addToken(schemaTokenId = 1, startOffset = 0, endOffset = 3, parentNode = root, slot = 0)
            addToken(schemaTokenId = 2, startOffset = 4, endOffset = 5, parentNode = root, slot = 1)
            addToken(schemaTokenId = 3, startOffset = 6, endOffset = 7, parentNode = root, slot = 2)

            val binaryExpr = startNode(kind = 200, startOffset = 8) // BinaryExpr
            addChild(parentNodeIndex = root, childNodeIndex = binaryExpr, slot = 3)

            addToken(schemaTokenId = 4, startOffset = 8, endOffset = 10, parentNode = binaryExpr, slot = 0)
            addToken(schemaTokenId = 5, startOffset = 11, endOffset = 12, parentNode = binaryExpr, slot = 1)
            addToken(schemaTokenId = 4, startOffset = 13, endOffset = 14, parentNode = binaryExpr, slot = 2)

            finishNode(binaryExpr, endOffset = 14)
            finishNode(root, endOffset = 14)
        }

        assertEquals(8, tree.nodeCount)
        assertEquals(0, tree.getRootNodeIndex())
        assertEquals(CstNode(0), tree.root)
        assertFalse(tree.hasErrors)

        // Verify root node
        assertEquals(100, tree.getNodeKind(0))
        assertEquals(NULL_NODE_INDEX, tree.getNodeParent(0))
        assertEquals(0, tree.getNodeStartOffset(0))
        assertEquals(14, tree.getNodeLength(0))
        assertEquals(14, tree.getNodeEndOffset(0))
        assertEquals("val x = 42 + 1", tree.getText(0))
        assertEquals(4, tree.getChildNodeCount(0))

        // Verify binary expr
        assertEquals(200, tree.getNodeKind(4))
        assertEquals(0, tree.getNodeParent(4))
        assertEquals(8, tree.getNodeStartOffset(4))
        assertEquals(6, tree.getNodeLength(4))
        assertEquals(14, tree.getNodeEndOffset(4))
        assertEquals("42 + 1", tree.getText(4))
        assertEquals(3, tree.getChildNodeCount(4))

        // Verify slot lookups
        assertEquals(1, tree.getChildBySlot(0, 0))
        assertEquals(2, tree.getChildBySlot(0, 1))
        assertEquals(3, tree.getChildBySlot(0, 2))
        assertEquals(4, tree.getChildBySlot(0, 3))
        assertEquals(NULL_NODE_INDEX, tree.getChildBySlot(0, 99))

        assertEquals(5, tree.getChildBySlot(4, 0))
        assertEquals(6, tree.getChildBySlot(4, 1))
        assertEquals(7, tree.getChildBySlot(4, 2))

        // Verify slot in parent
        assertEquals(0, tree.getNodeSlotInParent(1))
        assertEquals(1, tree.getNodeSlotInParent(2))
        assertEquals(2, tree.getNodeSlotInParent(3))
        assertEquals(3, tree.getNodeSlotInParent(4))
        assertEquals(NO_SLOT.toInt(), tree.getNodeSlotInParent(0))

        // Verify sibling navigation
        assertEquals(NULL_NODE_INDEX, tree.getPrevSibling(1))
        assertEquals(2, tree.getNextSibling(1))
        assertEquals(1, tree.getPrevSibling(2))
        assertEquals(3, tree.getNextSibling(2))
        assertEquals(NULL_NODE_INDEX, tree.getNextSibling(4))

        // Verify leaf texts
        assertEquals("val", tree.getText(1))
        assertEquals("x", tree.getText(2))
        assertEquals("=", tree.getText(3))
        assertEquals("42", tree.getText(5))
        assertEquals("+", tree.getText(6))
        assertEquals("1", tree.getText(7))

        // Verify descendants & ancestors
        val rootDescendants = tree.getAllDescendants(0).toList()
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), rootDescendants)

        val binaryAncestors = tree.getAllAncestors(4).toList()
        assertEquals(listOf(0), binaryAncestors)

        val leftNumAncestors = tree.getAllAncestors(5).toList()
        assertEquals(listOf(4, 0), leftNumAncestors)
    }

    @Test
    fun `context parameter handles access tree data correctly`() {
        val tree = buildCst("foo") {
            val root = startNode(kind = 1, startOffset = 0)
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 3, parentNode = root, slot = 0)
            finishNode(root, 3)
        }

        val rootHandle = CstNode(0)
        val tokenHandle = CstToken(1)

        assertTrue(rootHandle.isValid)
        assertFalse(rootHandle.isNone)
        assertTrue(tokenHandle.isValid)
        assertFalse(tokenHandle.isNone)
        assertEquals("CstNode(0)", rootHandle.toString())
        assertEquals("CstToken(1)", tokenHandle.toString())
        assertEquals("CstNode.NONE", CstNode.NONE.toString())
        assertEquals("CstToken.NONE", CstToken.NONE.toString())

        with(tree) {
            assertEquals(1, rootHandle.kind)
            assertFalse(rootHandle.isToken)
            assertFalse(rootHandle.isTrivia)
            assertFalse(rootHandle.isMissing)
            assertFalse(rootHandle.isError)
            assertEquals("foo", rootHandle.text)
            assertEquals(0, rootHandle.startOffset)
            assertEquals(3, rootHandle.length)
            assertEquals(3, rootHandle.endOffset)
            assertEquals(CstNode.NONE, rootHandle.parent)
            assertEquals(1, rootHandle.childCount)
            assertEquals(tokenHandle.asNode, rootHandle.childAt(0))
            assertEquals(CstNode.NONE, rootHandle.childAt(1))
            assertEquals(tokenHandle.asNode, rootHandle.childBySlot(0))
            assertEquals(CstNode.NONE, rootHandle.childBySlot(99))
            assertEquals(tokenHandle, rootHandle.tokenBySlot(0))
            assertEquals(CstToken.NONE, rootHandle.tokenBySlot(99))

            // Children list view
            val childrenList = rootHandle.children
            assertEquals(1, childrenList.size)
            assertFalse(childrenList.isEmpty)
            assertEquals(tokenHandle.asNode, childrenList[0])
            assertEquals(CstNode.NONE, childrenList[1])

            val collectedChildren = mutableListOf<CstNode>()
            childrenList.forEach { collectedChildren.add(it) }
            assertEquals(listOf(tokenHandle.asNode), collectedChildren)

            val slotCollected = mutableListOf<CstNode>()
            rootHandle.forEachChildInSlot(0) { slotCollected.add(it) }
            assertEquals(listOf(tokenHandle.asNode), slotCollected)

            // Token handle
            assertTrue(tokenHandle.asNode.isToken)
            assertFalse(tokenHandle.isTrivia)
            assertFalse(tokenHandle.isMissing)
            assertEquals(encodeTokenKind(10), tokenHandle.kind)
            assertEquals("foo", tokenHandle.text)
            assertEquals(0, tokenHandle.startOffset)
            assertEquals(3, tokenHandle.length)
            assertEquals(3, tokenHandle.endOffset)
            assertEquals(rootHandle, tokenHandle.parent)
            assertEquals(rootHandle, tokenHandle.asNode.parent)
            assertEquals(0, tokenHandle.asNode.childCount)
            assertEquals(CstNode.NONE, tokenHandle.prevSibling)
            assertEquals(CstNode.NONE, tokenHandle.nextSibling)
        }
    }

    @Test
    fun `error recovery features missing nodes, error nodes, and diagnostics`() {
        val tree = buildCst("var = 42") {
            val root = startNode(kind = 1, startOffset = 0)
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 3, parentNode = root, slot = 0) // var

            // Missing identifier at offset 3
            addMissingToken(
                schemaTokenId = 20,
                offset = 3,
                parentNode = root,
                slot = 1,
                diagnosticCode = "MISSING_ID",
                diagnosticMessage = "Expected identifier after 'var'",
            )

            // Error node wrapping unexpected '='
            val err = startErrorNode(startOffset = 4)
            addToken(schemaTokenId = 30, startOffset = 4, endOffset = 5, parentNode = err)
            finishNode(err, endOffset = 5)
            addChild(root, err, slot = NO_SLOT.toInt())

            addToken(schemaTokenId = 40, startOffset = 6, endOffset = 8, parentNode = root, slot = 2) // 42
            finishNode(root, endOffset = 8)
        }

        assertTrue(tree.hasErrors)
        assertEquals(1, tree.diagnostics.size)
        val diag = tree.diagnostics.first()
        assertEquals("MISSING_ID", diag.code)
        assertEquals("Expected identifier after 'var'", diag.message)
        assertEquals(DiagnosticSeverity.ERROR, diag.severity)

        with(tree) {
            val rootNode = tree.root
            assertEquals(4, rootNode.childCount)

            val missingToken = rootNode.tokenBySlot(1)
            assertTrue(missingToken.isValid)
            assertTrue(missingToken.isMissing)
            assertEquals(0, missingToken.length)
            assertEquals(3, missingToken.startOffset)
            assertEquals(3, missingToken.endOffset)
            assertEquals("", missingToken.text)

            val errorNode = rootNode.childAt(2)
            assertTrue(errorNode.isError)
            assertEquals(ERROR_NODE_KIND, errorNode.kind)
            assertEquals(1, errorNode.childCount)
            assertEquals("=", errorNode.text)
        }
    }

    @Test
    fun `trivia tokens and offset lookups with depth-first walk`() {
        val tree = buildCst("x + 1") {
            val root = startNode(kind = 1)
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 1, parentNode = root, slot = 0)
            addTrivia(schemaTokenId = 100, startOffset = 1, endOffset = 2, parentNode = root)
            addToken(schemaTokenId = 11, startOffset = 2, endOffset = 3, parentNode = root, slot = 1)
            addTrivia(schemaTokenId = 100, startOffset = 3, endOffset = 4, parentNode = root)
            addToken(schemaTokenId = 12, startOffset = 4, endOffset = 5, parentNode = root, slot = 2)
            finishNode(root)
        }

        with(tree) {
            val wsNode = tree.root.childAt(1)
            assertTrue(wsNode.isTrivia)
            assertTrue(wsNode.asToken.isTrivia)

            // nodeAtOffset
            val atZero = tree.nodeAtOffset(0)
            assertEquals(1, atZero.nodeIndex) // 'x' token

            val atWs = tree.nodeAtOffset(1, includeTrivia = true)
            assertEquals(2, atWs.nodeIndex) // whitespace trivia

            val atWsNoTrivia = tree.nodeAtOffset(1, includeTrivia = false)
            assertEquals(0, atWsNoTrivia.nodeIndex) // root node

            // tokenAtOffset
            val tokAtZero = tree.tokenAtOffset(0)
            assertEquals(1, tokAtZero.tokenIndex)

            // walkDepthFirst
            val allVisited = mutableListOf<Int>()
            tree.walkDepthFirst(includeTrivia = true) { allVisited.add(it.nodeIndex) }
            assertEquals(listOf(0, 1, 2, 3, 4, 5), allVisited)

            val nonTriviaVisited = mutableListOf<Int>()
            tree.walkDepthFirst(includeTrivia = false) { nonTriviaVisited.add(it.nodeIndex) }
            assertEquals(listOf(0, 1, 3, 5), nonTriviaVisited)

            val descendantsExclusive = mutableListOf<Int>()
            tree.forEachDescendant(0, inclusive = false, includeTrivia = false) { descendantsExclusive.add(it.nodeIndex) }
            assertEquals(listOf(1, 3, 5), descendantsExclusive)

            val ancestorsInclusive = mutableListOf<Int>()
            tree.forEachAncestor(5, inclusive = true) { ancestorsInclusive.add(it.nodeIndex) }
            assertEquals(listOf(5, 0), ancestorsInclusive)
        }
    }

    @Test
    fun `debug tree string outputs readable structure`() {
        val tree = buildCst("x + 1") {
            val root = startNode(kind = 1, startOffset = 0)
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 1, parentNode = root, slot = 0)
            addTrivia(schemaTokenId = 100, startOffset = 1, endOffset = 2, parentNode = root)
            addToken(schemaTokenId = 11, startOffset = 2, endOffset = 3, parentNode = root, slot = 1)
            addMissingToken(
                schemaTokenId = 12,
                offset = 3,
                parentNode = root,
                slot = 2,
                diagnosticMessage = "Missing right operand"
            )
            finishNode(root, 3)
        }

        val debugStr = tree.toDebugTreeString(
            nodeNameLookup = { if (it == 1) "BinaryExpr" else "Node#$it" },
            tokenNameLookup = {
                when (it) {
                    10 -> "Identifier"
                    11 -> "Plus"
                    12 -> "Number"
                    100 -> "Whitespace"
                    else -> "Token#$it"
                }
            }
        )

        assertTrue(debugStr.contains("BinaryExpr[0]"))
        assertTrue(debugStr.contains("slot#0: 'x' (Identifier[1])"))
        assertTrue(debugStr.contains("' ' (Whitespace[2])"))
        assertTrue(debugStr.contains("slot#1: '+' (Plus[3])"))
        assertTrue(debugStr.contains("slot#2: '' (Number[4]) <MISSING>"))
        assertTrue(debugStr.contains("Diagnostics (1):"))
        assertTrue(debugStr.contains("Missing right operand"))
    }

    @Test
    fun `bottom-up span calculation supports parentIdx greater than childIdx and trailing missing tokens`() {
        val source = "   10 + 20"
        val tree = buildCst(source) {
            // Left operand allocated first (index 0) before parent BinaryExpr (index 2)
            val leftLit = startNode(kind = 10)
            addToken(schemaTokenId = 1, startOffset = 3, endOffset = 5, parentNode = leftLit, slot = 0)

            // Parent BinaryExpr allocated after leftLit (parentIdx > childIdx), left unfinished until build()
            val binaryExpr = startNode(kind = 20)
            addChild(parentNodeIndex = binaryExpr, childNodeIndex = leftLit, slot = 0)
            addToken(schemaTokenId = 2, startOffset = 6, endOffset = 7, parentNode = binaryExpr, slot = 1)

            val rightLit = startNode(kind = 10)
            addToken(schemaTokenId = 1, startOffset = 8, endOffset = 10, parentNode = rightLit, slot = 0)
            addChild(parentNodeIndex = binaryExpr, childNodeIndex = rightLit, slot = 2)

            // Trailing missing token with offset 0 should not truncate binaryExpr's resolvedEnd
            addMissingToken(schemaTokenId = 3, offset = 0, parentNode = binaryExpr, slot = 3)

            // Trailing empty composite child (resolved to 0..0) should also not truncate binaryExpr's resolvedEnd
            val emptyTrailingChild = startNode(kind = 30)
            addChild(parentNodeIndex = binaryExpr, childNodeIndex = emptyTrailingChild, slot = 4)
        }

        // binaryExpr is at index 2; its span should be 3..10 ("10 + 20", length 7)
        assertEquals(3, tree.getNodeStartOffset(0))
        assertEquals(2, tree.getNodeLength(0))
        assertEquals(3, tree.getNodeStartOffset(2))
        assertEquals(7, tree.getNodeLength(2))
        assertEquals(10, tree.getNodeEndOffset(2))
        assertEquals("10 + 20", tree.getText(2))
    }

    @Test
    fun `SourceRange and SourcePosition provide zero-allocation range and line-column lookups`() {
        val source = "val x = 10\nval y = 20\r\nval z = 30"
        val tree = buildCst(source) {
            val root = startNode(kind = 1)
            // line 1: "val" at 0..3, "10" at 8..10
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 3, parentNode = root, slot = 0)
            addToken(schemaTokenId = 11, startOffset = 8, endOffset = 10, parentNode = root, slot = 1)
            // line 2: "val" at 11..14, "20" at 19..21
            addToken(schemaTokenId = 10, startOffset = 11, endOffset = 14, parentNode = root, slot = 2)
            addToken(schemaTokenId = 11, startOffset = 19, endOffset = 21, parentNode = root, slot = 3)
            // line 3 (after \r\n at 21..23): "val" at 23..26, "30" at 31..33
            addToken(schemaTokenId = 10, startOffset = 23, endOffset = 26, parentNode = root, slot = 4)
            addToken(schemaTokenId = 11, startOffset = 31, endOffset = 33, parentNode = root, slot = 5)
            reportDiagnostic(nodeIndex = 6, startOffset = 31, endOffset = 33, code = "TEST", message = "Test diag")
            finishNode(root)
        }

        // Line table checks
        assertEquals(3, tree.lineCount)
        assertEquals("val x = 10", tree.getLineText(1))
        assertEquals("val y = 20", tree.getLineText(2))
        assertEquals("val z = 30", tree.getLineText(3))
        assertEquals(SourceRange(0, 10), tree.getLineRange(1))
        assertEquals(SourceRange(11, 21), tree.getLineRange(2))
        assertEquals(SourceRange(11, 23), tree.getLineRange(2, includeLineBreak = true))
        assertEquals(SourceRange(23, 33), tree.getLineRange(3))
        assertEquals(SourceRange.NONE, tree.getLineRange(0))
        assertEquals(SourceRange.NONE, tree.getLineRange(4))

        // Bidirectional offset <-> SourcePosition
        assertEquals(SourcePosition(1, 1), tree.getPosition(0))
        assertEquals(SourcePosition(1, 9), tree.getPosition(8))
        assertEquals(SourcePosition(2, 1), tree.getPosition(11))
        assertEquals(SourcePosition(2, 9), tree.getPosition(19))
        assertEquals(SourcePosition(3, 1), tree.getPosition(23))
        assertEquals(SourcePosition(3, 9), tree.getPosition(31))
        assertEquals(SourcePosition(3, 11), tree.getPosition(33))
        assertEquals(SourcePosition.NONE, tree.getPosition(-1))
        assertEquals(SourcePosition.NONE, tree.getPosition(100))

        assertEquals(0, tree.getOffset(1, 1))
        assertEquals(8, tree.getOffset(SourcePosition(1, 9)))
        assertEquals(10, tree.getOffset(1, 11)) // '\n' on line 1
        assertEquals(-1, tree.getOffset(1, 12)) // past line 1's '\n' (start of line 2)
        assertEquals(11, tree.getOffset(2, 1))
        assertEquals(19, tree.getOffset(2, 9))
        assertEquals(23, tree.getOffset(3, 1))
        assertEquals(31, tree.getOffset(3, 9))
        assertEquals(33, tree.getOffset(3, 11)) // EOF on final line
        assertEquals(-1, tree.getOffset(3, 12))
        assertEquals(-1, tree.getOffset(0, 1))
        assertEquals(-1, tree.getOffset(1, 50))

        // Canonical NONE normalization
        assertEquals(SourceRange.NONE, SourceRange(-5, 10))
        assertEquals(SourcePosition.NONE, SourcePosition(0, 1))
        assertEquals(SourcePosition.NONE, SourcePosition(1, 0))

        // SourceRange operations
        val r1 = SourceRange(11, 21)
        assertTrue(r1.isValid)
        assertFalse(r1.isNone)
        assertEquals(10, r1.length)
        assertFalse(r1.isEmpty)
        assertEquals(11..<21, r1.asIntRange)
        assertTrue(11 in r1)
        assertTrue(20 in r1)
        assertFalse(21 in r1)
        assertTrue(SourceRange(12, 18) in r1)
        assertTrue(r1.intersects(SourceRange(15, 25)))
        assertFalse(r1.intersects(SourceRange(21, 25)))
        assertEquals("11..21", r1.toString())
        assertEquals("SourceRange.NONE", SourceRange.NONE.toString())

        // Node, Token, and Diagnostic accessors
        with(tree) {
            val tokline2Num = tree.root.tokenBySlot(3)
            assertEquals(SourceRange(19, 21), tokline2Num.sourceRange)
            assertEquals("20", tree.getText(tokline2Num.sourceRange))
            assertEquals(SourcePosition(2, 9), tokline2Num.startPosition)
            assertEquals(SourcePosition(2, 11), tokline2Num.endPosition)
            assertEquals(1, tokline2Num.startPosition.lineIndex)
            assertEquals(8, tokline2Num.startPosition.columnIndex)
            assertTrue(tokline2Num.startPosition < tokline2Num.endPosition)
            assertEquals("2:9", tokline2Num.startPosition.toString())

            assertEquals(SourceRange.NONE, CstNode.NONE.sourceRange)
            assertEquals(SourcePosition.NONE, CstNode.NONE.startPosition)
            assertEquals(SourceRange(31, 33), tree.diagnostics.first().sourceRange)
        }
    }

    @Test
    fun `builder auto-sorts out-of-order children so nodeAtOffset and tokenAtOffset succeed`() {
        val source = "val x = 42"
        val tree = buildCst(source) {
            val root = startNode(kind = 1)
            // Attach children out of source order (right-to-left)
            val numTok = addToken(schemaTokenId = 4, startOffset = 8, endOffset = 10, parentNode = root, slot = 3)
            val eqTok = addToken(schemaTokenId = 3, startOffset = 6, endOffset = 7, parentNode = root, slot = 2)
            val idTok = addToken(schemaTokenId = 2, startOffset = 4, endOffset = 5, parentNode = root, slot = 1)
            val valTok = addToken(schemaTokenId = 1, startOffset = 0, endOffset = 3, parentNode = root, slot = 0)
            finishNode(root)

            // Verify node indices before build
            assertEquals(1, numTok)
            assertEquals(4, valTok)
        }

        // Root span should cover 0..10 ("val x = 42")
        assertEquals(0, tree.getNodeStartOffset(0))
        assertEquals(10, tree.getNodeLength(0))
        assertEquals("val x = 42", tree.getText(0))

        // Offset lookups should find each leaf token rather than falling back to root
        assertEquals(4, tree.tokenAtOffset(0).tokenIndex) // "val"
        assertEquals(3, tree.tokenAtOffset(4).tokenIndex) // "x"
        assertEquals(2, tree.tokenAtOffset(6).tokenIndex) // "="
        assertEquals(1, tree.tokenAtOffset(8).tokenIndex) // "42"

        // Slot bindings must remain intact after auto-sorting
        assertEquals(4, tree.getChildBySlot(0, 0))
        assertEquals(3, tree.getChildBySlot(0, 1))
        assertEquals(2, tree.getChildBySlot(0, 2))
        assertEquals(1, tree.getChildBySlot(0, 3))
    }

    @Test
    fun `builder enforces unmodifiable diagnostics and optional cycle detection`() {
        val tree = buildCst("err", checkCycles = true) {
            val root = startNode(kind = 1)
            addToken(schemaTokenId = 1, startOffset = 0, endOffset = 3, parentNode = root, slot = 0)
            reportDiagnostic(nodeIndex = root, startOffset = 0, endOffset = 3, code = "ERR", message = "Error")
            finishNode(root)
        }

        // Diagnostics list must reject mutation via cast
        @Suppress("UNCHECKED_CAST")
        val mutableCast = tree.diagnostics as MutableList<CstDiagnostic>
        kotlin.test.assertFailsWith<UnsupportedOperationException> {
            mutableCast.clear()
        }

        // Cycle detection in addChild when checkCycles = true
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            buildCst("cycle", checkCycles = true) {
                val n1 = startNode(kind = 1)
                val n2 = startNode(kind = 2)
                addChild(n1, n2, slot = 0)
                addChild(n2, n1, slot = 0)
            }
        }

        // Cycle detection in build() when checkCycles = true is passed to build()
        kotlin.test.assertFailsWith<IllegalStateException> {
            val builder = ConcreteSyntaxBuilder(initialCapacity = 8, checkCycles = false)
            val n1 = builder.startNode(kind = 1, startOffset = 0)
            val n2 = builder.startNode(kind = 2, startOffset = 0)
            builder.finishNode(n1, 1)
            builder.finishNode(n2, 1)
            builder.addChild(n1, n2, slot = 0)
            builder.addChild(n2, n1, slot = 0)
            builder.build("x", checkCycles = true)
        }
    }

    @Test
    fun `error tokens and four slot binding states work without wrapper overhead`() {
        // Source: "val x ; = " (where ';' is a skipped recovery token, optional slot 0 is absent,
        // slot 1 is present 'x', slot 2 is present '=', slot 3 is unavailable expr, slot 4 is missing ';')
        val source = "val x ; = "
        val tree = buildCst(source) {
            val root = startNode(kind = 1)
            // slot 0: optional 'mut' keyword -> left absent (NONE)
            addToken(schemaTokenId = 10, startOffset = 0, endOffset = 3, parentNode = root, slot = 1) // "val" (Present)
            addToken(schemaTokenId = 11, startOffset = 4, endOffset = 5, parentNode = root, slot = 2) // "x" (Present)
            // Stray ';' marked directly as a recovery/error token without an ErrorNode wrapper
            addErrorToken(
                schemaTokenId = 99,
                startOffset = 6,
                endOffset = 7,
                parentNode = root,
                diagnosticCode = "UNEXPECTED_SEMI",
                diagnosticMessage = "Unexpected ';'",
            )
            addToken(schemaTokenId = 12, startOffset = 8, endOffset = 9, parentNode = root, slot = 3) // "=" (Present)
            // Slot 4: mandatory expression could not be established -> Unavailable
            addUnavailableNode(
                offset = 10,
                parentNode = root,
                slot = 4,
                diagnosticCode = "EXPECTED_EXPR",
                diagnosticMessage = "Expected expression after '='",
            )
            // Slot 5: mandatory trailing semicolon -> Missing
            addMissingToken(
                schemaTokenId = 99,
                offset = 10,
                parentNode = root,
                slot = 5,
                diagnosticCode = "MISSING_SEMI",
                diagnosticMessage = "Expected ';'",
            )
            finishNode(root)
        }

        assertTrue(tree.hasErrors)
        assertEquals(3, tree.diagnostics.size)

        with(tree) {
            val root = tree.root

            // 1. Absent (optional slot 0 not populated)
            val slot0 = root.childBySlot(0)
            assertTrue(slot0.isNone)
            assertFalse(slot0.isValid)
            assertFalse(slot0.isPresent)
            assertFalse(slot0.isMissing)
            assertFalse(slot0.isError)

            // 2. Present (slot 1: "val")
            val slot1 = root.tokenBySlot(1)
            assertTrue(slot1.isValid)
            assertTrue(slot1.isPresent)
            assertFalse(slot1.isNone)
            assertFalse(slot1.isMissing)
            assertFalse(slot1.isError)
            assertEquals("val", slot1.text)

            // 3. Unavailable (slot 4: expression recovery failed)
            val slot4 = root.childBySlot(4)
            assertTrue(slot4.isValid)
            assertTrue(slot4.isError)
            assertFalse(slot4.isPresent)
            assertFalse(slot4.isNone)
            assertFalse(slot4.isMissing)
            assertEquals(0, slot4.length)

            // 4. Missing (slot 5: synthesized ';')
            val slot5 = root.tokenBySlot(5)
            assertTrue(slot5.isValid)
            assertTrue(slot5.isMissing)
            assertFalse(slot5.isPresent)
            assertFalse(slot5.isNone)
            assertFalse(slot5.isError)
            assertEquals(99, slot5.schemaId)

            // 5. Recovery/Error Token (stray ';' at offset 6..7)
            val errTok = tree.tokenAtOffset(6)
            assertTrue(errTok.isValid)
            assertTrue(errTok.isError)
            assertTrue(errTok.asNode.isError)
            assertFalse(errTok.isPresent)
            assertFalse(errTok.isMissing)
            assertFalse(errTok.isTrivia)
            assertEquals(99, errTok.schemaId)
            assertEquals(";", errTok.text)

            val debugStr = tree.toDebugTreeString()
            assertTrue(debugStr.contains("';' (Token#99[3]) <ERROR> (6..7)"))
        }
    }

    @Test
    fun `ConcreteSyntaxTree works with Document and custom Document implementations`() {
        val customDoc = object : Document {
            override val text: String = "line1\nline2\nline3"
            // Custom line offsets: line 1 at 0, line 2 at 6, line 3 at 12
            override val lineOffsets: IntArray = intArrayOf(0, 6, 12)
        }

        val tree = buildCst(document = customDoc) {
            val root = startNode(kind = 1)
            addToken(schemaTokenId = 1, startOffset = 0, endOffset = 5, parentNode = root)
            addToken(schemaTokenId = 2, startOffset = 6, endOffset = 11, parentNode = root)
            finishNode(root)
        }

        assertSame(customDoc, tree.document)
        assertEquals("line1\nline2\nline3", tree.document.text)
        assertEquals(3, tree.lineCount)
        assertEquals("line1", tree.getLineText(1))
        assertEquals("line2", tree.getLineText(2))
        assertEquals("line3", tree.getLineText(3))
        assertEquals(SourcePosition(2, 1), tree.getPosition(6))
        assertEquals(SourcePosition(3, 1), tree.getPosition(12))

        with(tree) {
            assertEquals(customDoc, tree.root.document)
            val tok = tree.tokenAtOffset(0)
            assertEquals(customDoc, tok.document)
            assertEquals("line1", tok.text)
        }

        // Test StringDocument and factory functions
        val stringDoc = Document("hello\nworld")
        assertEquals(2, stringDoc.lineOffsets.size)
        assertEquals(0, stringDoc.lineOffsets[0])
        assertEquals(6, stringDoc.lineOffsets[1])
        assertEquals("hello\nworld", stringDoc.text)

        val emptyTree = ConcreteSyntaxTree.empty(document = customDoc)
        assertSame(customDoc, emptyTree.document)
    }
}
