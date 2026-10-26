package dev.dialector.cst

import dev.dialector.cst.ConcreteSyntaxTree
import dev.dialector.cst.CstNode
import dev.dialector.cst.CstToken
import dev.dialector.cst.DiagnosticSeverity
import dev.dialector.cst.ERROR_NODE_KIND
import dev.dialector.cst.NO_SLOT
import dev.dialector.cst.NULL_NODE_INDEX
import dev.dialector.cst.buildCst
import dev.dialector.cst.encodeTokenKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}
