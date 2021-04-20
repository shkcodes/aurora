package com.shkcodes.aurora.cache.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.User
import java.time.ZonedDateTime

private const val TWITTER_URL_REGEX = "https:(//t\\.co/([A-Za-z0-9]|[A-Za-z]){10})"

@Entity(tableName = "tweets")
data class TweetEntity(
    @PrimaryKey val tweetId: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val favoriteCount: Int,
    val inReplyToScreenName: String?,
    val inReplyToStatusId: String?,
    val inReplyToUserId: String?,
    val isQuoteTweet: Boolean,
    val possiblySensitive: Boolean,
    val retweetCount: Int,
    val retweeted: Boolean,
    val truncated: Boolean,
    @Embedded val user: CachedUser,
    val mediaUrl: String,
    @Embedded val quotedTweet: QuotedTweet?
)

private fun Tweet.toTweetEntity() = TweetEntity(
    tweetId = id,
    content = displayableContent,
    createdAt = createdAt,
    favoriteCount = favoriteCount,
    inReplyToScreenName = inReplyToScreenName,
    inReplyToStatusId = inReplyToStatusId,
    inReplyToUserId = inReplyToUserId,
    isQuoteTweet = isQuoteTweet,
    possiblySensitive = possiblySensitive,
    retweetCount = retweetCount,
    retweeted = retweeted,
    truncated = truncated,
    user = user.toCachedUser(),
    mediaUrl = entities.urls.firstOrNull()?.url.orEmpty(),
    quotedTweet = quotedTweet?.toQuotedTweet()
)

data class CachedUser(
    val userId: Long,
    val name: String,
    val screenName: String,
    val profileImageUrl: String
)

fun User.toCachedUser() = CachedUser(
    userId = id,
    name = name,
    screenName = screenName,
    profileImageUrl = profileImageUrl
)

data class QuotedTweet(
    val quotedContent: String,
    @Embedded val quotedUser: QuotedUser,
    val quotedUrl: String?
)

private fun Tweet.toQuotedTweet() = QuotedTweet(
    quotedContent = displayableContent,
    quotedUser = user.toQuotedUser(),
    quotedUrl = entities.urls.firstOrNull()?.url.orEmpty()
)

fun User.toQuotedUser() = QuotedUser(
    quotedUserId = id,
    quotedUserName = name,
    quotedUserScreenName = screenName,
    quotedUserProfileImageUrl = profileImageUrl
)

data class QuotedUser(
    val quotedUserId: Long,
    val quotedUserName: String,
    val quotedUserScreenName: String,
    val quotedUserProfileImageUrl: String
)

fun Tweets.toCachedTweets() = map { it.toTweetEntity() }

typealias CachedTweets = List<TweetEntity>

private val Tweet.displayableContent: String
    get() = TWITTER_URL_REGEX.toRegex().replace(content, "")
