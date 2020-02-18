package dev.dialector.typesystem

/**
 * An extensible type system providing a type lattice and inference engine intended to help power language
 * semantic checks.
 *
 *
 */
interface TypeSystem {
    val supertypeRelations: List<SupertypeRelation>
    val typeLattice: TypeLattice
}