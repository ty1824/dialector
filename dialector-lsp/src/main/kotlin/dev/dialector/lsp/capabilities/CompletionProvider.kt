package dev.dialector.lsp.capabilities

import dev.dialector.lsp.DialectorServer
import dev.dialector.lsp.ScopeService
import dev.dialector.lsp.SyntaxService
import dev.dialector.lsp.asTextPosition
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams

interface CompletionProvider : LspCapability {
    companion object : LspCapabilityDescriptor<CompletionProvider>()

    /**
     * Most tools trigger completion request automatically without explicitly
     * requesting it using a keyboard shortcut (e.g. Ctrl+Space). Typically they
     * do so when the user starts to type an identifier. For example if the user
     * types `c` in a JavaScript file code complete will automatically pop up
     * present `console` besides others as a completion item. Characters that
     * make up identifiers don't need to be listed here.
     *
     * If code complete should automatically be trigger on characters not being
     * valid inside an identifier (for example `.` in JavaScript) list them in
     * `triggerCharacters`.
     */
    val triggerCharacters: List<String>?

    /**
     * The list of all possible characters that commit a completion. This field
     * can be used if clients don't support individual commit characters per
     * completion item. See client capability
     * `completion.completionItem.commitCharactersSupport`.
     *
     * If a server provides both `allCommitCharacters` and commit characters on
     * an individual completion item the ones on the completion item win.
     *
     * @since 3.2.0
     */
    val allCommitCharacters: List<String>?

    interface ResolveProvider {
        fun resolveCompletionItem(server: DialectorServer, unresolved: CompletionItem): CompletionItem
    }

    /**
     * The server provides support to resolve additional
     * information for a completion item.
     */
    val resolveProvider: ResolveProvider?

    fun completion(server: DialectorServer, params: CompletionParams /* TODO: make this specific to Dialector */): CompletionList
}

abstract class DefaultCompletionProvider : CompletionProvider {
    override fun completion(server: DialectorServer, params: CompletionParams): CompletionList {
        val reference = server.get(SyntaxService).getReferenceAt(params.position.asTextPosition())
        return if (reference != null) {
            val scope = server.get(ScopeService).getScopeForReference(reference)
            val items = scope.elements.map { CompletionItem(it) }.toMutableList()
            CompletionList(false, items)
        } else {
            CompletionList(listOf())
        }
    }
}
