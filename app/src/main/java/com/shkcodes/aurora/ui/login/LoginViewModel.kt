package com.shkcodes.aurora.ui.login

import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.login.LoginContract.Intent
import com.shkcodes.aurora.ui.login.LoginContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.ShowAuthScreen -> {
                onSideEffect(SideEffect.DisplayScreen(Screen.AUTH))
            }
        }
    }
}
