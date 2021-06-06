package com.shkcodes.aurora.profile

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.TweetContentClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.MediaClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.UserProfile
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

    private val freshTweetItem = TweetItem(tweetEntity, emptyList())

    private val user = mockk<User>() {
        every { id } returns 23121993
        every { name } returns "M&M"
        every { screenName } returns "@@"
    }

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { fetchUserProfile(any()) } returns Result.Success(user)
        coEvery { fetchUserTweets(any()) } returns Result.Success(listOf(tweetItem))
        coEvery { fetchUserTweets("@@", 23121993) } returns Result.Success(
            listOf(
                tweetItem,
                freshTweetItem
            )
        )
    }
    private val errorHandler: ErrorHandler = mockk {
        every { getErrorMessage(any()) } returns "error"
    }
    private val preferenceService: PreferenceService = mockk {
        every { autoplayVideos } returns true
    }

    private fun viewModel() =
        ProfileViewModel(testDispatcherProvider, userService, errorHandler, preferenceService)

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
                        tweets = listOf(tweetItem),
                        autoplayVideos = true
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
                    tweets = listOf(tweetItem),
                    autoplayVideos = true
                )
            )
        }
    }

    @Test
    fun `state updates correctly in case of paginated failure`() = test {
        coEvery { userService.fetchUserTweets("@@", 23121993) } returns Result.Failure(
            Exception()
        )
        val sut = viewModel()

        sut.testStates {
            sut.handleIntent(Init("@@"))

            assert(expectItem() == State(true))
            assert(expectItem() == State(false, user, listOf(tweetItem), autoplayVideos = true))

            sut.handleIntent(LoadNextPage)

            assert(
                expectItem() == State(
                    false,
                    user,
                    listOf(tweetItem),
                    isPaginatedLoading = true,
                    autoplayVideos = true
                )
            )
            assert(
                expectItem() == State(
                    false,
                    user,
                    listOf(tweetItem),
                    isPaginatedLoading = false,
                    isPaginatedError = true,
                    autoplayVideos = true
                )
            )
        }
    }

    @Test
    fun `state updates correctly in case of paginated success`() = test {
        val sut = viewModel()

        sut.testStates {
            sut.handleIntent(Init("@@"))

            assert(expectItem() == State(true))
            assert(expectItem() == State(false, user, listOf(tweetItem), autoplayVideos = true))

            sut.handleIntent(LoadNextPage)

            assert(
                expectItem() == State(
                    false,
                    user,
                    listOf(tweetItem),
                    isPaginatedLoading = true,
                    autoplayVideos = true
                )
            )
            assert(
                expectItem() == State(
                    false,
                    user,
                    listOf(tweetItem, freshTweetItem),
                    isPaginatedLoading = false,
                    autoplayVideos = true
                )
            )
        }
    }

    @Test
    fun `navigates to media viewer on media click`() = test {
        val sut = viewModel()
        sut.handleIntent(MediaClick(3, 3333))

        sut.testSideEffects {
            assert(
                expectItem() == SideEffect.DisplayScreen(
                    MediaViewer(
                        3,
                        3333
                    )
                )
            )
        }
    }

    @Test
    fun `opens url on url click`() = test {
        val sut = viewModel()
        sut.testSideEffects {
            val url = "https://www.www.com"
            sut.handleIntent(TweetContentClick(url))

            assert(
                expectItem() == SideEffect.Action(
                    OpenUrl(
                        url
                    )
                )
            )
        }
    }

    @Test
    fun `opens profile screen on profile click`() = test {
        val sut = viewModel()
        sut.testSideEffects {
            val userHandle = "@don't_@_me"
            sut.handleIntent(TweetContentClick(userHandle))
            assert(
                expectItem() == SideEffect.DisplayScreen(
                    UserProfile(
                        userHandle.substring(1)
                    )
                )
            )
        }
    }

    @Test
    fun `does nothing if data already fetched`() = test {
        val sut = viewModel()

        sut.testStates {
            sut.handleIntent(Init("@@"))
            assert(expectItem() == State())
            assert(expectItem() == State(false, user, listOf(tweetItem), autoplayVideos = true))
            sut.handleIntent(Init("@@"))
            expectNoEvents()
        }
    }

}