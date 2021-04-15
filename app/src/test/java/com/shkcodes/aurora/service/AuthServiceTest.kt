package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.AuthApi
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.cache.PreferenceManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AuthServiceTest : BaseTest() {

    lateinit var authService: AuthService

    private val authApi = mockk<AuthApi>(relaxUnitFun = true) {
        coEvery { getRequestToken() } returns "token=secret&data=some other data"
    }
    private val preferenceManager = mockk<PreferenceManager>()


    @Before
    override fun setUp() {
        super.setUp()
        authService = AuthService(authApi, preferenceManager)
    }

    @Test
    fun `get request token returns correctly`() = testDispatcher.runBlockingTest {
        val result = authService.getRequestToken()
        assert(result == Result.Success("secret"))
    }

    @Test
    fun `get access token returns correctly`() = testDispatcher.runBlockingTest {
        val result = authService.getAccessToken("","")
        assert(result is Result.Success)
    }

}