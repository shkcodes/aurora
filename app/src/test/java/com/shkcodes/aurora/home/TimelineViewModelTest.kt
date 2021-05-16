package com.shkcodes.aurora.home

import app.cash.turbine.test
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.PreferencesService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Init
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.TimelineContract.State
import com.shkcodes.aurora.ui.timeline.TimelineItem
import com.shkcodes.aurora.ui.timeline.TimelineViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
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
        coEvery {
            fetchTimelineTweets(
                any(),
                any()
            )
        } returns Result.Success(listOf(timelineItem)) andThen Result.Success(
            listOf(
                timelineItem,
                timelineItem
            )
        )
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }
    private val events = MutableSharedFlow<Event>()
    private val eventBus: EventBus = mockk {
        every { getEvents() } returns events
    }
    private val preferencesService: PreferencesService = mockk {
        every { autoplayVideos } returns false
    }

    @Before
    override fun setUp() {
        super.setUp()
        tweetsViewModel =
            TimelineViewModel(
                testDispatcherProvider,
                userService,
                errorHandler,
                preferencesService,
                eventBus
            )
    }

    @Test
    fun `state updates correctly on init in case of success`() =
        tweetsViewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(timelineItem)))
        })

    @Test
    fun `state updates correctly on init in case of failure`() {
        coEvery {
            userService.fetchTimelineTweets(
                any(),
                any()
            )
        } returns Result.Failure(Exception())
        tweetsViewModel.test(intents = listOf(Init), states = {
            assert(expectItem() == State(true))
            assert(
                expectItem() == State(
                    isLoading = false,
                    errorMessage = "error",
                    isTerminalError = true
                )
            )
        })
    }

    @Test
    fun `state updates correctly on retry`() {
        coEvery {
            userService.fetchTimelineTweets(any(), any())
        } returns Result.Failure(Exception()) andThen Result.Success(listOf(timelineItem))
        val states = tweetsViewModel.getState()
        testDispatcher.runBlockingTest {
            states.test {
                tweetsViewModel.handleIntent(Init)
                assert(expectItem() == State(true))
                assert(
                    expectItem() == State(
                        isLoading = false,
                        isTerminalError = true,
                        errorMessage = "error"
                    )
                )

                tweetsViewModel.handleIntent(Retry)

                assert(expectItem() == State(true))
                assert(expectItem() == State(false, listOf(timelineItem)))

            }
        }
    }

    @Test
    fun `state updates correctly on init in case of paginated failure`() {
        coEvery { userService.fetchTimelineTweets(false, 23121993) } returns Result.Failure(
            Exception()
        )
        tweetsViewModel.test(
            intents = listOf(Init, LoadNextPage),
            states = {
                assert(expectItem() == State(true))
                assert(expectItem() == State(false, listOf(timelineItem)))
                assert(
                    expectItem() == State(
                        false,
                        listOf(timelineItem),
                        isPaginatedLoading = true
                    )
                )
                assert(
                    expectItem() == State(
                        false,
                        listOf(timelineItem),
                        isPaginatedLoading = false,
                        isPaginatedError = true
                    )
                )
            })
    }

    @Test
    fun `state updates correctly on refresh`() {
        tweetsViewModel.test(
            intents = listOf(Init, Refresh), states = {
                assert(expectItem() == State(true))
                assert(expectItem() == State(false, listOf(timelineItem)))
                assert(expectItem() == State(true, listOf(timelineItem)))
                assert(expectItem() == State(false, listOf(timelineItem, timelineItem)))
            })
    }

    @Test
    fun `navigates to media viewer on media click`() {
        tweetsViewModel.test(intents = listOf(MediaClick(3, 3333)),
            sideEffects = {
                assert(expectItem() == SideEffect.DisplayScreen(MediaViewer(3, 3333)))
            })
    }

}