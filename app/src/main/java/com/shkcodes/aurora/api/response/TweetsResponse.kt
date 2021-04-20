package com.shkcodes.aurora.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.ZonedDateTime

typealias Tweets = List<Tweet>

@JsonClass(generateAdapter = true)
data class Tweet(
    @Json(name = "id")
    val id: Long,
    @Json(name = "created_at")
    val createdAt: ZonedDateTime,
    @Json(name = "full_text")
    val content: String,
    @Json(name = "favorite_count")
    val favoriteCount: Int,
    @Json(name = "favorited")
    val favorited: Boolean,
    @Json(name = "in_reply_to_screen_name")
    val inReplyToScreenName: String?,
    @Json(name = "in_reply_to_status_id")
    val inReplyToStatusId: String?,
    @Json(name = "in_reply_to_user_id")
    val inReplyToUserId: String?,
    @Json(name = "is_quote_status")
    val isQuoteTweet: Boolean,
    @Json(name = "possibly_sensitive")
    val possiblySensitive: Boolean = false,
    @Json(name = "retweet_count")
    val retweetCount: Int,
    @Json(name = "retweeted")
    val retweeted: Boolean,
    @Json(name = "truncated")
    val truncated: Boolean,
    @Json(name = "user")
    val user: User,
    @Json(name = "entities")
    val entities: Entities,
    @Json(name = "quoted_status")
    val quotedTweet: Tweet?
)

@JsonClass(generateAdapter = true)
data class Entities(
    @Json(name = "urls")
    val urls: List<Url>
)

@JsonClass(generateAdapter = true)
data class Url(
    @Json(name = "display_url")
    val url: String
)
