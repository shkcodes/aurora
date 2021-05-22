package com.shkcodes.aurora.api

import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.User
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("1.1/statuses/home_timeline.json")
    suspend fun getTimelineTweets(
        @Query("count") count: Int = 30,
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("max_id") afterId: Long? = null
    ): Tweets

    @GET("1.1/users/show.json")
    suspend fun getUserProfile(@Query("screen_name") userHandle: String): User

    @GET("1.1/statuses/user_timeline.json")
    suspend fun getUserTweets(
        @Query("tweet_mode") tweetMode: String = "extended"
    ): Tweets
}
