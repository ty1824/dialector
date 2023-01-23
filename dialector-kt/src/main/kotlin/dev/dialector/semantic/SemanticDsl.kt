package dev.dialector.semantic


interface SemanticAnalysisContext {
    fun <S : SemanticSystem> getSystem(definition: SemanticSystemDefinition<S>): S

    fun <A, D> query(data: SemanticDataDefinition<*, A, D>, argument: A): Query<A, D>
}