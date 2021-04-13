package com.shkcodes.aurora.login

import app.cash.turbine.test
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.login.LoginContract.Intent.ShowAuthScreen
import com.shkcodes.aurora.ui.login.LoginViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LoginViewModelTest : BaseTest() {

    private lateinit var viewModel: LoginViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = LoginViewModel(testDispatcherProvider)
    }

    @Test
    fun `navigates to auth screen on show auth screen intent`() = testDispatcher.runBlockingTest {
            viewModel.getSideEffects().test {
                viewModel.handleIntent(ShowAuthScreen)
                assert(SideEffect.DisplayScreen(Screen.AUTH) == expectItem())
            }
        }

}