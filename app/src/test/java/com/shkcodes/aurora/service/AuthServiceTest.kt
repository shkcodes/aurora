package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.AuthApi
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.cache.PreferenceManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class AuthServiceTest : BaseTest() {

    private val authApi = mockk<AuthApi>(relaxUnitFun = true) {
        coEvery { getRequestToken() } returns "token=secret&data=some other data"
    }
    private val preferenceManager = mockk<PreferenceManager>()

    private fun sut() = AuthService(authApi, preferenceManager)

    @Test
    fun `get request token returns correctly`() = testDispatcher.runBlockingTest {
        val result = sut().getRequestToken()
        assert(result == "secret")
    }

    @Test
    fun `get access token returns correctly`() = testDispatcher.runBlockingTest {
        val result = runCatching { sut().getAccessToken("", "") }
        assert(result.isSuccess)
    }

}