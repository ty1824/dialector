package dev.dialector.glottony.server

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.services.WorkspaceService

class GlottonyWorkspace(val folders: List<String>) {
}

class GlottonyWorkspaceService : WorkspaceService {
    fun init(rootUri: String) {

    }
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {

    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {

    }
}