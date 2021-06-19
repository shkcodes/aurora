package com.shkcodes.aurora.ui

import com.shkcodes.aurora.R

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    object Auth : Screen()
}

val bottomNavScreens = listOf(R.id.home_timeline, R.id.home, R.id.temp)
