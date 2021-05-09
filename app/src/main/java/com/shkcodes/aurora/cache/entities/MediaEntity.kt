package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shkcodes.aurora.api.response.MediaType.GIF
import com.shkcodes.aurora.api.response.MediaType.PHOTO
import com.shkcodes.aurora.api.response.MediaType.VIDEO
import com.shkcodes.aurora.api.response.Tweet

enum class MediaType {
    GIF, VIDEO, PHOTO
}

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val id: Long,
    val tweetId: Long,
    val bitrate: Long?,
    val duration: Long,
    val mediaType: MediaType,
    val url: String,
    val thumbnail: String
)

fun Tweet.toMediaEntity(): List<MediaEntity>? {
    return extendedEntities?.media?.map {
        MediaEntity(
            id = it.id,
            tweetId = id,
            bitrate = it.videoInfo?.variants?.firstOrNull()?.bitrate,
            duration = it.videoInfo?.duration ?: 0,
            url = it.videoInfo?.variants?.firstOrNull()?.url ?: it.url,
            mediaType = it.type.toEntityMediaType(),
            thumbnail = it.url
        )
    }
}

private fun com.shkcodes.aurora.api.response.MediaType.toEntityMediaType(): MediaType {
    return when (this) {
        GIF -> MediaType.GIF
        PHOTO -> MediaType.PHOTO
        VIDEO -> MediaType.VIDEO
    }
}
