package com.shkcodes.aurora.ui.auth

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Init
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Retry
import com.shkcodes.aurora.ui.auth.AuthContract.State.Error
import com.shkcodes.aurora.ui.auth.AuthContract.State.Loading
import com.shkcodes.aurora.ui.auth.AuthContract.State.RequestToken
import com.shkcodes.aurora.ui.auth.AuthContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val authService: AuthService,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                fetchRequestToken()
            }

            is RequestAccessToken -> {
                val token = intent.tokenState.token
                val verifier = intent.authorizationResponse.split("=").last()
                emitState { Loading }
                viewModelScope.launch {
                    authService.getAccessToken(verifier, token).evaluate({
                        authService.isLoggedIn = true
                        onSideEffect(SideEffect.DisplayScreen(Screen.HOME))
                    }, {
                        emitState { Error(errorHandler.getErrorMessage(it)) }
                    })
                }
            }

            is Retry -> {
                emitState { Loading }
                fetchRequestToken()
            }
        }
    }

    private fun fetchRequestToken() {
        viewModelScope.launch {
            authService.getRequestToken().evaluate({
                emitState { RequestToken(token = it) }
            }, {
                emitState { Error(errorHandler.getErrorMessage(it)) }
            })
        }
    }
}
