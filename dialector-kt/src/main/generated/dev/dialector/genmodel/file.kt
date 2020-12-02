package dev.dialector.genmodel

import dev.dialector.model.Node
import dev.dialector.model.NodeReference
import dev.dialector.model.ValidNode
import dev.dialector.model.sample.MStruct
import dev.dialector.model.sample.MStructField
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

private class ValidNodeImpl(
  init: ValidNodeInitializer
) : ValidNode, Node {
  override var validProperty: String = init.validProperty!!

  override var validChild: ValidNode = init.validChild!!

  override var validReference: NodeReference<ValidNode> = init.validReference!!

  override var parent: Node? = null

  override val properties: Map<String, Any?>
    get() = mapOf("validProperty" to validProperty)
  override val children: Map<String, List<Node>>
    get() = mapOf("validChild" to listOf(validChild))
  override val references: Map<String, NodeReference<*>>
    get() = mapOf("validReference" to validReference)}

class ValidNodeInitializer {
  var validProperty: String? = null

  var validChild: ValidNode? = null

  var validReference: NodeReference<ValidNode>? = null

  fun build(): ValidNode = ValidNodeImpl(this)
}

private class MStructImpl(
  init: MStructInitializer
) : MStruct, Node {
  override var name: String = init.name!!

  override val fields: List<MStructField> = init.fields.toMutableList()

  override var parent: Node? = null

  override val properties: Map<String, Any?>
    get() = mapOf("name" to name)
  override val children: Map<String, List<Node>>
    get() = mapOf("fields" to (fields))
  override val references: Map<String, NodeReference<*>>
    get() = mapOf()}

class MStructInitializer {
  var name: String? = null

  var fields: List<MStructField> = mutableListOf()

  fun build(): MStruct = MStructImpl(this)
}

private class MStructFieldImpl(
  init: MStructFieldInitializer
) : MStructField, Node {
  override var name: String = init.name!!

  override var type: String = init.type!!

  override var parent: Node? = null

  override val properties: Map<String, Any?>
    get() = mapOf("name" to name"type" to type)
  override val children: Map<String, List<Node>>
    get() = mapOf()
  override val references: Map<String, NodeReference<*>>
    get() = mapOf()}

class MStructFieldInitializer {
  var name: String? = null

  var type: String? = null

  fun build(): MStructField = MStructFieldImpl(this)
}
