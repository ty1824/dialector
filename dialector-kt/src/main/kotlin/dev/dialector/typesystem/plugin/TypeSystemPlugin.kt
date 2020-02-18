package dev.dialector.typesystem.plugin

import dev.dialector.typesystem.SupertypeRelation

/**
 * Describes a plugin for a TypeSystem
 */
interface TypeSystemPlugin {
    val subtypeRelations: List<SupertypeRelation>
}

/**
 * @param C The type of the configuration object this plugin supports.
 */
abstract class ConfigurableTypeSystemPlugin<C>