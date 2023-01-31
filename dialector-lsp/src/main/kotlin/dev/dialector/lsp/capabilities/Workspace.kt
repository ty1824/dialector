package dev.dialector.lsp.capabilities

import dev.dialector.lsp.DialectorServer
import org.eclipse.lsp4j.*
import java.util.concurrent.CompletableFuture

interface Workspace : LspCapability {
    companion object : LspCapabilityDescriptor<Workspace>()

    fun didChangeConfiguration(server: DialectorServer, params: DidChangeConfigurationParams)

    fun didChangeWatchedFiles(server: DialectorServer, params: DidChangeWatchedFilesParams)

    val workspaceSymbolProvider: WorkspaceSymbolCapability?

    /**
     * The server has support for workspace folders
     */
    val workspaceFolders: WorkspaceFoldersCapability?

    val executeCommandProvider: ExecuteCommandCapability?

    interface WorkspaceSymbolCapability {
        /**
         * The server provides support to resolve additional
         * information for a workspace symbol.
         *
         * // TODO Merge with resolveWorkspaceSymbol?
         * @since 3.17.0
         */
        val resolveProvider: Boolean?

        fun symbol(server: DialectorServer, params: WorkspaceSymbolParams): List<WorkspaceSymbol>

        fun resolveWorkspaceSymbol(server: DialectorServer, workspaceSymbol: WorkspaceSymbol): WorkspaceSymbol?
    }

    interface WorkspaceFoldersCapability {
        /**
         * Whether the server wants to receive workspace folder
         * change notifications.
         *
         * If a string is provided, the string is treated as an ID
         * under which the notification is registered on the client
         * side. The ID can be used to unregister for these events
         * using the `client/unregisterCapability` request.
         */
        val changeNotifications: WorkspaceFolderChangeCapability?

        interface WorkspaceFolderChangeCapability {
            val changeNotification: String

            fun didChangeWorkspaceFolders(server: DialectorServer, params: DidChangeWorkspaceFoldersParams)
        }
    }

    interface ExecuteCommandCapability {
        /**
         * The commands to be executed on the server
         */
        val commands: List<String>

        fun executeCommand(params: ExecuteCommandParams?): CompletableFuture<Any>
    }

}