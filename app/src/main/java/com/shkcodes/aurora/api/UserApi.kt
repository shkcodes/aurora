package com.shkcodes.aurora.api

import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.User
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {

    @GET("1.1/statuses/home_timeline.json")
    suspend fun getTimelineTweets(
        @Query("count") count: Int = 30,
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("max_id") afterId: Long? = null,
        @Query("since_id") sinceId: Long? = null
    ): Tweets

    @GET("1.1/users/show.json")
    suspend fun getUserProfile(@Query("screen_name") userHandle: String): User

    @GET("1.1/statuses/user_timeline.json")
    suspend fun getUserTweets(
        @Query("screen_name") userHandle: String,
        @Query("max_id") afterId: Long? = null,
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("count") count: Int = 200,
        @Query("exclude_replies") excludeReplies: Boolean = false
    ): Tweets

    @GET("1.1/favorites/list.json")
    suspend fun getFavoriteTweets(
        @Query("screen_name") userHandle: String,
        @Query("max_id") afterId: Long? = null,
        @Query("tweet_mode") tweetMode: String = "extended",
        @Query("count") count: Int = 200,
        @Query("exclude_replies") excludeReplies: Boolean = false
    ): Tweets

    @POST("1.1/statuses/update.json")
    suspend fun postTweet(@Query("status") content: String): Tweet
}
