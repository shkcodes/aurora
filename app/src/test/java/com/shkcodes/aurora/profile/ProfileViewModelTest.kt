package com.shkcodes.aurora.profile

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.State
import com.shkcodes.aurora.ui.profile.ProfileViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ProfileViewModelTest : BaseTest() {
    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
    }

    private val tweetItem = TweetItem(tweetEntity, emptyList())

    private val user = mockk<User>() {
        every { id } returns 23121993
        every { name } returns "M&M"
    }

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchUserProfile(any()) } returns Result.Success(user)
        coEvery { fetchUserTweets() } returns Result.Success(listOf(tweetItem))
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }

    private fun viewModel() = ProfileViewModel(testDispatcherProvider, userService, errorHandler)

    @Test
    fun `updates state successfully on user profile fetch success`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                sut.handleIntent(Init("@@"))
                assert(expectItem() == State())
                assert(
                    expectItem() == State(
                        isLoading = false,
                        user = user,
                        items = listOf(tweetItem)
                    )
                )
            }
        }

    @Test
    fun `updates state successfully on user profile fetch failure`() =
        testDispatcher.runBlockingTest {
            coEvery { userService.fetchUserProfile(any()) } returns Result.Failure(Exception())
            val sut = viewModel()
            sut.testStates {
                sut.handleIntent(Init("@@"))
                assert(expectItem() == State())
                assert(
                    expectItem() == State(
                        isLoading = false,
                        isTerminalError = true,
                        errorMessage = "error"
                    )
                )
            }
        }

    @Test
    fun `updates state successfully on retry`() = testDispatcher.runBlockingTest {
        val sut = viewModel()
        sut.testStates {
            coEvery { userService.fetchUserProfile(any()) } returns Result.Failure(Exception())
            sut.handleIntent(Init("@@"))
            assert(expectItem() == State())
            assert(
                expectItem() == State(
                    isLoading = false,
                    isTerminalError = true,
                    errorMessage = "error"
                )
            )
            coEvery { userService.fetchUserProfile(any()) } returns Result.Success(user)

            sut.handleIntent(Retry("@@"))
                assert(
                    expectItem() == State(
                        isLoading = true,
                        isTerminalError = false,
                        errorMessage = ""
                    )
                )
                assert(
                    expectItem() == State(
                        isLoading = false,
                        user = user,
                        items = listOf(tweetItem)
                    )
                )
        }
    }

}