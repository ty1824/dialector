package dev.dialector.glottony.diagnostics

import dev.dialector.diagnostic.DiagnosticEvaluationContext
import dev.dialector.diagnostic.DiagnosticRule
import dev.dialector.diagnostic.check
import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.glottony.typesystem.asType
import dev.dialector.syntax.Node
import dev.dialector.syntax.getAllDescendants
import dev.dialector.syntax.given
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.TypeLattice

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

class GlottonyDiagnosticProvider(private val typesystem: GlottonyTypesystem) {
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

    suspend fun evaluate(root: GlottonyRoot): List<ModelDiagnostic> {
        val diagnostics = mutableListOf<ModelDiagnostic>()
        println("Computing types")
        val resolvedTypes = typesystem.requestInferenceResult(root)

        val context = object : DiagnosticEvaluationContext {
            override val typeLattice: TypeLattice = typesystem.lattice
            var currentNode: Node = root.rootNode

            override fun getTypeOf(node: Node): Type? = resolvedTypes[node]

            override fun diagnostic(message: String, node: Node) {
                diagnostics += SimpleModelDiagnostic(currentNode, node, message)
            }

        }
        try {
            println("Computing diagnostics")
            context.apply {
                for (node in root.rootNode.getAllDescendants(true)) {
                    context.currentNode = node
                    for (rule in diagnosticRules) {
                        rule(this, node)
                    }
                }
            }
            println("Computed diagnostics")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return diagnostics.toList()
    }

}