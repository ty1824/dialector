package dev.dialector.typesystem.integration

import dev.dialector.model.Node
import dev.dialector.model.NodeClause

interface BoundsContext {

}

/**
 * A rule that defines inference that should be performed for nodes matching a [NodeClause]
 */
interface InferenceRule<T : Node> {
    val isValidFor: NodeClause<T>
    val infer: ProgramInferenceContext.(node: T) -> Unit

    operator fun invoke(context: ProgramInferenceContext, node: Node) {
        if (isValidFor(node)) context.infer(node as T)
    }
}

infix fun <T : Node> NodeClause<T>.infer(infer: ProgramInferenceContext.(node: T) -> Unit): InferenceRule<T> =
    object : InferenceRule<T> {
        override val isValidFor: NodeClause<T> = this@infer
        override val infer: ProgramInferenceContext.(node: T) -> Unit = infer
    }

/*
inline class SampleInferenceContext() : InferenceContext {

}

fun InferenceContext.generateConstraints(rule: InferenceContext.() -> Unit) = this.rule()

fun foo(inferenceSystem: InferenceSystem) {
    val context = SampleInferenceContext(inferenceSystem)
    context.generateConstraints {
        x subtype y
        y equal z
    }
}
*/