package com.shkcodes.aurora.cache.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.User
import java.time.ZonedDateTime

@Entity(tableName = "tweets")
data class TweetEntity(
    @PrimaryKey val tweetId: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val favoriteCount: Int,
    val inReplyToScreenName: String?,
    val inReplyToStatusId: String?,
    val inReplyToUserId: String?,
    val isQuoteStatus: Boolean,
    val possiblySensitive: Boolean,
    val retweetCount: Int,
    val retweeted: Boolean,
    val truncated: Boolean,
    @Embedded val user: CachedUser,
    val mediaUrl: String
)

private fun Tweet.toTweetEntity() = TweetEntity(
    tweetId = id,
    content = content,
    createdAt = createdAt,
    favoriteCount = favoriteCount,
    inReplyToScreenName = inReplyToScreenName,
    inReplyToStatusId = inReplyToStatusId,
    inReplyToUserId = inReplyToUserId,
    isQuoteStatus = isQuoteStatus,
    possiblySensitive = possiblySensitive,
    retweetCount = retweetCount,
    retweeted = retweeted,
    truncated = truncated,
    user = user.toCachedUser(),
    mediaUrl = entities.urls.firstOrNull()?.url.orEmpty()
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

fun Tweets.toCachedTweets() = map { it.toTweetEntity() }

typealias CachedTweets = List<TweetEntity>
