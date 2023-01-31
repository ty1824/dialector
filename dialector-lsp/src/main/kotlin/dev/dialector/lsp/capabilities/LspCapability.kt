package dev.dialector.lsp.capabilities

import org.eclipse.lsp4j.*

sealed interface LspCapability

sealed class LspCapabilityDescriptor<T : LspCapability>()

interface LspCapabilities {
    fun <T : LspCapability> get(capability: LspCapabilityDescriptor<T>): T
}

fun generateServerCapabilities(capabilities: LspCapabilities): ServerCapabilities =
    ServerCapabilities().apply {
        setTextDocumentSync(TextDocumentSyncOptions().apply {
            val opts = capabilities.get(TextDocumentSync)
            openClose = opts.openClose != null
            change = opts.change?.textDocumentSyncKind
            setSave(opts.save?.saveOptions)
            willSave = opts.willSave != null
            willSaveWaitUntil = opts.willSaveWaitUntil != null
        })
        completionProvider = CompletionOptions()

        workspace = WorkspaceServerCapabilities().apply {
            val opts = capabilities.get(Workspace)
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
