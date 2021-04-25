package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.MediaType.GIF
import com.shkcodes.aurora.api.response.Tweet

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val id: Long,
    val tweetId: Long,
    val bitrate: Long?,
    val duration: Long?,
    val isGif: Boolean,
    val imageUrl: String,
    val videoUrl: String?,
)

fun Tweet.toMediaEntity(): List<MediaEntity>? {
    return extendedEntities?.media?.map {
        MediaEntity(
            id = it.id,
            tweetId = id,
            bitrate = it.videoInfo?.variants?.firstOrNull()?.bitrate,
            duration = it.videoInfo?.duration,
            imageUrl = it.url,
            isGif = it.type == GIF,
            videoUrl = it.videoInfo?.variants?.firstOrNull()?.url
        )
    }
}
