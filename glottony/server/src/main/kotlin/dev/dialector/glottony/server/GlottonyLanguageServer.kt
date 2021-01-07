package dev.dialector.glottony.server

import dev.dialector.glottony.diagnostics.GlottonyDiagnosticProvider
import dev.dialector.glottony.diagnostics.ModelDiagnostic
import dev.dialector.glottony.parser.GlottonyParser
import dev.dialector.glottony.typesystem.GlottonyTypesystemContext
import dev.dialector.model.Node
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SaveOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class GlottonyLanguageServer : LanguageServer, LanguageClientAware {
    val workspaceService: GlottonyWorkspaceService = GlottonyWorkspaceService()
    val textDocumentService: GlottonyDocumentService = GlottonyDocumentService(this)
    lateinit var client: LanguageClient

    var shutdownReceived: Boolean = false

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFutures.computeAsync { _ ->
            println(params)
            workspaceService.init(params.rootUri)
            InitializeResult(getCapabilities())
        }
    }

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture("")

    override fun exit() {
        println("exiting")
    }

    override fun getTextDocumentService(): TextDocumentService {
        println("getting text doc service")
        return textDocumentService
    }


    override fun getWorkspaceService(): WorkspaceService = workspaceService

    fun getCapabilities(): ServerCapabilities {
        val capabilities = ServerCapabilities().apply {
            textDocumentSync = Either.forRight(TextDocumentSyncOptions().apply {
                openClose = true
                save = SaveOptions(true)
                // TODO: For now, move to Incremental when supported
                change = TextDocumentSyncKind.Full
            })
        }
        println("CAPABILITIES: $capabilities")
        return capabilities
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}

fun ModelDiagnostic.toDiagnostic(sourceMap: Map<Node, ParserRuleContext>): Diagnostic? {
    val rule = sourceMap[this.target]
    return if (rule != null) {
        Diagnostic(Range(rule.start.toPosition(), rule.stop.toPosition()), this.message)
    } else null
}

fun Token.toPosition(): Position = Position(this.line-1, this.charPositionInLine)

class GlottonyDocumentService(val server: GlottonyLanguageServer) : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams) {
        server.client.showMessage(MessageParams(MessageType.Info, "Opened doc"))
        println(params.textDocument.uri)
        println(params.textDocument.text)
        val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.textDocument.text)

        val diagnostics = GlottonyDiagnosticProvider(GlottonyTypesystemContext()).evaluate(file)
        println(diagnostics)
        server.client.publishDiagnostics(PublishDiagnosticsParams(
            params.textDocument.uri,
            diagnostics.map { it.toDiagnostic(sourceMap) }
        ))
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        println(params.textDocument.uri)
        val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.contentChanges[0].text)

        val diagnostics = GlottonyDiagnosticProvider(GlottonyTypesystemContext()).evaluate(file)
        println(diagnostics)
        server.client.publishDiagnostics(PublishDiagnosticsParams(
            params.textDocument.uri,
            diagnostics.map { it.toDiagnostic(sourceMap) }
        ))
    }

    override fun didClose(params: DidCloseTextDocumentParams) {

    }

    override fun didSave(params: DidSaveTextDocumentParams) {

    }
}

class GlottonyFileHandle() {

}