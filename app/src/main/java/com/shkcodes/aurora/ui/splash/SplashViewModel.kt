package com.shkcodes.aurora.ui.splash

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen2
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val authService: AuthService,
    private val userService: UserService
) : ViewModel() {

    init {
        viewModelScope.launch {
            userService.flushTweetsCache()
            delay(SPLASH_TIMEOUT)
            val destination = if (authService.isLoggedIn) {
                Screen2.Home
            } else {
                Screen2.Login
            }
            onSideEffect(SideEffect.DisplayScreen(destination))
        }
    }
}
