@file:Suppress("MagicNumber")

package com.shkcodes.aurora.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.MediaType
import com.shkcodes.aurora.cache.entities.MediaType.GIF
import com.shkcodes.aurora.cache.entities.MediaType.VIDEO
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.util.inflate
import java.time.Duration

private const val MEDIA_CORNER_RADIUS = 8F

@Composable
fun TweetMedia(
    media: List<MediaEntity>,
    exoPlayer: SimpleExoPlayer,
    isVideoPlaying: Boolean,
    handler: (index: Int) -> Unit
) {
    if (media.isNotEmpty() && media.first().isAnimatedMedia) {
        if (isVideoPlaying) {
            TweetVideo(exoPlayer)
        } else {
            TweetImage(
                media = media.first(),
                modifier = Modifier
                    .padding(top = Dimens.space)
                    .fillMaxWidth()
                    .height(Dimens.single_row_media_height)
                    .clickable { handler(0) }
            )
        }
    } else {
        TweetImages(media, handler)
    }
}

@Composable
fun TweetVideo(exoPlayer: SimpleExoPlayer) {
    Box(
        modifier = Modifier
            .padding(top = Dimens.space)
            .fillMaxWidth()
            .height(Dimens.single_row_media_height)
            .clip(RoundedCornerShape(MEDIA_CORNER_RADIUS))
    ) {
        AndroidView(
            factory = { context ->
                context.inflate<PlayerView>(R.layout.player_view).apply {
                    player = exoPlayer
                    useController = false
                }
            }
        )
    }
}

@Composable
fun TweetImages(images: List<MediaEntity>, handler: (index: Int) -> Unit) {
    when (images.size) {
        1 -> {
            TweetImage(
                media = images.first(),
                modifier = Modifier
                    .padding(top = Dimens.space)
                    .fillMaxWidth()
                    .height(Dimens.single_row_media_height)
                    .clickable { handler(0) }
            )
        }
        2 -> {
            ImagesRow(
                media1 = images.first(),
                media2 = images.last(),
                handler = handler
            )
        }
        3 -> {
            ImagesRow(
                media1 = images[0],
                media2 = images[1], isGridRow = true,
                handler = handler
            )
            TweetImage(
                media = images[2],
                modifier = Modifier
                    .padding(top = Dimens.space_small)
                    .fillMaxWidth()
                    .height(Dimens.multi_row_media_height)
                    .clickable { handler(2) }
            )
        }
        4 -> {
            ImagesRow(
                media1 = images[0],
                media2 = images[1], isGridRow = true,
                handler = handler
            )
            ImagesRow(
                media1 = images[2],
                media2 = images[3], isGridRow = true, isBottomRow = true,
                handler = handler
            )
        }
    }
}

@Composable
private fun ImagesRow(
    media1: MediaEntity,
    media2: MediaEntity,
    handler: (index: Int) -> Unit,
    isGridRow: Boolean = false,
    isBottomRow: Boolean = false
) {
    Row(
        modifier = Modifier
            .padding(top = if (isBottomRow) Dimens.space_small else Dimens.space)
            .height(if (isGridRow) Dimens.multi_row_media_height else Dimens.single_row_media_height)
    ) {
        TweetImage(
            media = media1,
            modifier = Modifier
                .padding(end = Dimens.two_dp)
                .weight(1F)
                .clickable { handler(if (isBottomRow) 2 else 0) }
        )
        TweetImage(
            media = media2,
            modifier = Modifier
                .padding(start = Dimens.two_dp)
                .weight(1F)
                .clickable { handler(if (isBottomRow) 3 else 1) }
        )
    }
}

private const val ANIMATED_MEDIA_INDICATOR_OPACITY = 0.3F

@Composable
private fun TweetImage(media: MediaEntity, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = rememberCoilPainter(
                    request = media.thumbnail,
                    fadeIn = true
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(MEDIA_CORNER_RADIUS))
            )
            if (media.mediaType == VIDEO) {
                PlayButton()
            }
        }

        AnimatedMediaIndicator(media.mediaType, media.duration)
    }
}

@Composable
private fun PlayButton() {
    Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        tint = colors.secondary,
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.space_small))
            .size(Dimens.play_icon_size)
            .background(Color.Black.copy(ANIMATED_MEDIA_INDICATOR_OPACITY))
    )
}

@Composable
fun AnimatedMediaIndicator(type: MediaType, duration: Long) {
    when (type) {
        GIF -> GifIndicator()
        VIDEO -> VideoDuration(duration)
        else -> {
        }
    }
}

@Composable
private fun GifIndicator() {
    Icon(
        imageVector = Icons.Default.Gif,
        contentDescription = null,
        tint = colors.secondary,
        modifier = Modifier
            .padding(Dimens.space_small)
            .clip(RoundedCornerShape(Dimens.space_small))
            .size(Dimens.gif_indicator_size)
            .background(Color.Black.copy(ANIMATED_MEDIA_INDICATOR_OPACITY))
    )
}

@Composable
fun VideoDuration(millis: Long) {
    val duration = Duration.ofMillis(millis)
    Text(
        text = "${"%02d".format(duration.toMinutes())}:${"%02d".format((duration.seconds % 60))}",
        fontWeight = FontWeight.Bold,
        color = colors.secondary,
        fontSize = Dimens.text_caption,
        modifier = Modifier
            .padding(Dimens.space_small)
            .clip(RoundedCornerShape(Dimens.space_small))
            .background(Color.Black.copy(ANIMATED_MEDIA_INDICATOR_OPACITY))
            .padding(Dimens.space_small)
    )
}
