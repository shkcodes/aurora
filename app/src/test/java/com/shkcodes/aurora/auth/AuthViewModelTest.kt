package com.shkcodes.aurora.auth

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Retry
import com.shkcodes.aurora.ui.auth.AuthContract.State
import com.shkcodes.aurora.ui.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AuthViewModelTest : BaseTest() {

    private val authService: AuthService = mockk(relaxUnitFun = true) {
        coEvery { getRequestToken() } returns "token"
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }

    private fun viewModel(): AuthViewModel {
        return AuthViewModel(testDispatcherProvider, authService, errorHandler)
    }

    @Test
    fun `state updates correctly on init in case of success`() = test {
        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))
        }
    }

    @Test
    fun `state updates correctly on init in case of error`() = test {
        coEvery { authService.getRequestToken() } throws Exception()
        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        }
    }


    @Test
    fun `state updates correctly on request access token in case of success`() = test {
        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))

            sut.handleIntent(RequestAccessToken("verifier=verifierer"))

            assert(expectItem() == State.Loading)
        }

        sut.testSideEffects {
            assert(SideEffect.DisplayScreen(Screen.Home) == expectItem())
        }
    }

    @Test
    fun `state updates correctly on request access token in case of failure`() = test {
        coEvery { authService.getAccessToken(any(), any()) } throws Exception()

        val sut = viewModel()


        sut.testStates {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))

            sut.handleIntent(
                RequestAccessToken("verifier=verifierer")
            )

            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        }
    }

    @Test
    fun `state updates correctly on retry event`() = test {
        coEvery { authService.getRequestToken() } throws Exception()

        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))

            sut.handleIntent(Retry)

            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        }
    }


}