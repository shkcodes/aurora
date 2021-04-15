package com.shkcodes.aurora.home

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State
import com.shkcodes.aurora.ui.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class HomeViewModelTest : BaseTest() {

    private lateinit var viewModel: HomeViewModel

    private val tweet = mockk<TweetEntity> {
        every { tweetId } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
    }
    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { getTimelineTweets() } returns Result.Success(listOf(tweet))
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = HomeViewModel(testDispatcherProvider, userService, errorHandler)
    }

    @Test
    fun `state updates correctly on init in case of success`() =
        viewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Content(listOf(tweet)))
        })

    @Test
    fun `state updates correctly on init in case of failure`() {
        coEvery { userService.getTimelineTweets() } returns Result.Failure(Exception())
        viewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        })
    }

    @Test
    fun `state updates correctly on retry`() =
        viewModel.test(intents = listOf(Init, Retry), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Content(listOf(tweet)))
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Content(listOf(tweet)))
        })
}