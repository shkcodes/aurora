package com.shkcodes.aurora.auth

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.AuthService
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Init
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.State
import com.shkcodes.aurora.ui.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AuthViewModelTest : BaseTest() {

    private lateinit var viewModel: AuthViewModel
    private val authService: AuthService = mockk(relaxUnitFun = true) {
        coEvery { getRequestToken() } returns "token"
    }

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AuthViewModel(testDispatcherProvider, authService)
    }

    @Test
    fun `state update correctly on init`() = viewModel.test(intents = listOf(Init), states = {
        assert(expectItem() == State.Loading)
        assert(expectItem() == State.RequestToken("token"))
    })


    @Test
    fun `state update correctly on request access token`() = viewModel.test(intents = listOf(
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


}