package dev.dialector.semantic

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ScopeGraphTest {

    @Test
    fun simpleDeclarations() {
        val graph = ScopeGraph()
        val scope = mockk<ScopeVariable>()
        val element = mockk<Node>()
        val name = "hello"
        graph.declare(scope, element, name, null)

        assertContains(graph.getDeclarations(scope), name to element)

        val otherElement = mockk<Node>()
        val otherName = "goodbye"
        graph.declare(scope, otherElement, otherName, null)

        assertContains(graph.getDeclarations(scope), name to element)
        assertContains(graph.getDeclarations(scope), otherName to otherElement)
    }

    @Test
    fun simpleReference() {
        val graph = ScopeGraph()
        val scope = mockk<ScopeVariable>()
        val element = mockk<Node>()
        val name = "hello"
        val reference = mockk<NodeReference<Node>>()
        every { reference.targetIdentifier } returns name

        graph.declare(scope, element, name, null)
        graph.reference(reference, scope, null)

        assertEquals(element, graph.getTarget(reference))

        val otherElement = mockk<Node>()
        val otherName = "goodbye"
        val otherReference = mockk<NodeReference<Node>>()
        every { otherReference.targetIdentifier } returns otherName

        graph.declare(scope, otherElement, otherName, null)
        graph.reference(otherReference, scope, null)

        assertEquals(element, graph.getTarget(reference))
        assertEquals(otherElement, graph.getTarget(otherReference))
    }

    @Test
    fun inheritingScope() {
        val graph = ScopeGraph()
        val parentScope = mockk<ScopeVariable>()
        val childScope = mockk<ScopeVariable>()
        val element = mockk<Node>()
        val name = "hello"
        graph.declare(parentScope, element, name, null)
        graph.inherit(childScope, parentScope, "test")

        assertFalse(
            graph.getDeclarations(childScope).contains(name to element),
            "Immediate child scope should not contain parent element",
        )
        assertContains(graph.getAllDeclarations(childScope), name to element)
    }

    @Test
    fun resolvingWithInheritance() {
        val graph = ScopeGraph()
        val parentScope = mockk<ScopeVariable>()
        val childScope = mockk<ScopeVariable>()
        val element = mockk<Node>()
        val name = "hello"
        val reference = mockk<NodeReference<Node>>()
        every { reference.targetIdentifier } returns name
        graph.declare(parentScope, element, name, null)
        graph.inherit(childScope, parentScope, "test")
        graph.reference(reference, childScope, null)

        // Should resolve to parent element
        assertEquals(element, graph.getTarget(reference))

        val otherElement = mockk<Node>()
        val otherReference = mockk<NodeReference<Node>>()
        every { otherReference.targetIdentifier } returns name
        graph.declare(childScope, otherElement, name, null)

        // Should resolve to child element now that it is "hiding" the parent element
        assertEquals(otherElement, graph.getTarget(reference))
    }
}
