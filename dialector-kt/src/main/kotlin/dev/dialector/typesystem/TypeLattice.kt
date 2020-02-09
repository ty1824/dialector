package dev.dialector.typesystem

import kotlin.reflect.KClass

interface TypeLattice {
    fun isSubtypeOf(candidate: Type, supertype: Type)
    fun leastCommonSupertype(types: Iterable<Type>)
}

interface SupertypeRelation {
    val forTypeClause: TypeClause
    fun supertypes(type: Type): Iterable<Type>
}

infix fun TypeClause.hasSupertypes(supertypes: (type: Type) -> Iterable<Type>): SupertypeRelation = object : SupertypeRelation {
    override val forTypeClause = this@hasSupertypes
    override fun supertypes(type: Type): Iterable<Type> = supertypes(type)
}

//val test = type(object : Type {}) hasSupertypes { object : Type {} }



class SampleTypeLattice(typeSystem: TypeSystem) : TypeLattice {
    private val knownSuperclasses = typeSystem.supertypeRelations
            .filter { it.forTypeClause is TypeObjectClause}
            .map {
                val type = (it.forTypeClause as TypeObjectClause).type
                type to it.supertypes(type)
            }

    private val typeClassRules: Map<KClass<out Type>, SupertypeRelation> = typeSystem.supertypeRelations
            .filter { it.forTypeClause is TypeClassClause }
            .map { (it.forTypeClause as TypeClassClause).typeClass to it}
            .toMap()

    private val nonSpecializedRules = typeSystem.supertypeRelations.filter { it.forTypeClause !is TypeClassClause || it.forTypeClause !is TypeObjectClause }

    private val subtypeCache: MutableMap<Type, Set<Type>> = mutableMapOf<Type, Set<Type>>().apply {
        typeSystem.supertypeRelations.forEach {
            val typeClause = it.forTypeClause
            if (typeClause is TypeObjectClause) {
                it.supertypes(typeClause.type).forEach { supertype ->
                    this[supertype]
                }
            }
        }
    }

    override fun isSubtypeOf(candidate: Type, supertype: Type) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun leastCommonSupertype(types: Iterable<Type>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}