package dev.dialector.processor

import dev.dialector.processor.ast.childNode
import dev.dialector.processor.ast.copy
import dev.dialector.processor.ast.simpleNode
import dev.dialector.syntax.Node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

/**
 * These tests are designed to validate the generated code, both its API and its functionality.
 */
class DialectorSymbolProcessorTest {

    @Test
    fun processorApi() {
        val singleChildValue = childNode()
        val optionalChildValue = childNode()
        val pluralFirstChildValue = childNode()
        val pluralSecondChildValue = childNode()
        val pluralThirdChildValue = childNode()
        val node = simpleNode {
            property = "hello"
            optionalProperty = "provided"
            singleChild = singleChildValue
            optionalChild = optionalChildValue
            pluralChildren += pluralFirstChildValue
            pluralChildren += listOf(pluralSecondChildValue, pluralThirdChildValue)
            reference = "target.id"
            optionalReference = "target.otherId"
        }

        // Verify raw values
        assertEquals("hello", node.property)
        assertEquals("provided", node.optionalProperty)
        assertEquals("default", node.defaultProperty)
        assertEquals(singleChildValue, node.singleChild)
        assertEquals(optionalChildValue, node.optionalChild)
        assertEquals(listOf(pluralFirstChildValue, pluralSecondChildValue, pluralThirdChildValue), node.pluralChildren)
        assertEquals("target.id", node.reference.targetIdentifier)
        assertEquals("target.otherId", node.optionalReference?.targetIdentifier)

        // Verify parent assignment
        assertEquals(node, singleChildValue.parent)

        // Verify reference property assignment
        assertEquals(node, node.reference.sourceNode)
        assertEquals(SimpleNode::reference, node.reference.relation)
        assertEquals(SimpleNode::optionalReference, node.optionalReference?.relation)
    }

    @Test
    fun factoryApi() {
        val singleChildValue = childNode()
        val optionalChildValue = childNode()
        val pluralFirstChildValue = childNode()
        val pluralSecondChildValue = childNode()
        val pluralThirdChildValue = childNode()
        val node = simpleNode(
            "hello",
            "provided",
            null,
            singleChildValue,
            optionalChildValue,
            listOf(pluralFirstChildValue, pluralSecondChildValue, pluralThirdChildValue),
            "target.id",
            "target.otherId"
        )

        // Verify raw values
        assertEquals("hello", node.property)
        assertEquals("provided", node.optionalProperty)
        assertEquals("default", node.defaultProperty)
        assertEquals(singleChildValue, node.singleChild)
        assertEquals(optionalChildValue, node.optionalChild)
        assertEquals(listOf(pluralFirstChildValue, pluralSecondChildValue, pluralThirdChildValue), node.pluralChildren)
        assertEquals("target.id", node.reference.targetIdentifier)
        assertEquals("target.otherId", node.optionalReference?.targetIdentifier)

        // Verify parent assignment
        assertEquals(node, singleChildValue.parent)

        // Verify reference property assignment
        assertEquals(node, node.reference.sourceNode)
        assertEquals(SimpleNode::reference, node.reference.relation)
        assertEquals(SimpleNode::optionalReference, node.optionalReference?.relation)
    }

    @Test
    fun copyCreatesDeepCopyWithoutParent() {
        val singleChildValue = childNode()
        val optionalChildValue = childNode()
        val pluralFirstChildValue = childNode()
        val pluralSecondChildValue = childNode()
        val original = simpleNode {
            property = "hello"
            optionalProperty = "provided"
            singleChild = singleChildValue
            optionalChild = optionalChildValue
            pluralChildren += pluralFirstChildValue
            pluralChildren += pluralSecondChildValue
            reference = "target.id"
            optionalReference = "target.otherId"
        }

        // Copy the node - no cast needed thanks to the generated copy() extension function
        val copied = original.copy()

        // Verify copied node has no parent
        assertNull(copied.parent)

        // Verify properties are copied
        assertEquals(original.property, copied.property)
        assertEquals(original.optionalProperty, copied.optionalProperty)
        assertEquals(original.defaultProperty, copied.defaultProperty)

        // Verify children are deep copied (different instances)
        assertNotSame(original.singleChild, copied.singleChild)
        assertNotSame(original.optionalChild, copied.optionalChild)
        assertNotSame(original.pluralChildren[0], copied.pluralChildren[0])
        assertNotSame(original.pluralChildren[1], copied.pluralChildren[1])

        // Verify copied children have correct parent
        assertEquals(copied, copied.singleChild.parent)
        assertEquals(copied, copied.optionalChild?.parent)
        assertEquals(copied, copied.pluralChildren[0].parent)
        assertEquals(copied, copied.pluralChildren[1].parent)

        // Verify references point to same target identifiers
        assertEquals(original.reference.targetIdentifier, copied.reference.targetIdentifier)
        assertEquals(original.optionalReference?.targetIdentifier, copied.optionalReference?.targetIdentifier)

        // Verify reference source nodes are updated to the copied node
        assertEquals(copied, copied.reference.sourceNode)
        assertEquals(copied, copied.optionalReference?.sourceNode)

        // Verify original is unchanged
        assertEquals(original, original.singleChild.parent)
    }

    @Test
    fun copyAsNodeWorksPolymorphically() {
        val original: Node = simpleNode {
            property = "test"
            singleChild = childNode()
            reference = "ref.id"
        }

        // Copy via Node.copyAsNode() - this is the internal method, normally users would use
        // the generated copy() extension function on the concrete type
        val copied: Node = original.copyAsNode()

        // Verify it's a different instance
        assertNotSame(original, copied)

        // Verify it's the same type
        assertEquals(original::class, copied::class)

        // Verify parent is null
        assertNull(copied.parent)
    }

    @Test
    fun copyWorksForSimpleNodeWithoutChildren() {
        val original = childNode()

        val copied = original.copy()

        assertNotSame(original, copied)
        assertNull(copied.parent)
    }
}
