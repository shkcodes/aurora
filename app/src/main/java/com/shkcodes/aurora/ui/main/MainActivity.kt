package com.shkcodes.aurora.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shkcodes.aurora.theme.AuroraTheme
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthScreen
import com.shkcodes.aurora.ui.home.HomeScreen
import com.shkcodes.aurora.ui.login.LoginScreen
import com.shkcodes.aurora.ui.media.MediaViewer
import com.shkcodes.aurora.ui.profile.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuroraTheme {
                Scaffold {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.SPLASH.route
                    ) {
                        composable(Screen.SPLASH.route) {
                            HomeScreen(navController)
                        }
                        composable(Screen.LOGIN.route) {
                            LoginScreen(navController)
                        }
                        composable(Screen.AUTH.route) {
                            AuthScreen(navController)
                        }
                        composable(Screen.HOME.route) {
                            HomeScreen(navController)
                        }
                        composable(
                            Screen.MEDIA_VIEWER.route,
                            Screen.MEDIA_VIEWER.arguments
                        ) {
                            val index = it.arguments?.getString("index")!!.toInt()
                            val tweetId = it.arguments?.getString("tweetId")!!
                                .toLong() // TODO: investigate why we can't directly call getLong
                            MediaViewer(index, tweetId)
                        }
                        composable(
                            Screen.PROFILE.route,
                            Screen.PROFILE.arguments
                        ) {
                            val userHandle = it.arguments?.getString("userHandle")!!
                            ProfileScreen(userHandle, navController)
                        }
                    }
                }
            }
        }
    }
}
