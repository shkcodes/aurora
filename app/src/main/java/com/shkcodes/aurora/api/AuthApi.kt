package com.shkcodes.aurora.api

import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("oauth/request_token")
    suspend fun getRequestToken(): String

    @POST("oauth/access_token")
    suspend fun getAccessToken(
        @Query("oauth_verifier") verifier: String,
        @Query("oauth_token") token: String
    )
}
