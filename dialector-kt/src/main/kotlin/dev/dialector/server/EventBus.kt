package dev.dialector.server

import kotlin.reflect.KClass

interface EventBus {
    fun <T : Any> subscribe(handler: (T) -> Nothing)
    fun <T : Any> publish(eventType: KClass<T>, event: T)
}
//
// class EventBus {
//    private val flow = MutableSharedFlow<Any>(
//        extraBufferCapacity = Int.MAX_VALUE,
//        onBufferOverflow = BufferOverflow.DROP_LATEST)
//
//    val events = flow.asSharedFlow()
//
//    suspend inline fun <reified T : Any> subscribe(crossinline handler: suspend (T) -> Nothing) {
//        events.filterIsInstance<T>().collect(handler)
//    }
//
//    fun <T : Any> publish(eventType: KClass<T>, event: T) {
//        flow.tryEmit(event)
//    }
// }

inline fun <reified T : Any> EventBus.publish(event: T) = this.publish(T::class, event)
