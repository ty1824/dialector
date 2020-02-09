package dev.dialector.typesystem.plugin

import dev.dialector.typesystem.SupertypeRelation

/**
 * Describes a plugin for a TypeSystem
 */
interface TypeSystemPlugin {
    val subtypeRelations: List<SupertypeRelation>
}