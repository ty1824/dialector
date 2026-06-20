package dev.dialector.processor

import dev.dialector.processor.ast.childNode
import dev.dialector.processor.ast.simpleNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val node =
            simpleNode {
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

        // Verify generic properties/children/references
        assertEquals(3, node.properties.size)
        assertEquals(3, node.children.size)
        assertEquals(2, node.references.size)

        assertEquals(
            setOf(node::singleChild.name, node::optionalChild.name, node::pluralChildren.name),
            node.children.keys,
        )
        assertEquals(setOf(node::reference.name, node::optionalReference.name), node.references.keys)
        assertEquals(setOf(node::property.name, node::optionalProperty.name, node::defaultProperty.name), node.properties.keys)

        assertEquals(node.property, node.properties[node::property.name])
        assertEquals(node.optionalProperty, node.properties[node::optionalProperty.name])
        assertEquals(node.defaultProperty, node.properties[node::defaultProperty.name])

        assertEquals(listOf(node.singleChild), node.children[node::singleChild.name])
        val optionalChild = node.children[node::optionalChild.name]!!
        assertTrue(optionalChild.size < 2)
        assertEquals(node.optionalChild, optionalChild.firstOrNull())
        assertEquals(node.pluralChildren, node.children[node::pluralChildren.name])

        assertEquals(node.reference, node.references[node::reference.name])
        assertEquals(node.optionalReference, node.references[node::optionalReference.name])

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
        val node =
            simpleNode(
                "hello",
                "provided",
                null,
                singleChildValue,
                optionalChildValue,
                listOf(pluralFirstChildValue, pluralSecondChildValue, pluralThirdChildValue),
                "target.id",
                "target.otherId",
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
}
