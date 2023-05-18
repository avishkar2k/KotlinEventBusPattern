package com.example.eventbusactivity

import android.view.View
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.coroutines.coroutineContext

abstract class EventBus<T> {
    private val _events = MutableSharedFlow<T>()
    abstract val events: SharedFlow<T>

    suspend fun invokeEvent(event: T) = _events.emit(event)

    fun getAsSharedFlow(): SharedFlow<T> {
        return _events.asSharedFlow()
    }
}

object ListenClickEvent : EventBus<View>() {
    override val events: SharedFlow<View>
        get() = getAsSharedFlow()

}

object ListenUiChangeEvent : EventBus<String>() {
    override val events: SharedFlow<String>
        get() = getAsSharedFlow()

}

object EventBusObj {
    private val _events = MutableSharedFlow<Any>()
    val events = _events.asSharedFlow()

    suspend fun publish(event: Any) {
        _events.emit(event)
    }

    suspend inline fun <reified T> subscribe(crossinline onEvent: (T) -> Unit) {
        events.filterIsInstance<T>().collectLatest { event ->
            coroutineContext.ensureActive()
            onEvent(event)
        }
    }
}