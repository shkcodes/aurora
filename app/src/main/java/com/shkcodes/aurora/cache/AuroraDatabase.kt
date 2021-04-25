package com.shkcodes.aurora.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity

@Database(entities = [TweetEntity::class, MediaEntity::class], version = 1)
@TypeConverters(CacheConverter::class)
abstract class AuroraDatabase : RoomDatabase() {
    companion object {
        const val NAME = "aurora.db"
    }

    abstract fun tweetsDao(): TweetsDao
}
