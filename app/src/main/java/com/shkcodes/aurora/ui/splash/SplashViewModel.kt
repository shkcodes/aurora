package com.shkcodes.aurora.ui.splash

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceService: PreferenceService,
    private val authService: AuthService
) : ViewModel() {

    init {
        viewModelScope.launch {
            val destination = if (preferenceService.isLoggedIn) {
                authService.initClient()
                Screen.Home
            } else {
                Screen.Login
            }
            delay(SPLASH_TIMEOUT)
            onSideEffect(SideEffect.DisplayScreen(destination))
        }
    }
}
