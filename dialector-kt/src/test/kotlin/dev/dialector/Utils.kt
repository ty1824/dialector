package dev.dialector

import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import kotlin.test.fail

open class TestNode : Node {
    override var parent: Node? = null
    override val properties: MutableMap<String, Any?> = mutableMapOf()
    override val children: MutableMap<String, List<Node>> = mutableMapOf()
    override val references: MutableMap<String, NodeReference<*>?> = mutableMapOf()
}

fun assertAll(message: String, vararg blocks: () -> Unit) {
    val messages = mutableListOf<Pair<Int, String>>()
    blocks.forEachIndexed { index, block ->
        try { block() } catch (e: Throwable) {
            messages += index to e.stackTraceToString()
        }
    }

    if (messages.isNotEmpty()) {
        val failureMessage = StringBuilder("Failure: $message")
        messages.forEach { (index, message) ->
            failureMessage.appendLine("\n  block[$index]:")
            message.lineSequence().forEach {
                failureMessage.appendLine("    $it")
            }
        }
        fail(failureMessage.toString())
    }
}
