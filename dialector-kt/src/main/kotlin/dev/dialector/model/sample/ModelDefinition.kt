package dev.dialector.model.sample

import dev.dialector.model.*

@NodeDefinition
interface MClass : Node {
    @Property
    val name: String

    @Child
    val fields: List<MField>

    @Child
    val functions: List<MFunction>
}

interface MField : Node {
    @Property
    val name: String

    @Property
    val type: String
}

interface MFunction: Node {}