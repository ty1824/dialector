package dev.dialector.lsp.capabilities

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue

class TestCapabilities {

    @Test
    fun testTextDocumentSyncCapabilities() {
        val textDocumentSync = mockk<TextDocumentSync>(relaxed = true)
        val workspace = mockk<Workspace>(relaxed = true)

        val openCloseCapability = mockk<TextDocumentSync.OpenCloseCapability>()

        every { textDocumentSync.openClose } returns openCloseCapability
//        every { textDocumentSync.change } returns null
//        every { textDocumentSync.save } returns null
//        every { textDocumentSync.willSave } returns null
//        every { textDocumentSync.willSaveWaitUntil } returns null

        val capabilities = capabilities(TextDocumentSync to textDocumentSync, Workspace to workspace)

        val serverCapabilities = generateServerCapabilities(capabilities)

        assertTrue(serverCapabilities.textDocumentSync.isRight)
        assertTrue(serverCapabilities.textDocumentSync.right.openClose, "Open/Close support should be enabled")
    }
}