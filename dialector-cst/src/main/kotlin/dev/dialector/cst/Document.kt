package dev.dialector.cst

/**
 * An abstraction over a text document allowing an external provider to feed document text and
 * line offsets to a [ConcreteSyntaxTree].
 */
interface Document {
    val text: String
    val lineOffsets: IntArray

    companion object {
        /**
         * An empty document with zero length and a single line starting at offset 0.
         */
        val EMPTY: Document = StringDocument("", intArrayOf(0))

        /**
         * Creates a [Document] from the given [text], lazily computing line offsets.
         */
        fun of(text: String): Document = StringDocument(text)

        /**
         * Computes the 0-based character start offsets for each line in [text].
         * Supports `\n`, `\r\n`, and `\r` line separators.
         */
        fun computeLineOffsets(text: String): IntArray {
            var count = 1
            var i = 0
            val n = text.length
            while (i < n) {
                val c = text[i]
                if (c == '\r') {
                    count++
                    if (i + 1 < n && text[i + 1] == '\n') i++
                } else if (c == '\n') {
                    count++
                }
                i++
            }
            val starts = IntArray(count)
            starts[0] = 0
            var lineIdx = 1
            i = 0
            while (i < n) {
                val c = text[i]
                if (c == '\r') {
                    if (i + 1 < n && text[i + 1] == '\n') i++
                    starts[lineIdx++] = i + 1
                } else if (c == '\n') {
                    starts[lineIdx++] = i + 1
                }
                i++
            }
            return starts
        }
    }
}

/**
 * Default [Document] implementation backed by an in-memory [String] and optional precomputed [lineOffsets].
 * When [lineOffsets] is not provided, it is computed lazily on first access.
 */
class StringDocument(
    override val text: String,
    lineOffsets: IntArray? = null,
) : Document {
    override val lineOffsets: IntArray by lazy(LazyThreadSafetyMode.PUBLICATION) {
        lineOffsets ?: Document.computeLineOffsets(text)
    }

    override fun toString(): String = "Document(length=${text.length}, lines=${lineOffsets.size})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false
        return text == other.text && lineOffsets.contentEquals(other.lineOffsets)
    }

    override fun hashCode(): Int = 31 * text.hashCode() + lineOffsets.contentHashCode()
}

/**
 * Creates a [Document] from the given [text].
 */
fun Document(text: String): Document = StringDocument(text)

/**
 * Creates a [Document] from the given [text] and precomputed [lineOffsets].
 */
fun Document(text: String, lineOffsets: IntArray): Document = StringDocument(text, lineOffsets)
