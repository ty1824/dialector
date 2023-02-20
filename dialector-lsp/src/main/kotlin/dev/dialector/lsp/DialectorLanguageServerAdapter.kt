package dev.dialector.lsp

import dev.dialector.lsp.capabilities.LspCapability
import dev.dialector.lsp.capabilities.LspCapabilityDescriptor
import dev.dialector.lsp.capabilities.TextDocumentSync
import dev.dialector.semantic.type.Type
import dev.dialector.server.DocumentLocation
import dev.dialector.server.TextPosition
import dev.dialector.server.TextRange
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class DialectorWorkspace {
    fun onFilesChanged(): Nothing = TODO()
}

class DialectorWorkspaceService(private val server: DialectorServer) : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        TODO("Not yet implemented")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        TODO("Not yet implemented")
    }

    override fun executeCommand(params: ExecuteCommandParams?): CompletableFuture<Any> {
        return super.executeCommand(params)
    }

    override fun symbol(params: WorkspaceSymbolParams?): CompletableFuture<MutableList<out SymbolInformation>> {
        return super.symbol(params)
    }

    override fun didChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams?) {
        TODO("Should implement")
    }
}

fun Position.asTextPosition(): TextPosition = TextPosition(this.line, this.character)

fun TextPosition.asPosition(): Position = Position(this.line, this.column)

fun Range.asTextRange(): TextRange = TextRange(this.start.asTextPosition(), this.end.asTextPosition())

fun TextRange.asRange(): Range = Range(this.start.asPosition(), this.end.asPosition())

fun Location.asDocumentLocation(): DocumentLocation = DocumentLocation(this.uri, this.range.asTextRange())

fun DocumentLocation.asLocation(): Location = Location(this.uri, this.range.asRange())

interface DialectorService

abstract class DialectorServiceDescriptor<T : DialectorService>

interface SyntaxService : DialectorService {
    companion object : DialectorServiceDescriptor<SyntaxService>()

    /**
     * Retrieves the node at the given position.
     */
    fun getNodeAt(position: TextPosition): Node

    /**
     * Retrieves the  at the given position, if it exists
     */
    fun getReferenceAt(position: TextPosition): NodeReference<out Node>?

    fun getLocation(node: Node): DocumentLocation

    /**
     * Retrieves the text range containing the node's reference.
     */
    fun getLocationOfReference(reference: NodeReference<out Node>): DocumentLocation
}

interface Scope {
    val elements: Sequence<String>
}

interface ScopeService : DialectorService {
    companion object : DialectorServiceDescriptor<ScopeService>()

    fun getScopeForReference(reference: NodeReference<out Node>): Scope

    fun getTargetForReference(reference: NodeReference<out Node>): Node?
}

interface TypeService : DialectorService {
    companion object : DialectorServiceDescriptor<TypeService>()

    fun getTypeForNode(node: Node): Type?
}

interface DialectorServer {
    fun <T : DialectorService> get(type: DialectorServiceDescriptor<T>): T

    fun <T : LspCapability> get(capability: LspCapabilityDescriptor<T>): T
}

class DialectorLanguageServerAdapter(
    private val server: DialectorServer,
    private val workspaceService: DialectorWorkspaceService,
    private val textDocumentService: DialectorTextDocumentService
) : LanguageServer, LanguageClientAware {

    lateinit var client: LanguageClient
    var shutdownReceived: Boolean = false

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFutures.computeAsync {
            println(params)
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
            textDocumentSync = Either.forRight(
                TextDocumentSyncOptions().apply {
                    val opts = server.get(TextDocumentSync)
                    openClose = opts.openClose
                    change = opts.change
                    save = Either.forRight(opts.save?.saveOptions)
                    willSave = opts.willSave != null
                    willSaveWaitUntil = opts.willSaveWaitUntil != null
                }
            )
            completionProvider = CompletionOptions()
        }
        println("CAPABILITIES: $capabilities")
        return capabilities
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}
