package dev.dialector.lsp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.services.WorkspaceService

class DialectorWorkspace(val folders: List<String>) {
}

class DialectorWorkspaceService : WorkspaceService {
    fun init(rootUri: String) {

    }
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {

    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {

    }
}