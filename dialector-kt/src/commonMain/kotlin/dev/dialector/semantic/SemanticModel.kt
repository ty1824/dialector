package dev.dialector.semantic

import dev.dialector.syntax.SyntacticModel

/**
 * Maintains semantic information relating to a [SyntacticModel]
 */
interface SemanticModel {
    val program: SyntacticModel
}