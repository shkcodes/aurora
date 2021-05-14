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
    @Json(name = "display_text_range")
    val displayRange: List<Int>,
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
) {
    private fun String.purgeUrls(): String {
        val irrelevantUrls =
            entities.media?.map { it.shortenedUrl }
                .orEmpty() + extendedEntities?.media?.map { it.shortenedUrl }
                .orEmpty() + quoteTweetInfo?.url.orEmpty()
        return if (irrelevantUrls.isNotEmpty()) {
            replace(irrelevantUrls.joinToString("|").toRegex(), "")
        } else this
    }

    val displayableContent = content.purgeUrls().substring(displayRange.first()).trim()

    val hashTags = entities.hashtags.map { it.text }

    val repliedToUsers: List<String>
        get() {
            val hiddenHandles = content.purgeUrls().substring(0, displayRange.first())
            val mentions = entities.mentions.map { it.handle }
            return (mentions.filter { hiddenHandles.contains(it) } + listOfNotNull(
                inReplyToScreenName
            )).distinct()
        }
}

@JsonClass(generateAdapter = true)
data class Entities(
    @Json(name = "urls")
    val urls: List<Url>,
    @Json(name = "media")
    val media: List<Media>?,
    @Json(name = "hashtags")
    val hashtags: List<Hashtag>,
    @Json(name = "user_mentions")
    val mentions: List<UserMention>,
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
) {
    val aspectRatio =
        videoInfo?.let { it.aspectRatio.first().toDouble() / it.aspectRatio.last() } ?: 0.0
}

@JsonClass(generateAdapter = true)
data class VideoInfo(
    @Json(name = "duration_millis")
    val duration: Long = 0,
    @Json(name = "variants")
    val variants: List<VideoVariant>,
    @Json(name = "aspect_ratio")
    val aspectRatio: List<Int>,
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

@JsonClass(generateAdapter = true)
data class UserMention(
    @Json(name = "screen_name")
    val handle: String
)
