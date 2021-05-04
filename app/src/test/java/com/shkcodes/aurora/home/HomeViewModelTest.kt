package com.shkcodes.aurora.home

import app.cash.turbine.test
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State
import com.shkcodes.aurora.ui.home.HomeViewModel
import com.shkcodes.aurora.ui.home.TimelineItem
import com.shkcodes.aurora.ui.home.TimelineItems
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class HomeViewModelTest : BaseTest() {

    private lateinit var viewModel: HomeViewModel

    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
    }

    private val timelineItem = TimelineItem(tweetEntity, emptyList())

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchTimelineTweets(any()) } returns Result.Success(Unit)
        coEvery { getTimelineTweets() } returns flowOf(listOf(timelineItem))
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
            assert(expectItem() == State.Content(listOf(timelineItem), false))
        })

    @Test
    fun `state updates correctly on init in case of failure`() {
        coEvery { userService.fetchTimelineTweets(any()) } returns Result.Failure(Exception())
        coEvery { userService.getTimelineTweets() } returns flowOf(emptyList())
        viewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Loading)
            assert(expectItem() == State.Error("error"))
        })
    }

    @Test
    fun `state updates correctly on retry`() {
        coEvery { userService.fetchTimelineTweets(any()) } returns Result.Failure(Exception())
        val cache = BroadcastChannel<TimelineItems>(2)
        coEvery { userService.getTimelineTweets() } returns cache.asFlow()
        val states = viewModel.getState()
        testDispatcher.runBlockingTest {
            states.test {
                viewModel.handleIntent(Init)

                assert(expectItem() == State.Loading)
                assert(expectItem() == State.Error("error"))

                coEvery { userService.fetchTimelineTweets(any()) } returns Result.Success(Unit)

                viewModel.handleIntent(Retry)
                cache.send(listOf(timelineItem))

                assert(expectItem() == State.Loading)
                assert(expectItem() == State.Content(listOf(timelineItem), false))

            }
        }
    }

    @Test
    fun `state updates correctly on init in case of paginated failure`() {
        coEvery { userService.fetchTimelineTweets(23121993) } returns Result.Failure(Exception())
        viewModel.test(
            intents = listOf(Init, LoadNextPage(State.Content(listOf(timelineItem), false))),
            states = {
                assert(expectItem() == State.Loading)
                assert(expectItem() == State.Content(listOf(timelineItem), false))
                assert(
                    expectItem() == State.Content(
                        listOf(timelineItem),
                        isLoadingNextPage = true
                    )
                )
                assert(
                    expectItem() == State.Content(
                        listOf(timelineItem),
                        isLoadingNextPage = false,
                        isPaginatedError = true
                    )
                )
            })
    }

}