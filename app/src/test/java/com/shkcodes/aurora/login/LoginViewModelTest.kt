package com.shkcodes.aurora.login

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.login.LoginContract.Intent.ShowAuthScreen
import com.shkcodes.aurora.ui.login.LoginViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LoginViewModelTest : BaseTest() {

    private fun viewModel() = LoginViewModel(testDispatcherProvider)

    @Test
    fun `navigates to auth screen on show auth screen intent`() = testDispatcher.runBlockingTest {
        val sut = viewModel()
        sut.handleIntent(ShowAuthScreen)

        sut.testSideEffects {
            assert(SideEffect.DisplayScreen(Screen.Auth) == expectItem())
        }
    }
}