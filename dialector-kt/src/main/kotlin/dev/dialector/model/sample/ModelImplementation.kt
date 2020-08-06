package dev.dialector.model.sample

import dev.dialector.model.Node
import dev.dialector.model.NodeReference
import kotlin.reflect.KProperty

data class PropertyValue<T>(var value: T) {
    operator fun getValue(inst: Any?, property: KProperty<*>): T = value

    operator fun setValue(inst: Any?, property: KProperty<*>, newValue: T) {
        value = newValue
    }
}

class MClassInlineImpl : MClass {
    override var name: String by PropertyValue("")
    override val fields: MutableList<MField> = mutableListOf()
    override val functions: MutableList<MFunction> = mutableListOf()

    override var parent: Node? = null

    override val properties: Map<KProperty<*>, Any?> = mapOf(MClass::name to name)
    override val children: Map<KProperty<*>, List<Node>> = mapOf(MClass::fields to fields)
    override val references: Map<KProperty<*>, List<NodeReference<*>>> = mapOf()

    override fun allChildren(): List<Node> = fields + functions
    override fun allReferences(): List<NodeReference<*>> = listOf()
}

class MFieldInlineImpl : MField {
    override var name: String by PropertyValue("")
    override val type: String by PropertyValue("")

    override val parent: Node? = null

    override val properties: Map<KProperty<*>, Any?> = mapOf(MField::name to name, MField::type to type)
    override val children: Map<KProperty<*>, List<Node>> = mapOf()
    override val references: Map<KProperty<*>, List<NodeReference<*>>> = mapOf()

    override fun allChildren(): List<Node> = listOf()
    override fun allReferences(): List<NodeReference<*>> = listOf()
}