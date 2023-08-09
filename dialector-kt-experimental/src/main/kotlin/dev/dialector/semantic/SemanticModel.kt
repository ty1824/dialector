package dev.dialector.semantic

import dev.dialector.semantic.type.Type
import dev.dialector.semantic.type.lattice.TypeLattice
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.SyntacticModel

interface Scope

/**
 * Maintains semantic information relating to a [SyntacticModel]
 */
public interface SemanticModel {
    public val typeLattice: TypeLattice

    public fun typeOf(node: Node): Type?

    public fun scopeFor(node: Node): Scope?

    public fun resolve(reference: NodeReference<*>): Node?
}
