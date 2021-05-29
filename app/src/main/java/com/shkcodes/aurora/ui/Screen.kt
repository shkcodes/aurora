package com.shkcodes.aurora.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.shkcodes.aurora.R

enum class Screen(private val args: Map<String, NavType<*>> = emptyMap()) {
    SPLASH,
    LOGIN,
    AUTH,
    HOME,
    MEDIA_VIEWER(mapOf("tweetId" to NavType.LongType, "index" to NavType.IntType)),
    PROFILE(mapOf("userHandle" to NavType.StringType));

    val route: String = if (args.isNotEmpty()) {
        "$name/${args.keys.joinToString("/") { "{$it}" }}"
    } else {
        name
    }

    val arguments: List<NamedNavArgument>
        get() = args.map { navArgument(it.key) { it.value } }

    fun createRoute(vararg screenArgs: Any): String {
        assert(screenArgs.size == args.size)
        val placeholders = "\\{(.*?)\\}".toRegex().findAll(route).toList()
        return screenArgs.foldIndexed(route) { i, acc, argument ->
            acc.replace(placeholders[i].value, argument.toString())
        }
    }
}

enum class BottomNavScreens(val icon: ImageVector) {
    TWEETS(Icons.Default.Home),
    MENTIONS(Icons.Default.Person),
    FAVORITES(Icons.Default.Favorite),
    SETTINGS(Icons.Default.Settings),
}

sealed class Screen2 { // how imaginative
    object Login : Screen2()
    object Home : Screen2()
    object Auth : Screen2()
}

val bottomNavScreens = listOf(R.id.home_timeline, R.id.home, R.id.temp)
