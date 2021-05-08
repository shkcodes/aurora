package com.shkcodes.aurora.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen {
    SPLASH,
    LOGIN,
    AUTH,
    HOME,
    MEDIA_VIEWER
}

enum class BottomNavScreens(val icon: ImageVector) {
    TWEETS(Icons.Default.Home),
    MENTIONS(Icons.Default.Person),
    FAVORITES(Icons.Default.Favorite),
}
