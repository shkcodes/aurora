package com.shkcodes.aurora.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.dao.UsersDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.UserEntity

@Database(entities = [TweetEntity::class, MediaEntity::class, UserEntity::class], version = 1)
@TypeConverters(CacheConverter::class)
abstract class AuroraDatabase : RoomDatabase() {
    companion object {
        const val NAME = "aurora.db"
    }

    abstract fun tweetsDao(): TweetsDao
    abstract fun usersDao(): UsersDao
}
