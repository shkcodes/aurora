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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.transform.RoundedCornersTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.MediaType
import com.shkcodes.aurora.cache.entities.MediaType.GIF
import com.shkcodes.aurora.cache.entities.MediaType.VIDEO
import com.shkcodes.aurora.theme.Dimens
import java.time.Duration

private const val MEDIA_CORNER_RADIUS = 4F

@Composable
fun TweetMedia(media: List<MediaEntity>, handler: (index: Int) -> Unit) {
    when (media.size) {
        1 -> {
            MediaImage(
                media = media.first(),
                modifier = Modifier
                    .padding(top = Dimens.space)
                    .fillMaxWidth()
                    .height(Dimens.single_row_media_height)
                    .clickable { handler(0) }
            )
        }
        2 -> {
            MediaRow(
                media1 = media.first(),
                media2 = media.last(),
                handler = handler
            )
        }
        3 -> {
            MediaRow(
                media1 = media[0],
                media2 = media[1], isGridRow = true,
                handler = handler
            )
            MediaImage(
                media = media[2],
                modifier = Modifier
                    .padding(top = Dimens.space_small)
                    .fillMaxWidth()
                    .height(Dimens.multi_row_media_height)
                    .clickable { handler(2) }
            )
        }
        4 -> {
            MediaRow(
                media1 = media[0],
                media2 = media[1], isGridRow = true,
                handler = handler
            )
            MediaRow(
                media1 = media[2],
                media2 = media[3], isGridRow = true, isBottomRow = true,
                handler = handler
            )
        }
    }
}

@Composable
private fun MediaRow(
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
        MediaImage(
            media = media1,
            modifier = Modifier
                .padding(end = Dimens.two_dp)
                .weight(1F)
                .clickable { handler(if (isBottomRow) 2 else 0) }
        )
        MediaImage(
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
private fun MediaImage(media: MediaEntity, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Image(
            painter = rememberCoilPainter(
                request = media.thumbnail,
                fadeIn = true,
                requestBuilder = {
                    transformations(RoundedCornersTransformation(radius = MEDIA_CORNER_RADIUS))
                },
            ),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedMediaIndicator(media.mediaType, media.duration)
    }
}

@Composable
fun AnimatedMediaIndicator(type: MediaType, duration: Long) {
    when (type) {
        GIF -> GifIndicator()
        VIDEO -> VideoDuration(duration)
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
fun VideoDuration(duration: Long) {
    val duration = Duration.ofMillis(duration)
    Text(
        text = "${"%02d".format(duration.toMinutes())}:${"%02d".format(duration.seconds)}",
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
