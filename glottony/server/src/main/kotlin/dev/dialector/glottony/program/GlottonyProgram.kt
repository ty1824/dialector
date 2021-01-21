package dev.dialector.glottony

import dev.dialector.glottony.ast.File
import dev.dialector.glottony.diagnostics.GlottonyDiagnosticProvider
import dev.dialector.glottony.parser.SourceLocation
import dev.dialector.glottony.parser.SourceMap
import dev.dialector.glottony.parser.SourceRange
import dev.dialector.glottony.parser.contains
import dev.dialector.glottony.scopes.GlottonyScopeGraph
import dev.dialector.glottony.typesystem.GlottonyTypesystem
import dev.dialector.model.Node

class GlottonyRoot(
    var rootNode: File,
    var sourceMap: SourceMap
)

class GlottonyProgram {
    private val roots: MutableMap<String, GlottonyRoot> = mutableMapOf()
    val typesystem: GlottonyTypesystem = GlottonyTypesystem()
    val scopeGraph: GlottonyScopeGraph = GlottonyScopeGraph()
    val diagnostics: GlottonyDiagnosticProvider = GlottonyDiagnosticProvider(typesystem)

    fun addRoot(uri: String, root: GlottonyRoot) {
        roots[uri] = root
    }

    fun getRoot(uri: String): GlottonyRoot? = roots[uri]
}