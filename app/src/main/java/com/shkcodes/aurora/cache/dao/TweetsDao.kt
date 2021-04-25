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
import com.shkcodes.aurora.cache.entities.toCachedTweets
import com.shkcodes.aurora.cache.entities.toMediaEntity
import com.shkcodes.aurora.ui.home.TimelineTweets
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Dao
abstract class TweetsDao {

    @Transaction
    @Query("SELECT * FROM tweets where isTimelineTweet = :isTimelineTweet ORDER BY createdAt DESC")
    abstract fun getCachedTimeline(isTimelineTweet: Boolean = true): Flow<TimelineTweets>

    @Query("SELECT * FROM tweets where tweetId = :tweetId")
    abstract suspend fun getTweet(tweetId: Long): TweetEntity

    suspend fun cacheTimeline(tweets: Tweets) {
        val quoteTweets = tweets.mapNotNull { it.quotedTweet }
        val retweets = tweets.mapNotNull { it.retweetedTweet }
        val media = tweets.mapNotNull { it.toMediaEntity() }.flatten()
        saveTweets(
            tweets.toCachedTweets(true) +
                    quoteTweets.toCachedTweets() +
                    retweets.toCachedTweets()
        )
        saveMedia(media)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveTweets(tweets: CachedTweets)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMedia(media: List<MediaEntity>)

    @Query("DELETE FROM tweets WHERE createdAt <= :date")
    abstract suspend fun removeTweets(date: ZonedDateTime)
}
