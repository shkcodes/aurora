package com.shkcodes.aurora.home

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.PreferencesService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.TimelineContract.State
import com.shkcodes.aurora.ui.timeline.TimelineItem
import com.shkcodes.aurora.ui.timeline.TimelineViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TimelineViewModelTest : BaseTest() {

    private val time = LocalDateTime.of(1993, 12, 23, 1, 0, 0)
    private val tweetTime = ZonedDateTime.of(time, ZoneId.systemDefault())

    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
        every { createdAt } returns tweetTime
    }

    private val timelineItem = TimelineItem(tweetEntity, emptyList())

    private val freshTweet =
        tweetEntity.copy(createdAt = tweetTime.withHour(2))
    private val freshTimelineItem = TimelineItem(freshTweet, emptyList())

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchTimelineTweets(null, null) } returns Result.Success(listOf(timelineItem))
        coEvery {
            fetchTimelineTweets(tweetEntity, null)
        } returns Result.Success(listOf(freshTimelineItem))
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

    private fun viewModel(): TimelineViewModel {
        return TimelineViewModel(
            testDispatcherProvider,
            userService,
            errorHandler,
            preferencesService,
            eventBus
        )
    }

    @Test
    fun `state updates correctly on init in case of success`() = test {
        val sut = viewModel()
        sut.testStates {
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(timelineItem)))
        }
    }

    @Test
    fun `state updates correctly on init in case of failure`() = test {
        coEvery {
            userService.fetchTimelineTweets(
                any(),
                any()
            )
        } returns Result.Failure(Exception())
        val sut = viewModel()
        sut.testStates {
            assert(expectItem() == State(true))
            assert(
                expectItem() == State(
                    isLoading = false,
                    errorMessage = "error",
                    isTerminalError = true
                )
            )
        }
    }

    @Test
    fun `state updates correctly on retry`() = test {
        coEvery {
            userService.fetchTimelineTweets(any(), any())
        } returns Result.Failure(Exception()) andThen Result.Success(listOf(timelineItem))
        val sut = viewModel()

        sut.testStates {

            assert(expectItem() == State(true))
            assert(
                expectItem() == State(
                    isLoading = false,
                    isTerminalError = true,
                    errorMessage = "error"
                )
            )

            sut.handleIntent(Retry)
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(timelineItem)))
        }
    }

    @Test
    fun `state updates correctly on init in case of paginated failure`() = test {
        coEvery { userService.fetchTimelineTweets(null, 23121993) } returns Result.Failure(
            Exception()
        )
        val sut = viewModel()

        sut.testStates {

            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(timelineItem)))

            sut.handleIntent(LoadNextPage)

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
        }
    }

    @Test
    fun `state updates correctly on refresh`() = test {
        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(timelineItem)))

            sut.handleIntent(Refresh)

            assert(expectItem() == State(true, listOf(timelineItem)))
            assert(
                expectItem() == State(
                    false,
                    listOf(freshTimelineItem, timelineItem),
                    newItems = listOf(freshTimelineItem)
                )
            )
        }
    }

    @Test
    fun `navigates to media viewer on media click`() = test {
        val sut = viewModel()
        sut.handleIntent(MediaClick(3, 3333))

        sut.testSideEffects {
            assert(expectItem() == SideEffect.DisplayScreen(MediaViewer(3, 3333)))
        }
    }

    @Test
    fun `state updates correctly on mark items as seen`() = test {
        val sut = viewModel()
        sut.testStates {
            expectItem()
            expectItem()
            sut.handleIntent(Refresh)
            expectItem()
            assert(expectItem().newItems.size == 1)

            sut.handleIntent(MarkItemsAsSeen)
            assert(expectItem().newItems.isEmpty())
        }
    }

    @Test
    fun `state updates correctly on scroll index change`() = test {
        val sut = viewModel()
        sut.testStates {
            expectItem()
            expectItem()
            sut.handleIntent(Refresh)
            expectItem()
            assert(expectItem().newItems.size == 1)

            sut.handleIntent(ScrollIndexChange(0))
            assert(expectItem().newItems.isEmpty())
        }
    }
}