package com.shkcodes.aurora.splash

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen2
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SplashViewModelTest : BaseTest() {

    private val authService: AuthService = mockk()
    private val userService: UserService = mockk()

    private fun viewModel() = SplashViewModel(testDispatcherProvider, authService, userService)

    @Test
    fun `navigates to login after delay if user not logged in`() = testDispatcher.runBlockingTest {
        every { authService.isLoggedIn } returns false
        viewModel().testSideEffects {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen2.Login) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigates to home after delay if user logged in`() = testDispatcher.runBlockingTest {
        every { authService.isLoggedIn } returns true
        viewModel().testSideEffects {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen2.Home) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

}