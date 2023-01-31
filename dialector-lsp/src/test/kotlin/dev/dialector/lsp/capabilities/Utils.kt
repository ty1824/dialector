package dev.dialector.lsp.capabilities

fun capabilities(vararg forCapabilities: Pair<LspCapabilityDescriptor<*>, LspCapability>): LspCapabilities =
    object : LspCapabilities {
        val capabilities: Map<LspCapabilityDescriptor<*>, LspCapability> = forCapabilities.toMap()
        override fun <T : LspCapability> get(capability: LspCapabilityDescriptor<T>): T = capabilities[capability] as T
    }