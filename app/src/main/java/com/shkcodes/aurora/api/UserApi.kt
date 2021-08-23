package com.shkcodes.aurora.api

import com.shkcodes.aurora.api.response.User
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("1.1/users/show.json")
    suspend fun getUserProfile(@Query("screen_name") userHandle: String): User
}
