package com.shkcodes.aurora.api

import com.shkcodes.aurora.api.response.MediaUploadResponse
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.util.ApiConstants.UPLOAD_MEDIA_URL
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

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
    suspend fun postTweet(
        @Query("media_ids", encoded = true) mediaIds: String,
        @Query("status") content: String
    )

    @POST()
    @Multipart
    suspend fun uploadMedia(
        @Part imageFile: MultipartBody.Part,
        @Url url: String = UPLOAD_MEDIA_URL
    ): MediaUploadResponse
}
