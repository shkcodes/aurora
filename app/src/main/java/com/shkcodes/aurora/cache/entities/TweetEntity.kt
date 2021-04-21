package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.api.response.Tweets
import java.time.ZonedDateTime

@Entity(tableName = "tweets")
data class TweetEntity(
    @PrimaryKey val tweetId: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val isTimelineTweet: Boolean,
    val favoriteCount: Int,
    val inReplyToScreenName: String?,
    val inReplyToStatusId: String?,
    val inReplyToUserId: String?,
    val isQuoteTweet: Boolean,
    val possiblySensitive: Boolean,
    val retweetCount: Int,
    val retweeted: Boolean,
    val truncated: Boolean,
    val userId: Long,
    val userName: String,
    val userHandle: String,
    val userProfileImageUrl: String,
    val mediaUrl: String,
    val quotedTweetId: Long?,
    val retweetedTweetId: Long?
)

private fun Tweet.toTweetEntity(isTimelineTweet: Boolean): TweetEntity = TweetEntity(
    tweetId = id,
    content = displayableContent,
    createdAt = createdAt,
    isTimelineTweet = isTimelineTweet,
    favoriteCount = favoriteCount,
    inReplyToScreenName = inReplyToScreenName,
    inReplyToStatusId = inReplyToStatusId,
    inReplyToUserId = inReplyToUserId,
    isQuoteTweet = isQuoteTweet,
    possiblySensitive = possiblySensitive,
    retweetCount = retweetCount,
    retweeted = retweeted,
    truncated = truncated,
    userId = user.id,
    userName = user.name,
    userHandle = user.screenName,
    userProfileImageUrl = user.profileImageUrl,
    mediaUrl = entities.urls.firstOrNull()?.url.orEmpty(),
    quotedTweetId = quotedTweet?.id,
    retweetedTweetId = retweetedTweet?.id
)

fun Tweets.toCachedTweets(isTimelineTweet: Boolean = false) =
    map { it.toTweetEntity(isTimelineTweet) }

typealias CachedTweets = List<TweetEntity>

private const val TWITTER_URL_REGEX = "https:(//t\\.co/([A-Za-z0-9]|[A-Za-z]){10})"

private val Tweet.displayableContent: String
    get() = TWITTER_URL_REGEX.toRegex().replace(content, "")
