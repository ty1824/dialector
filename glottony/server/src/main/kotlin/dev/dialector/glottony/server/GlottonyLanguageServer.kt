package dev.dialector.glottony.server

import dev.dialector.glottony.GlottonyProgram
import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.GlottonySourceMap
import dev.dialector.glottony.diagnostics.ModelDiagnostic
import dev.dialector.glottony.parser.GlottonyParser
import dev.dialector.model.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class GlottonyLanguageServer : LanguageServer, LanguageClientAware {
    val workspaceService: GlottonyWorkspaceService = GlottonyWorkspaceService()
    val textDocumentService: GlottonyDocumentService = GlottonyDocumentService(this)
    lateinit var client: LanguageClient
    val program: GlottonyProgram = GlottonyProgram()

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
    object Scope : CoroutineScope {
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        server.client.showMessage(MessageParams(MessageType.Info, "Opened doc"))
        println(params.textDocument.uri)
        println(params.textDocument.text)
        Scope.launch(Dispatchers.Default) {
            val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.textDocument.text)
            val program = server.program
            program.addRoot(
                params.textDocument.uri,
                GlottonyRoot(file, sourceMap.mapValues {
                    GlottonySourceMap(it.value.start.toPosition(), it.value.stop.toPosition())
                })
            )
            val diagnostics = program.diagnostics.evaluate(program.getRoot(params.textDocument.uri)!!)
            println(diagnostics)
            server.client.publishDiagnostics(PublishDiagnosticsParams(
                params.textDocument.uri,
                diagnostics.map { it.toDiagnostic(sourceMap) }
            ))
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        println(params.textDocument.uri)
        Scope.launch(Dispatchers.Default) {
            println("Parsing")
            val program = server.program
            val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.contentChanges[0].text)
            program.addRoot(
                params.textDocument.uri,
                GlottonyRoot(file, sourceMap.mapValues {
                    GlottonySourceMap(it.value.start.toPosition(), it.value.stop.toPosition())
                })
            )
            println("Starting diagnostics")
            val diagnostics = program.diagnostics.evaluate(program.getRoot(params.textDocument.uri)!!)
            println(diagnostics)
            println("Publishing diagnostics")
            server.client.publishDiagnostics(PublishDiagnosticsParams(
                params.textDocument.uri,
                diagnostics.map { it.toDiagnostic(sourceMap) }
            ))
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO("Not yet implemented")
    }

//    override fun completion(position: CompletionParams?): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
//        return super.completion(position)
//    }
//
//    override fun resolveCompletionItem(unresolved: CompletionItem?): CompletableFuture<CompletionItem> {
//        return super.resolveCompletionItem(unresolved)
//    }


}

class GlottonyFileHandle() {

}