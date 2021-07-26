package com.shkcodes.aurora.home

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.Event.AutoplayVideosToggled
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.MediaViewer
import com.shkcodes.aurora.ui.Screen.UserProfile
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.State
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.HomeTimelineViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItem
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
class HomeTimelineViewModelTest : BaseTest() {

    private val time = LocalDateTime.of(1993, 12, 23, 1, 0, 0)
    private val tweetTime = ZonedDateTime.of(time, ZoneId.systemDefault())

    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
        every { createdAt } returns tweetTime
    }

    private val tweetItem = TweetItem(tweetEntity, emptyList())

    private val freshTweet =
        tweetEntity.copy(createdAt = tweetTime.withHour(2))
    private val freshTweetItem = TweetItem(freshTweet, emptyList())

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchTimelineTweets(null, null) } returns listOf(tweetItem)
        coEvery {
            fetchTimelineTweets(tweetEntity, null)
        } returns listOf(freshTweetItem)
        coEvery {
            fetchTimelineTweets(null, 23121993)
        } returns listOf(tweetItem, freshTweetItem)
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }
    private val events = MutableSharedFlow<Event>()
    private val eventBus: EventBus = mockk {
        every { getEvents() } returns events
    }
    private val preferenceService: PreferenceService = mockk {
        every { autoplayVideos } returns false
    }

    private fun viewModel(): HomeTimelineViewModel {
        return HomeTimelineViewModel(
            testDispatcherProvider,
            userService,
            errorHandler,
            preferenceService,
            eventBus
        )
    }

    @Test
    fun `state updates correctly on init in case of success`() = test {
        val sut = viewModel()
        sut.testStates {
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(tweetItem)))
        }
    }

    @Test
    fun `state updates correctly on init in case of failure`() = test {
        coEvery {
            userService.fetchTimelineTweets(
                any(),
                any()
            )
        } throws Exception()
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
        } throws Exception() andThen listOf(tweetItem)
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
            assert(expectItem() == State(false, listOf(tweetItem)))
        }
    }

    @Test
    fun `state updates correctly in case of paginated failure`() = test {
        coEvery { userService.fetchTimelineTweets(null, 23121993) } throws Exception()
        val sut = viewModel()

        sut.testStates {

            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(tweetItem)))

            sut.handleIntent(LoadNextPage)

            assert(
                expectItem() == State(
                    false,
                    listOf(tweetItem),
                    isPaginatedLoading = true
                )
            )
            assert(
                expectItem() == State(
                    false,
                    listOf(tweetItem),
                    isPaginatedLoading = false,
                    isPaginatedError = true
                )
            )
        }
    }

    @Test
    fun `state updates correctly in case of paginated success`() = test {
        val sut = viewModel()

        sut.testStates {

            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(tweetItem)))

            sut.handleIntent(LoadNextPage)

            assert(
                expectItem() == State(
                    false,
                    listOf(tweetItem),
                    isPaginatedLoading = true
                )
            )
            assert(
                expectItem() == State(
                    false,
                    listOf(tweetItem, freshTweetItem),
                    isPaginatedLoading = false,
                )
            )
        }
    }

    @Test
    fun `state updates correctly on refresh`() = test {
        val sut = viewModel()

        sut.testStates {
            assert(expectItem() == State(true))
            assert(expectItem() == State(false, listOf(tweetItem)))

            sut.handleIntent(Refresh)

            assert(expectItem() == State(true, listOf(tweetItem)))
            assert(
                expectItem() == State(
                    false,
                    listOf(freshTweetItem, tweetItem),
                    newTweets = listOf(freshTweetItem)
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
            assert(expectItem().newTweets.size == 1)

            sut.handleIntent(MarkItemsAsSeen)
            assert(expectItem().newTweets.isEmpty())
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
            assert(expectItem().newTweets.size == 1)

            sut.handleIntent(ScrollIndexChange(0))
            assert(expectItem().newTweets.isEmpty())
        }
    }

    @Test
    fun `state updates correctly on autoplay video toggle`() = test {
        every { preferenceService.autoplayVideos } returns false andThen true andThen false
        val sut = viewModel()
        sut.testStates {
            expectItem()
            expectItem()
            events.emit(AutoplayVideosToggled)
            assert(expectItem().autoplayVideos)
            events.emit(AutoplayVideosToggled)
            assert(!expectItem().autoplayVideos)
        }
    }

    @Test
    fun `opens url on url click`() = test {
        val sut = viewModel()
        sut.testSideEffects {
            val url = "https://www.www.com"
            sut.handleIntent(AnnotatedContentClick(url))
            assert(expectItem() == SideEffect.Action(OpenUrl(url)))
        }
    }

    @Test
    fun `opens profile screen on profile click`() = test {
        val sut = viewModel()
        sut.testSideEffects {
            val userHandle = "@don't_@_me"
            sut.handleIntent(AnnotatedContentClick(userHandle))
            assert(expectItem() == SideEffect.DisplayScreen(UserProfile(userHandle.substring(1))))
        }
    }
}