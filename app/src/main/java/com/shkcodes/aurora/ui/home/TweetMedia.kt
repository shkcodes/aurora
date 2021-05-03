@file:Suppress("MagicNumber")

package com.shkcodes.aurora.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.transform.RoundedCornersTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.theme.Dimens

private const val MEDIA_CORNER_RADIUS = 4F

@Composable
fun TweetMedia(media: List<MediaEntity>) {
    when (media.size) {
        1 -> {
            MediaImage(
                url = media.first().imageUrl,
                modifier = Modifier
                    .padding(top = Dimens.space)
                    .fillMaxWidth()
                    .height(Dimens.single_row_media_height)
            )
        }
        2 -> {
            MediaRow(
                url1 = media.first().imageUrl,
                url2 = media.last().imageUrl,
            )
        }
        3 -> {
            MediaRow(
                url1 = media[0].imageUrl,
                url2 = media[1].imageUrl, isGridRow = true
            )
            MediaImage(
                url = media[2].imageUrl,
                modifier = Modifier
                    .padding(top = Dimens.space_small)
                    .fillMaxWidth()
                    .height(Dimens.multi_row_media_height)
            )
        }
        4 -> {
            MediaRow(
                url1 = media[0].imageUrl,
                url2 = media[1].imageUrl, isGridRow = true
            )
            MediaRow(
                url1 = media[2].imageUrl,
                url2 = media[3].imageUrl, isGridRow = true, isBottomRow = true
            )
        }
    }
}

@Composable
private fun MediaRow(
    url1: String,
    url2: String,
    isGridRow: Boolean = false,
    isBottomRow: Boolean = false
) {
    Row(
        modifier = Modifier
            .padding(top = if (isBottomRow) Dimens.space_small else Dimens.space)
            .height(if (isGridRow) Dimens.multi_row_media_height else Dimens.single_row_media_height)
    ) {
        MediaImage(
            url = url1,
            modifier = Modifier
                .padding(end = Dimens.two_dp)
                .weight(1F)
        )
        MediaImage(
            url = url2,
            modifier = Modifier
                .padding(start = Dimens.two_dp)
                .weight(1F)
        )
    }
}

@Composable
private fun MediaImage(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberCoilPainter(
            request = url,
            fadeIn = true,
            requestBuilder = {
                transformations(RoundedCornersTransformation(radius = MEDIA_CORNER_RADIUS))
            },
        ),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
    )
}
