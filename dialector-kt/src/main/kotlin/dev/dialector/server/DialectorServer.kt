package dev.dialector.server

import dev.dialector.syntax.Node
import dev.dialector.syntax.SyntacticModel
import java.nio.file.Path

class DialectorServer {
}

/**
 * Responsible for keeping the program model up-to-date based on client changes along with propagating diagnostics and
 * server-side change requests to the client.
 *
 * Will propagate events:
 * - Node add/change/remove
 */
interface ModelService {

}

class LocalTextModelService : ModelService {
    val model : SyntacticModel = object : SyntacticModel {
        override fun getRoots(): Sequence<Node> {
            TODO("Not yet implemented")
        }

        override fun getRoot(id: String): Node {
            TODO("Not yet implemented")
        }

    }

    fun onChange(path: Path) {

    }
}

/**
 * Responsible for computing and maintaining semantic information about the program as the model changes.
 */
interface SemanticService {

}

/**
 * Responsible for computing and maintaining diagnostic feedback that can be relayed to the client to provide feedback on the model and
 * correctness.
 */
interface DiagnosticService {

}