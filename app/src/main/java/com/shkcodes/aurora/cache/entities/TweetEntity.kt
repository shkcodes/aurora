package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import twitter4j.Status
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity(tableName = "tweets")
data class TweetEntity(
    @PrimaryKey val id: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val tweetType: TweetType,
    val favoriteCount: Int,
    val inReplyToScreenName: String?,
    val inReplyToStatusId: Long?,
    val inReplyToUserId: Long?,
    val isQuoteTweet: Boolean,
    val possiblySensitive: Boolean,
    val retweetCount: Int,
    val retweeted: Boolean,
    val userId: Long,
    val userName: String,
    val userHandle: String,
    val userProfileImageUrl: String,
    val sharedUrls: List<Url>,
    val quoteTweetId: Long?,
    val retweetId: Long?,
    val retweetQuoteId: Long?,
    val hashtags: List<String>,
    val repliedToUsers: List<String>,
    val likedBy: String?
)

fun Status.toTweetEntity(tweetType: TweetType, likedBy: String?): TweetEntity {
    val instant = createdAt.toInstant()
    val creationTime = instant.atZone(ZoneId.systemDefault())
    return TweetEntity(
        id = id,
        content = displayableContent,
        createdAt = creationTime,
        tweetType = tweetType,
        favoriteCount = favoriteCount,
        inReplyToScreenName = inReplyToScreenName,
        inReplyToStatusId = inReplyToStatusId,
        inReplyToUserId = inReplyToUserId,
        isQuoteTweet = quotedStatus != null,
        possiblySensitive = isPossiblySensitive,
        retweetCount = retweetCount,
        retweeted = retweetedStatus != null,
        userId = user.id,
        userName = user.name,
        userHandle = user.screenName,
        userProfileImageUrl = user.profileImageURLHttps,
        sharedUrls = sharedUrls,
        quoteTweetId = quotedStatusId,
        retweetId = retweetedStatus?.id,
        retweetQuoteId = retweetedStatus?.quotedStatusId,
        hashtags = hashtagEntities.map { it.text },
        repliedToUsers = repliedToUsers,
        likedBy = likedBy
    )
}

private val Status.sharedUrls: List<Url>
    get() {
        return urlEntities.filterNot { it.url == quotedStatusPermalink?.url }
            .map { Url(it.url, it.displayURL, it.expandedURL) }
    }

private fun Status.purgeUrls(): String {
    val irrelevantUrls = mediaEntities?.map { it.url }
        .orEmpty() + quotedStatusPermalink?.url.orEmpty()
    return if (irrelevantUrls.isNotEmpty()) {
        text.replace(irrelevantUrls.joinToString("|").toRegex(), "")
    } else text
}

private val Status.displayableContent: String
    get() = purgeUrls().substring(displayTextRangeStart).trim()

private val Status.repliedToUsers: List<String>
    get() {
        val hiddenHandles = purgeUrls().substring(0, displayTextRangeStart)
        val mentions = userMentionEntities.map { it.screenName }
        return (mentions.filter { hiddenHandles.contains(it) } + listOfNotNull(
            inReplyToScreenName
        )).distinct()
    }

fun List<Status>.toCachedTweets(tweetType: TweetType, likedBy: String? = null) =
    map { it.toTweetEntity(tweetType, likedBy) }

typealias CachedTweets = List<TweetEntity>

enum class TweetType {
    TIMELINE, USER, FAVORITES, NONE
}

@JsonClass(generateAdapter = true)
data class Url(
    @Json(name = "url")
    val shortenedUrl: String,
    @Json(name = "display_url")
    val displayUrl: String,
    @Json(name = "expanded_url")
    val url: String
)
