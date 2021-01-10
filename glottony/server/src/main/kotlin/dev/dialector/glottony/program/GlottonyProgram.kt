package dev.dialector.glottony

import dev.dialector.glottony.ast.File
import dev.dialector.glottony.diagnostics.GlottonyDiagnosticProvider
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.model.Node
import org.eclipse.lsp4j.Position

class GlottonyRoot(var rootNode: File, var sourceMap: Map<Node, GlottonySourceMap>)

/**
 * Represents the source object a node was derived from.
 *
 * @param L The type of the location object this SourceMap represents.
 */
interface SourceMap<L> {
    val start: L
    val end: L
}

data class GlottonySourceMap(override val start: Position, override val end: Position) : SourceMap<Position>

class GlottonyProgram {
    private val roots: MutableMap<String, GlottonyRoot> = mutableMapOf()
    val typesystem: GlottonyTypesystem = GlottonyTypesystem()
    val diagnostics: GlottonyDiagnosticProvider = GlottonyDiagnosticProvider(typesystem)

    fun addRoot(uri: String, root: GlottonyRoot) {
        roots[uri] = root
    }

    fun getRoot(uri: String): GlottonyRoot? = roots[uri]
}