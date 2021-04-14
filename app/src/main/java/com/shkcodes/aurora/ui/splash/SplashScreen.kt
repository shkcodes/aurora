package com.shkcodes.aurora.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.colors
import com.shkcodes.aurora.theme.typography
import com.shkcodes.aurora.ui.Screen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    val viewModel = hiltNavGraphViewModel<SplashViewModel>()

    LaunchedEffect(Unit) {
        launch {
            viewModel.getSideEffects().collect { handleActions(it, navController) }
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = typography.h3.copy(color = colors.primary),
                fontStyle = FontStyle.Italic,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

private fun handleActions(sideEffect: SideEffect, navController: NavController) {
    when (sideEffect) {
        is SideEffect.DisplayScreen<*> -> {
            navController.navigate((sideEffect.screen as Screen).name) {
                popUpTo(Screen.SPLASH.name) { inclusive = true }
            }
        }
    }
}
