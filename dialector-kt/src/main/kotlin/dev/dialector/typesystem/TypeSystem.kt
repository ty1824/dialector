package dev.dialector.typesystem

import dev.dialector.typesystem.lattice.SupertypeRelation
import dev.dialector.typesystem.lattice.TypeLattice

/**
 * An extensible type system providing a type lattice and inference engine intended to help power language
 * semantic checks.
 *
 *
 */
interface TypeSystem {
    val supertypeRelations: List<SupertypeRelation<*>>
    val typeLattice: TypeLattice
}