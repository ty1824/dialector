package dev.dialector.syntax.sample

import dev.dialector.syntax.*

@NodeDefinition
interface MStruct : Node {
    @Property
    val name: String

    @Child()
    val fields: List<MStructField>
}

@NodeDefinition
interface MStructField : Node {
    @Property
    val name: String

    @Property
    val type: String
}



interface MType : Node {}

interface MFunction: Node {}