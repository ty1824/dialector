import kotlin.test.fail

fun assertAll(message: String, vararg blocks: () -> Unit) {
    val messages = mutableListOf<String>()
    blocks.forEach {
        try { it }
        catch (e: Throwable) {
            if (e.message != null) {
                val message = e.message
                if (message != null) messages.add(message)
            }
        }
    }

    if (messages.isNotEmpty()) {
        var failureMessage = "Failed: $message"
        messages.forEach {
            failureMessage += "\n$it"
        }
        fail(failureMessage)
    }
}