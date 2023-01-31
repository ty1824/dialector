package dev.dialector.lsp

import dev.dialector.lsp.capabilities.*
import dev.dialector.semantic.type.Type
import dev.dialector.server.DocumentLocation
import dev.dialector.server.TextPosition
import dev.dialector.server.TextRange
import dev.dialector.syntax.Node
import dev.dialector.syntax.NodeReference
import org.eclipse.lsp4j.*
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
    private val textDocumentService: DialectorTextDocumentService,
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
            setTextDocumentSync(TextDocumentSyncOptions().apply {
                val opts = server.get(TextDocumentSync)
                openClose = opts.openClose != null
                change = opts.change?.textDocumentSyncKind
                setSave(opts.save?.saveOptions)
                willSave = opts.willSave != null
                willSaveWaitUntil = opts.willSaveWaitUntil != null
            })
            completionProvider = CompletionOptions()

            workspace = WorkspaceServerCapabilities().apply {
                val opts = server.get(Workspace)
                opts.workspaceFolders?.let {
                    workspaceFolders = WorkspaceFoldersOptions().apply {
                        supported = true
                        setChangeNotifications(it.changeNotifications?.changeNotification)
                    }
                }

                opts.workspaceSymbolProvider?.let {
                    setWorkspaceSymbolProvider(WorkspaceSymbolOptions(it.resolveProvider))
                }

                opts.executeCommandProvider?.let {
                    executeCommandProvider = ExecuteCommandOptions(it.commands)
                }
            }
        }
        println("CAPABILITIES: $capabilities")
        return capabilities
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}