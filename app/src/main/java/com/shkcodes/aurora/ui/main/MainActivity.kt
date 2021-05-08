package com.shkcodes.aurora.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.shkcodes.aurora.theme.AuroraTheme
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthScreen
import com.shkcodes.aurora.ui.home.HomeScreen
import com.shkcodes.aurora.ui.login.LoginScreen
import com.shkcodes.aurora.ui.media.MediaViewer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuroraTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.SPLASH.name
                ) {
                    composable(Screen.SPLASH.name) {
                        HomeScreen(navController)
                    }
                    composable(Screen.LOGIN.name) {
                        LoginScreen(navController)
                    }
                    composable(Screen.AUTH.name) {
                        AuthScreen(navController)
                    }
                    composable(Screen.HOME.name) {
                        HomeScreen(navController)
                    }
                    composable(
                        "${Screen.MEDIA_VIEWER.name}/{tweetId}/{index}",
                        arguments = listOf(
                            navArgument("tweetId") { type = NavType.LongType },
                            navArgument("index") { type = NavType.IntType })
                    ) {
                        val index = it.arguments?.getInt("index")!!
                        val tweetId = it.arguments?.getLong("tweetId")!!
                        MediaViewer(index, tweetId)
                    }
                }
            }
        }
    }
}
