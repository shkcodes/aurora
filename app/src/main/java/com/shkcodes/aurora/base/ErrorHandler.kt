package com.shkcodes.aurora.base

interface ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String
}
