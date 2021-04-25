package com.shkcodes.aurora.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimens {
    val space_small: Dp @Composable get() = 4.dp
    val space: Dp @Composable get() = 8.dp
    val space_large: Dp @Composable get() = 24.dp
    val space_xxxlarge: Dp @Composable get() = 56.dp

    val keyline_1: Dp @Composable get() = 16.dp

    val text_body: TextUnit @Composable get() = 16.sp
    val text_caption: TextUnit @Composable get() = 12.sp
    val text_large: TextUnit @Composable get() = 18.sp

    val tweet_profile_pic: Dp @Composable get() = 32.dp
    val tweet_single_media_height: Dp @Composable get() = 170.dp
}
