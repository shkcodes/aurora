package com.shkcodes.aurora.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimens {
    val zero_dp: Dp @Composable get() = 0.dp
    val two_dp: Dp @Composable get() = 2.dp
    val space_small: Dp @Composable get() = 4.dp
    val space: Dp @Composable get() = 8.dp
    val space_large: Dp @Composable get() = 24.dp
    val space_xxxlarge: Dp @Composable get() = 56.dp

    val keyline_1: Dp @Composable get() = 16.dp

    val text_body: TextUnit @Composable get() = 16.sp
    val text_caption: TextUnit @Composable get() = 12.sp
    val text_large: TextUnit @Composable get() = 18.sp

    val corner_radius: Dp @Composable get() = 4.dp

    val tweet_profile_pic: Dp @Composable get() = 32.dp
    val single_row_media_height: Dp @Composable get() = 170.dp
    val multi_row_media_height: Dp @Composable get() = 100.dp
    val link_preview_height: Dp @Composable get() = 75.dp
    val gif_indicator_size: Dp @Composable get() = 28.dp
    val play_icon_size: Dp @Composable get() = 36.dp
    val video_player_max_height: Dp @Composable get() = 200.dp
}
