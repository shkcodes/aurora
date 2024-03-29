package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.AuthApi
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.execute
import com.shkcodes.aurora.cache.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val api: AuthApi,
    private val preferenceManager: PreferenceManager
) {

    suspend fun getRequestToken(): Result<String> {
        return execute {
            val response = api.getRequestToken()
            response.parseToken()
        }
    }

    suspend fun getAccessToken(verifier: String, token: String): Result<Unit> {
        return execute { api.getAccessToken(verifier, token) }
    }

    private fun String.parseToken(): String {
        return split("&").first().split("=")[1]
    }

    var isLoggedIn: Boolean
        get() = preferenceManager.isLoggedIn
        set(value) {
            preferenceManager.isLoggedIn = value
        }
}
