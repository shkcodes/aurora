package com.shkcodes.aurora.api

import com.shkcodes.aurora.api.response.Tweets
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("1.1/statuses/home_timeline.json")
    suspend fun getTimelineTweets(
        @Query("count") count: Int = 30,
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("max_id") maxId: Long? = null
    ): Tweets
}
