package dev.dialector.typesystem.inference.new

import dev.dialector.typesystem.Type

interface BoundsContext {

}

interface InferenceRule {

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