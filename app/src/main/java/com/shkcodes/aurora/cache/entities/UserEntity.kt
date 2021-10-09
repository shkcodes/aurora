package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import twitter4j.User

@Entity(tableName = "users", primaryKeys = ["id", "friendOf"])
data class UserEntity(
    val id: Long,
    val handle: String,
    val name: String,
    val profileImageUrl: String,
    val friendOf: Long
)
typealias Users = List<UserEntity>

fun User.toEntity(friendOf: Long) = UserEntity(id, screenName, name, profileImageURLHttps, friendOf)
