package dev.dialector.syntax

import dev.dialector.TestNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

interface NodeOne : Node

interface NodeTwo : Node

interface NodeThree : Node

class DModelTest {
    @Test
    fun testGetRoot() {
        mockkStatic(Node::getAllChildren) {
            val root = mockk<NodeOne> {
                every { parent } returns null
            }
            val child = mockk<NodeOne> {
                every { parent } returns root
            }
            val grandchild = mockk<NodeOne> {
                every { parent } returns child
            }

            val root2 = mockk<NodeOne> {
                every { parent } returns null
            }

            assertEquals(root, grandchild.getRoot())
            assertEquals(root, child.getRoot())
            assertEquals(root, root.getRoot())
            assertEquals(root2, root2.getRoot())
        }
    }

    @Test
    fun testDescendants() {
        mockkStatic(Node::getAllChildren) {
            val grandchildOne = mockk<NodeOne> {
                every { getAllChildren() } returns listOf()
            }
            val grandchildTwo = mockk<NodeTwo> {
                every { getAllChildren() } returns listOf()
            }
            val grandchildThree = mockk<NodeThree> {
                every { getAllChildren() } returns listOf()
            }
            val childOne = mockk<NodeTwo> {
                every { getAllChildren() } returns listOf(grandchildThree)
            }
            val childTwo = mockk<NodeTwo> {
                every { getAllChildren() } returns listOf(grandchildOne, grandchildTwo)
            }
            val children = listOf(childOne, childTwo)
            val root = mockk<NodeOne> {
                every { getAllChildren() } returns children
            }

            val allRootDescendantsInclusive = root.getAllDescendants(true)
            assertContains(allRootDescendantsInclusive, root)
            val allRootDescendants = root.getAllDescendants()
            assertFalse(allRootDescendants.contains(root), "Expected to not find $root in $allRootDescendants")

            assertContentEquals(
                sequenceOf(root, childOne, childTwo, grandchildThree, grandchildOne, grandchildTwo),
                allRootDescendantsInclusive,
            )

            val allChildDescendantsInclusive = childTwo.getAllDescendants(true)
            assertContains(
                allChildDescendantsInclusive,
                childTwo,
                "Expected to find $childOne in $allRootDescendantsInclusive",
            )
            val allChildDescendants = childTwo.getAllDescendants()
            assertFalse(allChildDescendants.contains(childTwo), "Expected to not find $root in $allRootDescendants")
            assertContentEquals(sequenceOf(childTwo, grandchildOne, grandchildTwo), allChildDescendantsInclusive)

            val allGrandchildDescendants = grandchildOne.getAllDescendants()
            assertContentEquals(sequenceOf(), allGrandchildDescendants)

            val typedDescendants = root.getDescendants<NodeOne>(true)
            assertContentEquals(sequenceOf(root, grandchildOne), typedDescendants)
        }
    }

    @Test
    fun testAncestors() {
        val root = mockk<NodeOne> {
            every { parent } returns null
        }
        val childOne = mockk<NodeTwo> {
            every { parent } returns root
        }
        val childTwo = mockk<NodeTwo> {
            every { parent } returns root
        }
        val grandchildOne = mockk<NodeOne> {
            every { parent } returns childTwo
        }
        val grandchildTwo = mockk<NodeTwo> {
            every { parent } returns childTwo
        }

        val grandchildTwoAncestorsInclusive = grandchildTwo.getAllAncestors(true)
        assertContains(grandchildTwoAncestorsInclusive, grandchildTwo, "Expected to find $grandchildTwo in $grandchildTwoAncestorsInclusive")
        val grandchildTwoAncestors = root.getAllAncestors()
        assertFalse(grandchildTwoAncestors.contains(grandchildTwo), "Expected to not find $grandchildTwo in $grandchildTwoAncestors")

        assertContentEquals(
            sequenceOf(grandchildTwo, childTwo, root),
            grandchildTwoAncestorsInclusive,
        )

        val childTwoAncestorsInclusive = childTwo.getAllAncestors(true)
        assertContains(
            childTwoAncestorsInclusive,
            childTwo,
            "Expected to find $childOne in $childTwoAncestorsInclusive",
        )
        val childTwoAncestors = childTwo.getAllAncestors()
        assertFalse(childTwoAncestors.contains(childTwo), "Expected to not find $childTwo in $childTwoAncestors")
        assertContentEquals(sequenceOf(childTwo, root), childTwoAncestorsInclusive)

        val allRootAncestors = root.getAllAncestors()
        assertContentEquals(sequenceOf(), allRootAncestors)

        val typedAncestors = grandchildOne.getAncestors<NodeOne>(true)
        assertContentEquals(sequenceOf(grandchildOne, root), typedAncestors)
    }

    class SourceNode(refTarget: String) : TestNode() {
        val ref: NodeReference<TargetNode> = NodeReferenceImpl(this, SourceNode::ref, refTarget)
    }

    class TargetNode : TestNode()

    @Test
    fun referenceResolution() {
        val source = SourceNode("abc")
        val target = TargetNode()
        val invalidTarget = TestNode()
        val validResolver = ReferenceResolver { _ -> target }
        val invalidResolver = ReferenceResolver { _ -> invalidTarget }

        val validResolved: TargetNode? = validResolver.resolve(source.ref)
        assertNotNull(validResolved)
        assertEquals(target, validResolved)

        val invalidResolved: TargetNode? = invalidResolver.resolve(source.ref)
        assertNull(invalidResolved)

        with(validResolver) {
            val validResolvedContextual = source.ref.resolveTarget()
            assertNotNull(validResolvedContextual)
            assertEquals(target, validResolvedContextual)
        }

        with(invalidResolver) {
            val invalidResolvedContextual: TargetNode? = source.ref.resolveTarget()
            assertNull(invalidResolvedContextual)
        }
    }

    @Test
    fun getAllChildren() {
        val a = TestNode()
        val m = TestNode()
        val n = TestNode()
        val x = TestNode()
        val y = TestNode()
        a.children["one"] = listOf(m, n)
        a.children["two"] = listOf(x, y)

        assertEquals(setOf(m, n, x, y), a.getAllChildren().toSet())
    }

    @Test
    fun getAllReferences() {
        val a = TestNode()
        val x = mockk<NodeReference<Node>>()
        val y = mockk<NodeReference<Node>>()
        a.references["one"] = x
        a.references["two"] = y

        assertEquals(setOf(x, y), a.getAllReferences().toSet())
    }
}
