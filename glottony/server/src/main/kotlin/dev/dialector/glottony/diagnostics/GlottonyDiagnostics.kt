package dev.dialector.glottony.diagnostics

import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.glottony.typesystem.GlottonyTypesystemContext
import dev.dialector.glottony.typesystem.asType
import dev.dialector.model.Node
import dev.dialector.model.getAllDescendants
import dev.dialector.model.given
import dev.dialector.typesystem.Type
import dev.dialector.typesystem.lattice.TypeLattice

interface ModelDiagnostic {
    /**
     * The node where this diagnostic originated
     */
    val origin: Node

    /**
     * The node this diagnostic pertains to.
     */
    val target: Node

    /**
     * The diagnostic message
     */
    val message: String
}

data class SimpleModelDiagnostic(
    override val origin: Node,
    override val target: Node,
    override val message: String
) : ModelDiagnostic

fun DiagnosticEvaluationContext.checkAssignability(candidate: Type, expected: Type, targetNode: Node) {
    if (!typeLattice.isSubtypeOf(candidate, expected)) {
        diagnostic("Return type $candidate is not assignable to $expected", targetNode)
    }
}

class GlottonyDiagnosticProvider(private val typesystemContext: GlottonyTypesystemContext) {
    val diagnosticRules: List<DiagnosticRule<*>> = listOf(
        given<FunctionDeclaration>() check {
            val bodyType = getTypeOf(it.body)
            val expectedType = it.type.asType()
            if (bodyType != null) checkAssignability(bodyType, expectedType, it.body)
        },
        given<ValStatement>() check {
            val expectedType = it.type
            if (expectedType != null) {
                val actualType = getTypeOf(it.expression)
                if (actualType != null) checkAssignability(actualType, expectedType.asType(), it.expression)
            }
        }
    )

    fun evaluate(root: Node): List<ModelDiagnostic> {
        val diagnostics = mutableListOf<ModelDiagnostic>()
        val context = object : DiagnosticEvaluationContext {
            override val typeLattice: TypeLattice = typesystemContext.lattice
            val resolvedTypes = typesystemContext.inferTypes(root)
            var currentNode: Node = root

            override fun getTypeOf(node: Node): Type? = resolvedTypes[node]

            override fun diagnostic(message: String, node: Node) {
                diagnostics += SimpleModelDiagnostic(currentNode, node, message)
            }

        }
        context.apply {
            for (node in root.getAllDescendants(true)) {
                context.currentNode = node
                for (rule in diagnosticRules) {
                    rule(this, node)
                }
            }
        }
        return diagnostics.toList()
    }

}