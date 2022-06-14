package dev.dialector.lsp.capabilities

import dev.dialector.lsp.DialectorServer
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.SaveOptions
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WillSaveTextDocumentParams
import org.eclipse.lsp4j.jsonrpc.messages.Either


interface TextDocumentSync : LspCapability {
    companion object : LspCapabilityDescriptor<TextDocumentSync>()

    /**
     * Open and close notifications are sent to the server. If omitted open
     * close notification should not be sent.
     */
    val openClose: Boolean?

    /**
     * Change notifications are sent to the server. See
     * TextDocumentSyncKind.None, TextDocumentSyncKind.Full and
     * TextDocumentSyncKind.Incremental. If omitted it defaults to
     * TextDocumentSyncKind.None.
     */
    val change: TextDocumentSyncKind?

    fun didOpen(server: DialectorServer, params: DidOpenTextDocumentParams)

    fun didChange(server: DialectorServer, params: DidChangeTextDocumentParams)

    fun didClose(server: DialectorServer, params: DidCloseTextDocumentParams)

    interface SaveCapability {
        val saveOptions: SaveOptions

        fun didSave(server: DialectorServer, params: DidSaveTextDocumentParams)
    }

    /**
     * The capability indicates that the server is interested in textDocument/didSave notifications.
     */
    val save: SaveCapability?

    interface WillSaveCapability {
        fun willSave(server: DialectorServer, params: WillSaveTextDocumentParams)
    }
    /**
     * The capability indicates that the server is interested in textDocument/willSave notifications.
     */
    val willSave: WillSaveCapability?

    interface WillSaveWaitUntilCapability {
        fun willSaveWaitUntil(server: DialectorServer, params: WillSaveTextDocumentParams): List<TextEdit>
    }

    /**
     * The capability indicates that the server is interested in textDocument/willSaveWaitUntil requests.
     */
    val willSaveWaitUntil: WillSaveWaitUntilCapability?
}

open class DefaultTextDocumentSync(
    override val save: TextDocumentSync.SaveCapability? = null,
    override val willSave: TextDocumentSync.WillSaveCapability? = null,
    override val willSaveWaitUntil: TextDocumentSync.WillSaveWaitUntilCapability? = null
) : TextDocumentSync {
    override val openClose = true
    override val change = TextDocumentSyncKind.Full

    override fun didOpen(server: DialectorServer, params: DidOpenTextDocumentParams) {
        TODO("Not yet implemented")
    }

    override fun didChange(server: DialectorServer, params: DidChangeTextDocumentParams) {
        TODO("Not yet implemented")
    }

    override fun didClose(server: DialectorServer, params: DidCloseTextDocumentParams) {
        TODO("Not yet implemented")
    }
}