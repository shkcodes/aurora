package com.shkcodes.aurora.ui.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Init
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.State
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(viewModel: AuthViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init)
        launch {
            viewModel.getSideEffects().collect { handleActions(it, navController) }
        }
    }

    val state = viewModel.getState().collectAsState().value

    Scaffold {
        when (state) {
            is State.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is State.RequestToken -> {
                AndroidView(factory = {
                    WebView(it).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView,
                                url: String,
                                favicon: Bitmap?
                            ) {
                                if (url.startsWith(BuildConfig.CALLBACK_URL)) {
                                    viewModel.handleIntent(RequestAccessToken(state, url))
                                }
                            }
                        }
                        loadUrl(state.authorizationUrl)
                    }
                })
            }
        }
    }
}

private fun handleActions(sideEffect: SideEffect, navController: NavController) {
    when (sideEffect) {
        is SideEffect.DisplayScreen<*> -> {
            navController.navigate((sideEffect.screen as Screen).name) {
                popUpTo(Screen.LOGIN.name) { inclusive = true }
            }
        }
    }
}
