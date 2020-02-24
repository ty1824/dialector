package dev.dialector.typesystem.inference

interface InferenceVariable {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

sealed class InferenceRelation {
    abstract val left: InferenceVariable
    abstract val right: InferenceVariable

    data class Equality(override val left: InferenceVariable, override val right: InferenceVariable) : InferenceRelation()
    data class Subtype(override val left: InferenceVariable, override val right: InferenceVariable) : InferenceRelation()
}

interface InferenceEngine {
    fun solve(relations: Sequence<InferenceRelation>)
}

class DefaultInferenceEngine : InferenceEngine{
    override fun solve(relations: Sequence<InferenceRelation>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}