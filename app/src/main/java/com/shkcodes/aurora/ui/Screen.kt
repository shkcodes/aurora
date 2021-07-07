package com.shkcodes.aurora.ui

import com.shkcodes.aurora.R

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    object Auth : Screen()
    data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
    data class UserProfile(val userHandle: String) : Screen()
    data class UserMediaViewer(val index: Int, val userHandle: String) : Screen()
}

val bottomNavScreens = listOf(R.id.home_timeline, R.id.home, R.id.temp)
