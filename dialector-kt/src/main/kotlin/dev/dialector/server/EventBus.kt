package dev.dialector.server

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.reflect.KClass

class EventBus {
    private val flow = MutableSharedFlow<Any>(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_LATEST)

    val events = flow.asSharedFlow()

    suspend inline fun <reified T : Any> subscribe(crossinline handler: suspend (T) -> Nothing) {
        events.filterIsInstance<T>().collect(handler)
    }

    fun <T : Any> publish(eventType: KClass<T>, event: T) {
        flow.tryEmit(event)
    }
}

inline fun <reified T : Any> EventBus.publish(event: T) = this.publish(T::class, event)