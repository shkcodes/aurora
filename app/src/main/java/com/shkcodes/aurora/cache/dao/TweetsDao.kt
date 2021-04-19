package com.shkcodes.aurora.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shkcodes.aurora.cache.entities.CachedTweets
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Dao
interface TweetsDao {

    @Query("SELECT * FROM tweets ORDER BY createdAt DESC")
    fun getTweets(): Flow<CachedTweets>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTweets(tweets: CachedTweets)

    @Query("DELETE FROM tweets WHERE createdAt <= :date")
    suspend fun removeTweets(date: ZonedDateTime)
}
