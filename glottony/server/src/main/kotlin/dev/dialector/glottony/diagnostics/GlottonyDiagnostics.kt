package dev.dialector.glottony.diagnostics

import dev.dialector.diagnostic.DiagnosticEvaluationContext
import dev.dialector.diagnostic.DiagnosticRule
import dev.dialector.diagnostic.check
import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.ast.FunctionDeclaration
import dev.dialector.glottony.ast.ReferenceExpression
import dev.dialector.glottony.ast.ValStatement
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.glottony.typesystem.asType
import dev.dialector.semantic.Scope
import dev.dialector.syntax.Node
import dev.dialector.syntax.getAllDescendants
import dev.dialector.syntax.given
import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.TypeLattice
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.RootId

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
            val bodyType = typeOf(it.body)
            val expectedType = it.type.asType()
            if (bodyType != null) checkAssignability(bodyType, expectedType, it.body)
        },
        given<ValStatement>() check {
            val expectedType = it.type
            if (expectedType != null) {
                val actualType = typeOf(it.expression)
                if (actualType != null) checkAssignability(actualType, expectedType.asType(), it.expression)
            }
        },
        given<ReferenceExpression>() check {
            if (resolve(it.target) == null) diagnostic("Invalid reference target ${it.target.targetIdentifier}", it)
        }
    )

    suspend fun evaluate(root: GlottonyRoot): List<ModelDiagnostic> {
        val diagnostics = mutableListOf<ModelDiagnostic>()
        println("Computing types")
        val resolvedTypes = typesystem.requestInferenceResult(root)

        val context = object : DiagnosticEvaluationContext {
            var currentNode: Node = root.rootNode

            override val typeLattice: TypeLattice = typesystem.lattice

            override fun typeOf(node: Node): Type? = resolvedTypes[node]

            override fun scopeFor(node: Node): Scope? {
                TODO("Not yet implemented")
            }

            override fun resolve(reference: NodeReference<*>): Node? {
                TODO("Not yet implemented")
            }

            override fun diagnostic(message: String, node: Node) {
                diagnostics += SimpleModelDiagnostic(currentNode, node, message)
            }

            override fun getRoots(): Sequence<Node> {
                TODO("Not yet implemented")
            }

            override fun getRoot(id: RootId): Node? {
                TODO("Not yet implemented")
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