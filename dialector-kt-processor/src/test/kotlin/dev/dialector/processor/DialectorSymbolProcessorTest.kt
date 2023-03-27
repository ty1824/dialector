package dev.dialector.processor

import dev.dialector.processor.ast.childNode
import dev.dialector.processor.ast.simpleNode
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
