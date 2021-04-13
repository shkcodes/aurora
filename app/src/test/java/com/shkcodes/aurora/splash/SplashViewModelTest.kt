package com.shkcodes.aurora.splash

import app.cash.turbine.test
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SplashViewModelTest : BaseTest() {

    private lateinit var viewModel: SplashViewModel
    private val authService: AuthService = mockk()

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = SplashViewModel(testDispatcherProvider, authService)
    }

    @Test
    fun `navigates to login after delay if user not logged in`() = testDispatcher.runBlockingTest {
        every { authService.isLoggedIn } returns false
        viewModel.getSideEffects().test {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen.LOGIN) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigates to home after delay if user logged in`() = testDispatcher.runBlockingTest {
        every { authService.isLoggedIn } returns true
        viewModel.getSideEffects().test {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen.HOME) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

}