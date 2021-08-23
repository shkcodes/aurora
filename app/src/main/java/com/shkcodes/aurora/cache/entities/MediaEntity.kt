package com.shkcodes.aurora.cache.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import twitter4j.Status

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
    val thumbnail: String,
    val aspectRatio: Double
) {
    @Ignore
    val isAnimatedMedia = mediaType == MediaType.VIDEO || mediaType == MediaType.GIF
}

fun Status.toMediaEntity(): List<MediaEntity> {
    return mediaEntities.map {
        MediaEntity(
            id = it.id,
            tweetId = id,
            bitrate = it.videoVariants?.firstOrNull()?.bitrate?.toLong(),
            duration = it.videoDurationMillis,
            url = it.videoVariants?.maxByOrNull { it.bitrate }?.url ?: it.mediaURLHttps,
            mediaType = it.type.toEntityMediaType(),
            thumbnail = it.mediaURLHttps,
            aspectRatio = it.videoAspectRatioWidth.toDouble()
        )
    }
}

private fun String.toEntityMediaType(): MediaType {
    return when (this) {
        "animated_gif" -> MediaType.GIF
        "photo" -> MediaType.PHOTO
        else -> MediaType.VIDEO
    }
}
