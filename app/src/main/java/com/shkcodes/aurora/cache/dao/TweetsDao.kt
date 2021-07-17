package com.shkcodes.aurora.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shkcodes.aurora.api.response.Tweets
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.TweetType
import com.shkcodes.aurora.cache.entities.TweetType.NONE
import com.shkcodes.aurora.cache.entities.toCachedTweets
import com.shkcodes.aurora.cache.entities.toMediaEntity
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import java.time.ZonedDateTime

@Dao
abstract class TweetsDao {

    @Transaction
    @Query(
        "SELECT * FROM tweets" +
            " where (tweetType = :type) AND (createdAt > :createdAt) ORDER BY createdAt DESC"
    )
    abstract suspend fun getCachedTimeline(
        createdAt: ZonedDateTime,
        type: TweetType = TweetType.TIMELINE
    ): TweetItems

    @Transaction
    @Query(
        "SELECT * FROM tweets" +
            " where (tweetType = :type) AND (userHandle = :userHandle) ORDER BY createdAt DESC"
    )
    abstract suspend fun getUserTweets(userHandle: String, type: TweetType = TweetType.USER): TweetItems

    @Query("SELECT * FROM tweets where id = :tweetId")
    abstract suspend fun getTweet(tweetId: Long): TweetEntity

    suspend fun cacheTimeline(tweets: Tweets, tweetType: TweetType) {
        val quoteTweets = tweets.mapNotNull { it.quoteTweet }
        val retweets = tweets.mapNotNull { it.retweet }
        val media = tweets.mapNotNull { it.toMediaEntity() }.flatten()
        val quoteTweetsMedia = quoteTweets.mapNotNull { it.toMediaEntity() }.flatten()
        val retweetsMedia = retweets.mapNotNull { it.toMediaEntity() }.flatten()
        val retweetQuoteTweets = retweets.mapNotNull { it.quoteTweet }
        saveTweets(
            tweets.toCachedTweets(tweetType) +
                quoteTweets.toCachedTweets(NONE) +
                retweets.toCachedTweets(NONE) +
                retweetQuoteTweets.toCachedTweets(NONE)
        )
        saveMedia(media + quoteTweetsMedia + retweetsMedia)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTweets(tweets: CachedTweets)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMedia(media: List<MediaEntity>)

    @Query("DELETE FROM tweets WHERE createdAt <= :date")
    abstract suspend fun removeTweets(date: ZonedDateTime)

    @Query("SELECT * FROM media where tweetId = :tweetId")
    abstract suspend fun getTweetMedia(tweetId: Long): List<MediaEntity>
}
