import kotlin.test.fail

fun assertAll(message: String, vararg blocks: () -> Unit) {
    val messages = mutableListOf<Pair<Int, String>>()
    blocks.forEachIndexed { index, block ->
        try { block() } catch (e: Throwable) {
            messages += index to e.stackTraceToString()
        }
    }

    if (messages.isNotEmpty()) {
        val failureMessage = StringBuilder("Failure: $message")
        messages.forEach { (index, message) ->
            failureMessage.appendLine("\n  block[$index]:")
            message.lineSequence().forEach {
                failureMessage.appendLine("    $it")
            }
        }
        fail(failureMessage.toString())
    }
}
