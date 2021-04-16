package com.shkcodes.aurora.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shkcodes.aurora.cache.entities.CachedTweets
import kotlinx.coroutines.flow.Flow

@Dao
interface TweetsDao {
    @Transaction
    @Query("SELECT * FROM tweets ORDER BY createdAt DESC")
    fun getTweets(): Flow<CachedTweets>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTweets(tweets: CachedTweets)
}
