package com.shkcodes.aurora.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.TweetType
import com.shkcodes.aurora.cache.entities.TweetType.NONE
import com.shkcodes.aurora.cache.entities.toCachedTweets
import com.shkcodes.aurora.cache.entities.toMediaEntity
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import twitter4j.Status
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

    @Transaction
    @Query(
        "SELECT * FROM tweets" +
            " where (tweetType = :type) AND (likedBy = :userHandle) ORDER BY createdAt DESC"
    )
    abstract suspend fun getUserFavorites(
        userHandle: String,
        type: TweetType = TweetType.FAVORITES
    ): TweetItems

    @Query("SELECT * FROM tweets where id = :tweetId")
    abstract suspend fun getTweet(tweetId: Long): TweetEntity

    suspend fun cacheTimeline(tweets: List<Status>, tweetType: TweetType, likedBy: String? = null) {
        val quoteTweets = tweets.mapNotNull { it.quotedStatus }
        val retweets = tweets.mapNotNull { it.retweetedStatus }
        val media = tweets.map { it.toMediaEntity() }.flatten()
        val quoteTweetsMedia = quoteTweets.map { it.toMediaEntity() }.flatten()
        val retweetsMedia = retweets.map { it.toMediaEntity() }.flatten()
        val retweetQuoteTweets = retweets.mapNotNull { it.quotedStatus }
        saveTweets(
            tweets.toCachedTweets(tweetType, likedBy) +
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
