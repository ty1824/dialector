package dev.dialector.lsp

import dev.dialector.lsp.capabilities.Workspace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DialectorWorkspaceService(private val server: DialectorServer) : WorkspaceService {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default

    val scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
        scope.launch(dispatcher) {
            server.get(Workspace).didChangeConfiguration(server, params)
        }
    }
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        scope.launch(dispatcher) {
            server.get(Workspace).didChangeWatchedFiles(server, params)
        }
    }

    override fun symbol(params: WorkspaceSymbolParams): CompletableFuture<Either<List<SymbolInformation>, List<WorkspaceSymbol>>> =
        scope.future(dispatcher) {
            Either.forRight(server.get(Workspace).workspaceSymbolProvider!!.symbol(server, params))
        }

    override fun resolveWorkspaceSymbol(workspaceSymbol: WorkspaceSymbol): CompletableFuture<WorkspaceSymbol?> =
        scope.future(dispatcher) {
            server.get(Workspace).workspaceSymbolProvider!!.resolveWorkspaceSymbol(server, workspaceSymbol)
        }

    override fun didChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams) {
        scope.launch(dispatcher) {
            server.get(Workspace).workspaceFolders!!.changeNotifications!!.didChangeWorkspaceFolders(server, params)
        }
    }

    override fun willCreateFiles(params: CreateFilesParams?): CompletableFuture<WorkspaceEdit?>? {
        throw UnsupportedOperationException()
    }

    override fun didCreateFiles(params: CreateFilesParams?) {
        throw UnsupportedOperationException()
    }

    override fun willRenameFiles(params: RenameFilesParams?): CompletableFuture<WorkspaceEdit?>? {
        throw UnsupportedOperationException()
    }

    override fun didRenameFiles(params: RenameFilesParams?) {
        throw UnsupportedOperationException()
    }

    override fun willDeleteFiles(params: DeleteFilesParams?): CompletableFuture<WorkspaceEdit?>? {
        throw UnsupportedOperationException()
    }

    override fun didDeleteFiles(params: DeleteFilesParams?) {
        throw UnsupportedOperationException()
    }

    override fun executeCommand(params: ExecuteCommandParams): CompletableFuture<Any> =
        scope.future(dispatcher) {
            server.get(Workspace).executeCommandProvider!!.executeCommand(params)
        }

    override fun diagnostic(params: WorkspaceDiagnosticParams?): CompletableFuture<WorkspaceDiagnosticReport?>? {
        throw UnsupportedOperationException()
    }
}