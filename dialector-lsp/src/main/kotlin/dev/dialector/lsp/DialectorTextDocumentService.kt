package dev.dialector.lsp

import dev.dialector.lsp.capabilities.CompletionProvider
import dev.dialector.lsp.capabilities.TextDocumentSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.CallHierarchyIncomingCall
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyItem
import org.eclipse.lsp4j.CallHierarchyOutgoingCall
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.ColorInformation
import org.eclipse.lsp4j.ColorPresentation
import org.eclipse.lsp4j.ColorPresentationParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentColorParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentLink
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.LinkedEditingRangeParams
import org.eclipse.lsp4j.LinkedEditingRanges
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.Moniker
import org.eclipse.lsp4j.MonikerParams
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.PrepareRenameResult
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.ResolveTypeHierarchyItemParams
import org.eclipse.lsp4j.SelectionRange
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensDelta
import org.eclipse.lsp4j.SemanticTokensDeltaParams
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.SemanticTokensRangeParams
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.TypeHierarchyItem
import org.eclipse.lsp4j.TypeHierarchyParams
import org.eclipse.lsp4j.WillSaveTextDocumentParams
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DialectorTextDocumentService(val server: DialectorServer) : TextDocumentService {
    val Scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        Scope.launch(Dispatchers.Default) {
            server.get(TextDocumentSync).didOpen(server, params)
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        Scope.launch(Dispatchers.Default) {
            server.get(TextDocumentSync).didChange(server, params)
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        Scope.launch(Dispatchers.Default) {
            server.get(TextDocumentSync).didClose(server, params)
        }
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        Scope.launch(Dispatchers.Default) {
            server.get(TextDocumentSync).save!!.didSave(server, params)
        }
    }

    override fun willSave(params: WillSaveTextDocumentParams) {
        Scope.launch(Dispatchers.Default) {
            server.get(TextDocumentSync).willSave!!.willSave(server, params)
        }
    }

    override fun willSaveWaitUntil(params: WillSaveTextDocumentParams): CompletableFuture<MutableList<TextEdit>> {
        return Scope.future(Dispatchers.Default) {
            server.get(TextDocumentSync).willSaveWaitUntil!!.willSaveWaitUntil(server, params).toMutableList()
        }
    }

    override fun completion(position: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        return Scope.future(Dispatchers.Default) {
            val completion = server.get(CompletionProvider).completion(server, position)
            Either.forRight(completion)
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> {
        return Scope.future(Dispatchers.Default) {
            server.get(CompletionProvider).resolveProvider!!.resolveCompletionItem(server, unresolved)
        }
    }

    override fun hover(params: HoverParams?): CompletableFuture<Hover> {
        TODO("How should hover be handled?")
    }

    override fun signatureHelp(params: SignatureHelpParams?): CompletableFuture<SignatureHelp> {
        TODO("Need to provide signature provider abstraction")
    }

    override fun declaration(params: DeclarationParams): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        return Scope.future(Dispatchers.Default) {
            val reference = server.get(SyntaxService).getReferenceAt(params.position.asTextPosition())
            if (reference != null) {
                val target = server.get(ScopeService).getTargetForReference(reference)
                if (target != null) {
                    val targetLocation = server.get(SyntaxService).getLocation(target)
                    Either.forLeft(mutableListOf(targetLocation.asLocation()))
                } else {
                    Either.forLeft(mutableListOf())
                }
            } else {
                Either.forLeft(mutableListOf())
            }
        }
    }

    override fun definition(params: DefinitionParams?): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        TODO("Represent distinction between Declaration and Definition?")
    }

    override fun typeDefinition(params: TypeDefinitionParams?): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        return super.typeDefinition(params)
    }

    override fun implementation(params: ImplementationParams?): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> {
        TODO("Abstraction for implementations")
    }

    override fun references(params: ReferenceParams?): CompletableFuture<MutableList<out Location>> {
        TODO("Index references to a particular node")
    }

    override fun documentHighlight(params: DocumentHighlightParams?): CompletableFuture<MutableList<out DocumentHighlight>> {
        return super.documentHighlight(params)
    }

    override fun documentSymbol(params: DocumentSymbolParams?): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
        return super.documentSymbol(params)
    }

    override fun codeAction(params: CodeActionParams?): CompletableFuture<MutableList<Either<Command, CodeAction>>> {
        return super.codeAction(params)
    }

    override fun resolveCodeAction(unresolved: CodeAction?): CompletableFuture<CodeAction> {
        return super.resolveCodeAction(unresolved)
    }

    override fun codeLens(params: CodeLensParams?): CompletableFuture<MutableList<out CodeLens>> {
        return super.codeLens(params)
    }

    override fun resolveCodeLens(unresolved: CodeLens?): CompletableFuture<CodeLens> {
        return super.resolveCodeLens(unresolved)
    }

    override fun formatting(params: DocumentFormattingParams?): CompletableFuture<MutableList<out TextEdit>> {
        TODO("Document formatting capability")
    }

    override fun rangeFormatting(params: DocumentRangeFormattingParams?): CompletableFuture<MutableList<out TextEdit>> {
        TODO("Document formatting capability")
    }

    override fun onTypeFormatting(params: DocumentOnTypeFormattingParams?): CompletableFuture<MutableList<out TextEdit>> {
        TODO("Document formatting capability")
    }

    override fun rename(params: RenameParams?): CompletableFuture<WorkspaceEdit> {
        return super.rename(params)
    }

    override fun linkedEditingRange(params: LinkedEditingRangeParams?): CompletableFuture<LinkedEditingRanges> {
        return super.linkedEditingRange(params)
    }

    override fun documentLink(params: DocumentLinkParams?): CompletableFuture<MutableList<DocumentLink>> {
        return super.documentLink(params)
    }

    override fun documentLinkResolve(params: DocumentLink?): CompletableFuture<DocumentLink> {
        return super.documentLinkResolve(params)
    }

    override fun documentColor(params: DocumentColorParams?): CompletableFuture<MutableList<ColorInformation>> {
        return super.documentColor(params)
    }

    override fun colorPresentation(params: ColorPresentationParams?): CompletableFuture<MutableList<ColorPresentation>> {
        return super.colorPresentation(params)
    }

    override fun foldingRange(params: FoldingRangeRequestParams?): CompletableFuture<MutableList<FoldingRange>> {
        return super.foldingRange(params)
    }

    override fun prepareRename(params: PrepareRenameParams?): CompletableFuture<Either<Range, PrepareRenameResult>> {
        return super.prepareRename(params)
    }

    override fun typeHierarchy(params: TypeHierarchyParams?): CompletableFuture<TypeHierarchyItem> {
        return super.typeHierarchy(params)
    }

    override fun resolveTypeHierarchy(params: ResolveTypeHierarchyItemParams?): CompletableFuture<TypeHierarchyItem> {
        return super.resolveTypeHierarchy(params)
    }

    override fun prepareCallHierarchy(params: CallHierarchyPrepareParams?): CompletableFuture<MutableList<CallHierarchyItem>> {
        return super.prepareCallHierarchy(params)
    }

    override fun callHierarchyIncomingCalls(params: CallHierarchyIncomingCallsParams?): CompletableFuture<MutableList<CallHierarchyIncomingCall>> {
        return super.callHierarchyIncomingCalls(params)
    }

    override fun callHierarchyOutgoingCalls(params: CallHierarchyOutgoingCallsParams?): CompletableFuture<MutableList<CallHierarchyOutgoingCall>> {
        return super.callHierarchyOutgoingCalls(params)
    }

    override fun selectionRange(params: SelectionRangeParams?): CompletableFuture<MutableList<SelectionRange>> {
        return super.selectionRange(params)
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> {
        return super.semanticTokensFull(params)
    }

    override fun semanticTokensFullDelta(params: SemanticTokensDeltaParams?): CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> {
        return super.semanticTokensFullDelta(params)
    }

    override fun semanticTokensRange(params: SemanticTokensRangeParams?): CompletableFuture<SemanticTokens> {
        return super.semanticTokensRange(params)
    }

    override fun moniker(params: MonikerParams?): CompletableFuture<MutableList<Moniker>> {
        return super.moniker(params)
    }
}
