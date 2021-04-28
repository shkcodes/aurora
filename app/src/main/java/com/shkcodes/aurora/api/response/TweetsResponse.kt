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
    @Json(name = "extended_entities")
    val extendedEntities: ExtendedEntities?,
    @Json(name = "quoted_status")
    val quoteTweet: Tweet?,
    @Json(name = "quoted_status_permalink")
    val quoteTweetInfo: QuoteTweetInfo?,
    @Json(name = "retweeted_status")
    val retweet: Tweet?
)

@JsonClass(generateAdapter = true)
data class Entities(
    @Json(name = "urls")
    val urls: List<Url>,
    @Json(name = "media")
    val media: List<Media>?,
    @Json(name = "hashtags")
    val hashtags: List<Hashtag>,
)

@JsonClass(generateAdapter = true)
data class ExtendedEntities(
    @Json(name = "media")
    val media: List<Media>
)

@JsonClass(generateAdapter = true)
data class Url(
    @Json(name = "url")
    val shortenedUrl: String,
    @Json(name = "display_url")
    val displayUrl: String,
    @Json(name = "expanded_url")
    val url: String
)

enum class MediaType {
    @Json(name = "video")
    VIDEO,

    @Json(name = "photo")
    PHOTO,

    @Json(name = "animated_gif")
    GIF
}

@JsonClass(generateAdapter = true)
data class Media(
    @Json(name = "id")
    val id: Long,
    @Json(name = "url")
    val shortenedUrl: String,
    @Json(name = "media_url_https")
    val url: String,
    @Json(name = "type")
    val type: MediaType,
    @Json(name = "video_info")
    val videoInfo: VideoInfo?,
)

@JsonClass(generateAdapter = true)
data class VideoInfo(
    @Json(name = "duration_millis")
    val duration: Long = 0,
    @Json(name = "variants")
    val variants: List<VideoVariant>
)

@JsonClass(generateAdapter = true)
data class VideoVariant(
    @Json(name = "bitrate")
    val bitrate: Long = 0,
    @Json(name = "url")
    val url: String,
)

@JsonClass(generateAdapter = true)
data class QuoteTweetInfo(
    @Json(name = "url")
    val url: String
)

@JsonClass(generateAdapter = true)
data class Hashtag(
    @Json(name = "text")
    val text: String
)
