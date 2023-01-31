package dev.dialector.lsp

import dev.dialector.lsp.capabilities.CompletionProvider
import dev.dialector.lsp.capabilities.TextDocumentSync
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either3
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class DialectorTextDocumentService(val server: DialectorServer) : TextDocumentService {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    
    val scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        scope.launch(dispatcher) {
            server.get(TextDocumentSync).openClose!!.didOpen(server, params)
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        scope.launch(dispatcher) {
            server.get(TextDocumentSync).change!!.didChange(server, params)
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        scope.launch(dispatcher) {
            server.get(TextDocumentSync).openClose!!.didClose(server, params)
        }
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
        scope.launch(dispatcher) {
            server.get(TextDocumentSync).save!!.didSave(server, params)
        }
    }

    override fun willSave(params: WillSaveTextDocumentParams) {
        scope.launch(dispatcher) {
            server.get(TextDocumentSync).willSave!!.willSave(server, params)
        }
    }

    override fun willSaveWaitUntil(params: WillSaveTextDocumentParams): CompletableFuture<MutableList<TextEdit>> {
        return scope.future(dispatcher) {
            server.get(TextDocumentSync).willSaveWaitUntil!!.willSaveWaitUntil(server, params).toMutableList()
        }
    }

    override fun completion(position: CompletionParams): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
        return scope.future(dispatcher) {
            val completion = server.get(CompletionProvider).completion(server, position)
            Either.forRight(completion)
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> {
        return scope.future(dispatcher) {
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
        return scope.future(dispatcher) {
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

    override fun prepareRename(params: PrepareRenameParams?): CompletableFuture<Either3<Range?, PrepareRenameResult?, PrepareRenameDefaultBehavior?>?>? {
        return super.prepareRename(params)
    }

    override fun prepareTypeHierarchy(params: TypeHierarchyPrepareParams?): CompletableFuture<List<TypeHierarchyItem?>?>? {
        throw UnsupportedOperationException()
    }

    override fun typeHierarchySupertypes(params: TypeHierarchySupertypesParams?): CompletableFuture<List<TypeHierarchyItem?>?>? {
        throw UnsupportedOperationException()
    }

    override fun typeHierarchySubtypes(params: TypeHierarchySubtypesParams): CompletableFuture<List<TypeHierarchyItem>> {
        throw UnsupportedOperationException()
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