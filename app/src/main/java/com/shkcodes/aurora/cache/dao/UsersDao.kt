package com.shkcodes.aurora.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shkcodes.aurora.cache.entities.Users

@Dao
interface UsersDao {

    @Query("SELECT * FROM users where id IN (:ids)")
    fun getUsers(ids: List<Long>): Users

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUsers(users: Users)
}
