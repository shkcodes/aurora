package com.shkcodes.aurora.auth

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Init
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Retry
import com.shkcodes.aurora.ui.auth.AuthContract.State
import com.shkcodes.aurora.ui.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AuthViewModelTest : BaseTest() {

    private lateinit var viewModel: AuthViewModel
    private val authService: AuthService = mockk(relaxUnitFun = true) {
        coEvery { getRequestToken() } returns Result.Success("token")
        coEvery { getAccessToken(any(), any()) } returns Result.Success(Unit)
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }


    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AuthViewModel(testDispatcherProvider, authService, errorHandler)
    }

    @Test
    fun `state update correctly on init in case of success`() =
        viewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))
        })

    @Test
    fun `state update correctly on init in case of error`() {
        coEvery { authService.getRequestToken() } returns Result.Failure(Exception())
        viewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        })
    }


    @Test
    fun `state update correctly on request access token in case of success`() =
        viewModel.test(intents = listOf(
            Init,
            RequestAccessToken(
                State.RequestToken("token"),
                "verifier=verifierer"
            )
        ), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))
            assert(expectItem() == State.Loading)
        }, sideEffects = {
            assert(SideEffect.DisplayScreen(Screen.HOME) == expectItem())
        })

    @Test
    fun `state update correctly on request access token in case of failure`() {
        coEvery { authService.getAccessToken(any(), any()) } returns Result.Failure(Exception())
        viewModel.test(intents = listOf(
            Init,
            RequestAccessToken(
                State.RequestToken("token"),
                "verifier=verifierer"
            )
        ), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.RequestToken("token"))
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        })
    }

    @Test
    fun `state update correctly on retry event`() {
        coEvery { authService.getRequestToken() } returns Result.Failure(Exception())
        viewModel.test(intents = listOf(Init, Retry), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        })
    }


}