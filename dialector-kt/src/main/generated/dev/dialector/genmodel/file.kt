package dev.dialector.genmodel

import dev.dialector.model.Node
import dev.dialector.model.PropertyValue
import dev.dialector.model.sample.MClass
import kotlin.String

class MClassImpl : MClass(), Node {
  val name: String by PropertyValue<String>
}
