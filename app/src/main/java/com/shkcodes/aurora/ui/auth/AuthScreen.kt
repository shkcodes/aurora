package com.shkcodes.aurora.ui.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Init
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Retry
import com.shkcodes.aurora.ui.auth.AuthContract.State
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AuthScreen(navController: NavController) {
    val viewModel = hiltNavGraphViewModel<AuthViewModel>()

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
            is State.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        Modifier.padding(Dimens.keyline_1.dp),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.handleIntent(Retry) },
                        modifier = Modifier.padding(Dimens.space.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.retry).toUpperCase(Locale.ROOT),
                            style = MaterialTheme.typography.button
                        )
                    }
                }
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
