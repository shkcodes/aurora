package com.shkcodes.aurora.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DispatcherProvider @Inject constructor() {

    open val main: CoroutineDispatcher = Dispatchers.Main
    open val io: CoroutineDispatcher = Dispatchers.IO
    open val default: CoroutineDispatcher = Dispatchers.Default
    open val unconfined: CoroutineDispatcher = Dispatchers.Unconfined

    suspend fun <T> execute(block: () -> T): T {
        return withContext(io) {
            block()
        }
    }
}
