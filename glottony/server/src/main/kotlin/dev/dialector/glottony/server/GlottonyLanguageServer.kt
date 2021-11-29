package dev.dialector.glottony.server

import dev.dialector.glottony.GlottonyProgram
import dev.dialector.glottony.GlottonyRoot
import dev.dialector.glottony.diagnostics.ModelDiagnostic
import dev.dialector.glottony.parser.GlottonyParser
import dev.dialector.glottony.parser.SourceLocation
import dev.dialector.glottony.parser.SourceMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionOptions
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
import kotlin.coroutines.cancellation.CancellationException

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
            completionProvider = CompletionOptions()
        }
        println("CAPABILITIES: $capabilities")
        return capabilities
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}

fun ModelDiagnostic.toDiagnostic(sourceMap: SourceMap): Diagnostic? {
    val range = sourceMap.getRangeForNode(target)
    return if (range != null) {
        Diagnostic(Range(range.start.toPosition(), range.end.toPosition()), message)
    } else null
}

fun SourceLocation.toPosition(): Position = Position(line, character)

fun Position.toSourceLocation(): SourceLocation = SourceLocation(line, character)

class GlottonyDocumentService(val server: GlottonyLanguageServer) : TextDocumentService {
    object Scope : CoroutineScope {
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }

    val typesystemJobs: MutableMap<String, Job> = mutableMapOf()
    override fun didOpen(params: DidOpenTextDocumentParams) {
        server.client.showMessage(MessageParams(MessageType.Info, "Opened doc"))
        val uri = params.textDocument.uri
        println(uri)
        println(params.textDocument.text)
        if (typesystemJobs.contains(uri)) {
            typesystemJobs[uri]!!.cancel(CancellationException("Starting new job"))
            typesystemJobs.remove(uri)
        }
        typesystemJobs[uri] = Scope.launch(Dispatchers.Default) {
            val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.textDocument.text)
            val program = server.program
            program.addRoot(
                uri,
                GlottonyRoot(file, sourceMap)
            )
            val diagnostics = program.diagnostics.evaluate(program.getRoot(uri)!!)
            println(diagnostics)
            server.client.publishDiagnostics(PublishDiagnosticsParams(
                uri,
                diagnostics.map { it.toDiagnostic(sourceMap) }
            ))
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        println(uri)
        if (typesystemJobs.contains(uri)) {
            typesystemJobs[uri]!!.cancel(CancellationException("Starting new job"))
            typesystemJobs.remove(uri)
        }
        typesystemJobs[uri] = Scope.launch(Dispatchers.Default) {
            println("Parsing")
            val program = server.program
            val (file, sourceMap) = GlottonyParser.parseStringWithSourceMap(params.contentChanges[0].text)
            program.addRoot(
                uri,
                GlottonyRoot(file, sourceMap)
            )
            println("Starting diagnostics")
            val diagnostics = program.diagnostics.evaluate(program.getRoot(uri)!!)
            println(diagnostics)
            println("Publishing diagnostics")
            server.client.publishDiagnostics(PublishDiagnosticsParams(
                uri,
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

    // TODO: Map position into node tree to find the current reference.
    override fun completion(position: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> = CompletableFuture.supplyAsync {
        println(position.position)
        val v: Either<MutableList<CompletionItem>, CompletionList> = runBlocking {
            val program = server.program
            println(position.textDocument.uri)
            val root = program.getRoot(position.textDocument.uri)!!
            val scopeGraph = program.scopeGraph.resolveRoot(root)
            val context = root.sourceMap.getNodeAtLocation(position.position.toSourceLocation())
                ?.references
                ?.asSequence()
                ?.firstOrNull()
            println(context)
            if (context != null) {
                println()
                Either.forLeft(scopeGraph.getVisibleDeclarations(context.value).map {
                    print(it.second)
                    CompletionItem(it.second)
                }.toMutableList())
            } else { Either.forLeft(mutableListOf()) }
        }
        v
    }
//
//    override fun resolveCompletionItem(unresolved: CompletionItem?): CompletableFuture<CompletionItem> {
//        return super.resolveCompletionItem(unresolved)
//    }


}

class GlottonyFileHandle() {

}