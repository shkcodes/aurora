package com.shkcodes.aurora.service

import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

interface TimeProvider {
    fun now(): LocalDateTime = LocalDateTime.now()
}

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider
