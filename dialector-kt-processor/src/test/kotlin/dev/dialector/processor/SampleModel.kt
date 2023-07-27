package dev.dialector.processor

import dev.dialector.syntax.Child
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeDefinition
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.Property
import dev.dialector.syntax.Reference

@NodeDefinition
interface SimpleNode : Node {
    @Property
    val property: String

    @Property
    val optionalProperty: String?

    @Property(hasDefault = true)
    val defaultProperty: String
        get() = "default"

    @Child
    val singleChild: ChildNode

    @Child
    val optionalChild: ChildNode

    @Child
    val pluralChildren: List<ChildNode>

    @Reference
    val reference: NodeReference<ReferenceTargetNode>

    @Reference
    val optionalReference: NodeReference<ReferenceTargetNode>?
}

@NodeDefinition
interface ChildNode : Node

@NodeDefinition
interface ReferenceTargetNode : Node {
    @Property
    val name: String
}
