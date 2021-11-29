package dev.dialector.syntax.sample

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference

class MStructInlineImpl : MStruct {
    override var name: String = ""
    override val fields: MutableList<MStructField> = mutableListOf()

    override var parent: Node? = null

    override val properties: Map<String, Any?> = mapOf("name" to name)
    override val children: Map<String, List<Node>> = mapOf("fields" to fields)
    override val references: Map<String, NodeReference<*>> = mapOf()
}

class MFieldInlineImpl(
    override val parent: Node? = null,
    name: String = "",
    type: String = ""
) : MStructField {
    override var name: String = ""
    override var type: String = ""

    override val properties: Map<String, Any?> = mapOf("name" to name, "type" to type)
    override val children: Map<String, List<Node>> = mapOf()
    override val references: Map<String, NodeReference<*>> = mapOf()
}