package dev.dialector.genmodel

import dev.dialector.model.*
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@NodeDefinition
interface ValidNode : Node {
  companion object

  @Property
  val validProperty: String

  @Child
  val validChild: ValidNode

  @Reference
  val validReference: NodeReference<ValidNode>
}


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
