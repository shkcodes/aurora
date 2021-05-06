package com.shkcodes.aurora.home

import app.cash.turbine.test
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Init
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.State
import com.shkcodes.aurora.ui.timeline.TimelineItem
import com.shkcodes.aurora.ui.timeline.TimelineItems
import com.shkcodes.aurora.ui.timeline.TimelineViewModel
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
class TimelineViewModelTest : BaseTest() {

    private lateinit var tweetsViewModel: TimelineViewModel

    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
    }

    private val timelineItem = TimelineItem(tweetEntity, emptyList())

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchTimelineTweets(any(), any()) } returns Result.Success(Unit)
        coEvery { getTimelineTweets() } returns flowOf(listOf(timelineItem))
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }

    @Before
    override fun setUp() {
        super.setUp()
        tweetsViewModel = TimelineViewModel(testDispatcherProvider, userService, errorHandler)
    }

    @Test
    fun `state updates correctly on init in case of success`() =
        tweetsViewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Content(true))
            assert(expectItem() == State.Content(false, listOf(timelineItem), false))
        })

    @Test
    fun `state updates correctly on init in case of failure`() {
        coEvery {
            userService.fetchTimelineTweets(
                any(),
                any()
            )
        } returns Result.Failure(Exception())
        coEvery { userService.getTimelineTweets() } returns flowOf(emptyList())
        tweetsViewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State.Content(true))
            assert(expectItem() == State.Error("error"))
        })
    }

    @Test
    fun `state updates correctly on retry`() {
        coEvery {
            userService.fetchTimelineTweets(any(), any())
        } returns Result.Failure(Exception())
        val cache = BroadcastChannel<TimelineItems>(2)
        coEvery { userService.getTimelineTweets() } returns cache.asFlow()
        val states = tweetsViewModel.getState()
        testDispatcher.runBlockingTest {
            states.test {
                tweetsViewModel.handleIntent(Init)

                assert(expectItem() == State.Content(true))
                assert(expectItem() == State.Error("error"))

                coEvery { userService.fetchTimelineTweets(any(), any()) } returns Result.Success(
                    Unit
                )

                tweetsViewModel.handleIntent(Retry)
                cache.send(listOf(timelineItem))

                assert(expectItem() == State.Content(true))
                assert(expectItem() == State.Content(false, listOf(timelineItem), false))

            }
        }
    }

    @Test
    fun `state updates correctly on init in case of paginated failure`() {
        coEvery { userService.fetchTimelineTweets(false, 23121993) } returns Result.Failure(
            Exception()
        )
        tweetsViewModel.test(
            intents = listOf(Init, LoadNextPage(State.Content(false, listOf(timelineItem), false))),
            states = {
                assert(expectItem() == State.Content(true))
                assert(expectItem() == State.Content(false, listOf(timelineItem), false))
                assert(
                    expectItem() == State.Content(
                        false,
                        listOf(timelineItem),
                        isPaginatedLoading = true
                    )
                )
                assert(
                    expectItem() == State.Content(
                        false,
                        listOf(timelineItem),
                        isPaginatedLoading = false,
                        isPaginatedError = true
                    )
                )
            })
    }

}