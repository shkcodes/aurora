package com.shkcodes.aurora.api

sealed class Result<T> {

    data class Success<T>(val value: T) : Result<T>()

    data class Failure<T>(val exception: Throwable) : Result<T>()

    val isFailure: Boolean
        get() = this is Failure
}

val <T> Result<T>.value: T
    get() = (this as? Result.Success)?.value ?: throw IllegalStateException("not a success")

val <T> Result<T>.exception: Throwable
    get() = (this as? Result.Failure)?.exception ?: throw IllegalStateException("not a failure")

inline fun <T> Result<T>.evaluate(
    onSuccess: (T) -> Unit,
    onFailure: (exception: Throwable) -> Unit
) {
    if (isFailure) onFailure(exception) else onSuccess(value)
}

@Suppress("TooGenericExceptionCaught")
suspend fun <T> execute(action: suspend (() -> T)): Result<T> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: Exception) {
        Result.Failure(e)
    }
}
