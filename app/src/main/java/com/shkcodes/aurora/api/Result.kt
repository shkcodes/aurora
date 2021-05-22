package com.shkcodes.aurora.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

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

suspend fun <T1, T2> CoroutineScope.zip(
    a: Result<T1>,
    b: Result<T2>
): Pair<Result<T1>, Result<T2>> {
    val call1 = async { a }
    val call2 = async { b }
    return Pair(call1.await(), call2.await())
}

fun <T1, T2> Pair<Result<T1>, Result<T2>>.evaluate(
    onSuccess: (Pair<T1, T2>) -> Unit,
    onFailure: (exception: Throwable) -> Unit
) {
    when {
        first.isFailure -> {
            onFailure(first.exception)
        }
        second.isFailure -> {
            onFailure(second.exception)
        }
        else -> {
            onSuccess(Pair(first.value, second.value))
        }
    }
}
