package dev.dialector.server

import dev.dialector.syntax.DynamicSyntacticModel
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import dev.dialector.syntax.RootId
import dev.dialector.syntax.SyntacticModel
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * Several workflows to consider:
 *
 * Workspace-level analysis
 *  - Done upon language server spin-up
 *  - Done whenever project files change
 *  - Need to be granular in some ways but don't need a real-time model?
 *
 * File-level analysis
 *  - Done upon opening/changing a single file
 *  - Needs to be very granular + diagnostics and needs to be able to respond in real-time
 */
class DialectorServer {
}

/**
 * A workspace is responsible for monitoring files & responding to changes
 *
 * Upon construction and file change, the workspace should recompute its syntactic and semantic models
 */
interface DialectorWorkspaceModel {

}

/**
 * A position in a document
 */
data class TextPosition(val line: Int, val column: Int)

/**
 * A range of text within a document
 */
data class TextRange(val start: TextPosition, val end: TextPosition)

/**
 * A location in a particular document identified by URI
 */
data class DocumentLocation(val uri: String, val range: TextRange)

/**
 * Responsible for keeping the program model up-to-date based on client changes along with propagating diagnostics and
 * server-side change requests to the client.
 *
 * Will propagate events:
 * - Node add/change/remove
 */
interface ModelService {

}

interface ParseIndex {
    val root: Node
    val sourceMap: Map<Node, TextRange>
    val referenceMap: Map<NodeReference<*>, TextRange>
}

interface Parser {
    fun parseRoot(content: String): ParseIndex
}

interface Event

data class RootUpdated(val id: RootId) : Event

data class RootRemoved(val id: RootId) : Event

class LocalTextModelService(
    val eventBus: EventBus,
    val pathToId: (Path) -> RootId,
    val parser: Parser,
) : ModelService {

    val model : DynamicSyntacticModel = object : DynamicSyntacticModel {
        private val roots: MutableMap<RootId, Node> = mutableMapOf()

        override fun putRoot(id: RootId, root: Node) {
            roots[id] = root
            eventBus.publish(RootUpdated(id))
        }

        override fun removeRoot(id: RootId) {
            roots.remove(id)
            eventBus.publish(RootRemoved(id))
        }

        override fun getRoots(): Sequence<Node> {
            return roots.values.asSequence()
        }

        override fun getRoot(id: RootId): Node? {
            return roots[id]
        }
    }

    fun onChange(path: Path, content: String) {
        val id = pathToId(path)
        val parseIndex = parser.parseRoot(content)
        model.putRoot(id, parseIndex.root)
    }

    fun onDelete(path: Path) {
        val id = pathToId(path)
        model.removeRoot(id)
    }
}

/**
 * Responsible for computing and maintaining semantic information about the program as the model changes.
 */
interface SemanticService {

}

/**
 * Responsible for computing and maintaining diagnostic feedback that can be relayed to the client to provide feedback
 * on syntactic and semantic correctness.
 */
interface DiagnosticService {

}