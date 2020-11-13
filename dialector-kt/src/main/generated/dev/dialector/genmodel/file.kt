package dev.dialector.genmodel

import dev.dialector.model.Node
import dev.dialector.model.sample.MClass
import dev.dialector.model.sample.MField
import dev.dialector.model.sample.MFunction
import kotlin.String
import kotlin.collections.List

class MClassImpl : MClass(), Node {
  var name: String

  val fields: List<MField>

  val functions: List<MFunction>

  companion object {
    fun MClass() {
    }
  }
}

class MFieldImpl : MField(), Node {
  var name: String

  var type: String

  companion object {
    fun MField() {
    }
  }
}
