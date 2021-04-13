package com.shkcodes.aurora.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shkcodes.aurora.theme.AuroraTheme
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthScreen
import com.shkcodes.aurora.ui.home.HomeScreen
import com.shkcodes.aurora.ui.login.LoginScreen
import com.shkcodes.aurora.ui.splash.SplashScreen
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
                        SplashScreen(
                            viewModel(
                                factory = HiltViewModelFactory(
                                    this@MainActivity,
                                    it
                                )
                            ), navController
                        )
                    }
                    composable(Screen.LOGIN.name) {
                        LoginScreen(
                            viewModel(
                                factory = HiltViewModelFactory(
                                    this@MainActivity,
                                    it
                                )
                            ), navController
                        )
                    }
                    composable(Screen.AUTH.name) {
                        AuthScreen(
                            viewModel(
                                factory = HiltViewModelFactory(
                                    this@MainActivity,
                                    it
                                )
                            ), navController
                        )
                    }
                    composable(Screen.HOME.name) {
                        HomeScreen()
                    }
                }
            }
        }
    }
}
