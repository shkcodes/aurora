package com.shkcodes.aurora.ui.auth

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent
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

    init {
        fetchRequestToken()
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is RequestAccessToken -> {
                val authorization = (currentState as RequestToken).authorization
                val verifier = intent.authorizationResponse.split("=").last()
                currentState = Loading
                viewModelScope.launch {
                    runCatching { authService.login(authorization, verifier) }.onSuccess {
                        onSideEffect(SideEffect.DisplayScreen(Screen.Home))
                    }.onFailure {
                        currentState = Error(errorHandler.getErrorMessage(it))
                    }
                }
            }

            is Retry -> {
                currentState = Loading
                fetchRequestToken()
            }
        }
    }

    private fun fetchRequestToken() {
        viewModelScope.launch {
            runCatching { authService.getRequestToken() }
                .onSuccess {
                    currentState = RequestToken(authorization = it)
                }.onFailure {
                    currentState = Error(errorHandler.getErrorMessage(it))
                }
        }
    }
}
