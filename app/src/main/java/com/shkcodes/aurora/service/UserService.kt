package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.execute
import com.shkcodes.aurora.api.response.Tweets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getTimelineTweets(): Result<Tweets> {
        return execute { userApi.getTimelineTweets() }
    }
}
