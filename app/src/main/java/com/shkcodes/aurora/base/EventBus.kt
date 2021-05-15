package com.shkcodes.aurora.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Event {
    object AutoplayVideosToggled : Event()
}

interface EventBus {
    fun getEvents(): Flow<Event>
    suspend fun sendEvent(event: Event)
}

@Singleton
class EventBusImpl @Inject constructor() : EventBus {
    private val eventsFlow = MutableSharedFlow<Event>()

    override fun getEvents(): Flow<Event> = eventsFlow

    override suspend fun sendEvent(event: Event) {
        eventsFlow.emit(event)
    }
}
