package com.shkcodes.aurora.ui.splash

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val authService: AuthService
) : ViewModel() {

    init {
        viewModelScope.launch {
            delay(SPLASH_TIMEOUT)
            val destination = if (authService.isLoggedIn) {
                Screen.HOME
            } else {
                Screen.LOGIN
            }
            onSideEffect(SideEffect.DisplayScreen(destination))
        }
    }
}
