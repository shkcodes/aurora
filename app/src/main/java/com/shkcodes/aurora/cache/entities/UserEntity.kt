package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import twitter4j.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val handle: String,
    val name: String,
    val profileImageUrl: String
)
typealias Users = List<UserEntity>

fun User.toEntity() = UserEntity(id, screenName, name, profileImageURLHttps)