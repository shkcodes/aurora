package com.shkcodes.aurora.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.BottomNavScreens
import com.shkcodes.aurora.ui.BottomNavScreens.FAVORITES
import com.shkcodes.aurora.ui.BottomNavScreens.MENTIONS
import com.shkcodes.aurora.ui.BottomNavScreens.SETTINGS
import com.shkcodes.aurora.ui.BottomNavScreens.TWEETS
import com.shkcodes.aurora.ui.settings.SettingsScreen
import com.shkcodes.aurora.ui.timeline.TweetsTimeline
import com.shkcodes.aurora.util.TempScreen

@Composable
fun HomeScreen(primaryNavController: NavController) {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        BottomNavBar(navController = navController)
    }) {
        Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
            NavHost(
                navController = navController,
                startDestination = TWEETS.name,
            ) {
                composable(TWEETS.name) {
                    TweetsTimeline(primaryNavController)
                }
                composable(MENTIONS.name) {
                    TempScreen(content = MENTIONS.name)
                }
                composable(FAVORITES.name) {
                    TempScreen(content = FAVORITES.name)
                }
                composable(SETTINGS.name) {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.bottom_nav_indicator_height)
                .background(colors.primary)
        )
        BottomNavigation(backgroundColor = colors.background) {
            val currentRoute = currentRoute(navController)
            BottomNavScreens.values().forEach { screen ->
                BottomNavigationItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                    label = { Text(screen.name) },
                    selectedContentColor = colors.primary,
                    unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    alwaysShowLabel = false,
                    selected = currentRoute == screen.name,
                    onClick = {
                        navController.navigate(screen.name) {
                            popUpTo = navController.graph.startDestination
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.arguments?.getString(KEY_ROUTE)
}
