package dev.dialector.glottony.ast

import dev.dialector.model.Child
import dev.dialector.model.Node
import dev.dialector.model.NodeDefinition
import dev.dialector.model.NodeReference
import dev.dialector.model.Property
import dev.dialector.model.Reference

@NodeDefinition
interface TopLevelConstruct : Node

@NodeDefinition
interface File : Node {
    @Child
    val contents: List<TopLevelConstruct>
}

@NodeDefinition
interface StructDeclaration : TopLevelConstruct {
    @Property
    val name: String
    /* TODO: Add more */
}

@NodeDefinition
interface FunctionDeclaration : TopLevelConstruct {
    @Property
    val name: String

    @Child
    val parameters: ParameterList

    @Child
    val type: Type

    // For now...
    @Property
    val body: String
}

@NodeDefinition
interface ParameterList : Node {
    @Child
    val parameters: List<Parameter>
}

@NodeDefinition
interface Parameter : Node {
    @Property
    val name: String

    @Child
    val type: Type
}

@NodeDefinition
interface Type : Node

@NodeDefinition
interface IntegerType : Type

@NodeDefinition
interface NumberType : Type

@NodeDefinition
interface StringType : Type

@NodeDefinition
interface StructType : Type {
    @Reference
    val ofStruct: NodeReference<StructDeclaration>
}
