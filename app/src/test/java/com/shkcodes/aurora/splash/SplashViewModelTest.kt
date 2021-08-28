package com.shkcodes.aurora.splash

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.TwitterService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.splash.SplashContract.Constants.SPLASH_TIMEOUT
import com.shkcodes.aurora.ui.splash.SplashViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SplashViewModelTest : BaseTest() {

    private val preferenceService: PreferenceService = mockk {
        every { userCredentials } returns mockk()
    }
    private val twitterService: TwitterService = mockk()

    private fun viewModel() = SplashViewModel(testDispatcherProvider, preferenceService, twitterService)

    @Test
    fun `navigates to login after delay if user not logged in`() = testDispatcher.runBlockingTest {
        every { preferenceService.isLoggedIn } returns false
        viewModel().testSideEffects {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen.Login) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigates to home after delay if user logged in`() = testDispatcher.runBlockingTest {
        every { preferenceService.isLoggedIn } returns true
        viewModel().testSideEffects {
            advanceTimeBy(SPLASH_TIMEOUT)
            assert(SideEffect.DisplayScreen(Screen.Home) == expectItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

}