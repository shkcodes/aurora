package com.shkcodes.aurora.api

import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.StringId
import com.shkcodes.aurora.base.StringProvider
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkErrorHandler @Inject constructor(
    private val stringProvider: StringProvider
) : ErrorHandler {

    private fun getString(stringId: StringId) = stringProvider.getString(stringId)

    override fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> getString(StringId.NO_INTERNET)
            is SocketTimeoutException -> getString(StringId.TIMEOUT)
            else -> getString(StringId.UNKNOWN)
        }
    }
}
