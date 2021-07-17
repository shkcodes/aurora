package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.api.response.Url
import java.time.ZonedDateTime

@Entity(tableName = "tweets")
data class TweetEntity(
    @PrimaryKey val id: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val tweetType: TweetType,
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
    val sharedUrls: List<Url>,
    val quoteTweetId: Long?,
    val retweetId: Long?,
    val retweetQuoteId: Long?,
    val hashtags: List<String>,
    val repliedToUsers: List<String>,
    val likedBy: String?
)

fun Tweet.toTweetEntity(tweetType: TweetType, likedBy: String?): TweetEntity = TweetEntity(
    id = id,
    content = displayableContent,
    createdAt = createdAt,
    tweetType = tweetType,
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
    sharedUrls = entities.urls.filterNot { it.shortenedUrl == quoteTweetInfo?.url },
    quoteTweetId = quoteTweet?.id,
    retweetId = retweet?.id,
    retweetQuoteId = retweet?.quoteTweet?.id,
    hashtags = hashTags,
    repliedToUsers = repliedToUsers,
    likedBy = likedBy
)

fun Tweets.toCachedTweets(tweetType: TweetType, likedBy: String? = null) =
    map { it.toTweetEntity(tweetType, likedBy) }

typealias CachedTweets = List<TweetEntity>

enum class TweetType {
    TIMELINE, USER, FAVORITES, NONE
}
