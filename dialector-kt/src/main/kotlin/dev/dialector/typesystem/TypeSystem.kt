package dev.dialector.typesystem

interface TypeSystem {
    val supertypeRelations: List<SupertypeRelation>
    val typeLattice: TypeLattice
}