package com.shkcodes.aurora.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.BottomNavScreens
import com.shkcodes.aurora.ui.BottomNavScreens.FAVORITES
import com.shkcodes.aurora.ui.BottomNavScreens.MENTIONS
import com.shkcodes.aurora.ui.BottomNavScreens.SETTINGS
import com.shkcodes.aurora.ui.BottomNavScreens.TWEETS
import com.shkcodes.aurora.ui.home.HomeContract.State
import com.shkcodes.aurora.ui.settings.SettingsScreen
import com.shkcodes.aurora.ui.timeline.HomeTimeline
import com.shkcodes.aurora.util.TempScreen

@Composable
fun HomeScreen(primaryNavController: NavController) {
    val navController = rememberNavController()
    val viewModel = hiltNavGraphViewModel<HomeViewModel>()
    val state = viewModel.composableState()

    Scaffold(bottomBar = {
        BottomNavBar(navController = navController, state = state)
    }) {
        Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
            NavHost(
                navController = navController,
                startDestination = TWEETS.name,
            ) {
                composable(TWEETS.name) {
                    HomeTimeline(primaryNavController)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomNavBar(navController: NavController, state: State) {
    val enterAnimation = expandVertically(
        animationSpec = tween(),
        expandFrom = Alignment.Bottom
    )
    val exitAnimation = shrinkVertically(
        animationSpec = tween(),
        shrinkTowards = Alignment.Bottom,
    )
    Column {
        Box {
            this@Column.AnimatedVisibility(
                state.isLoading,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            this@Column.AnimatedVisibility(
                visible = !state.isLoading,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.bottom_nav_indicator_height)
                        .background(colors.primary)
                )
            }
        }
        BottomNavigation(backgroundColor = colors.background) {
            val currentRoute = currentRoute(navController)
            BottomNavScreens.values().forEach { screen ->
                BottomNavigationItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = screen.name) },
                    selectedContentColor = colors.primary,
                    unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    selected = currentRoute == screen.name,
                    onClick = {
                        navController.navigate(screen.name) {
                            popUpTo(navController.graph.startDestinationId)
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
    return navBackStackEntry?.destination?.route
}
