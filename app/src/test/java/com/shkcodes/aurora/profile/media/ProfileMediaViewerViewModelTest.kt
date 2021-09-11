package com.shkcodes.aurora.profile.media

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.media.ProfileMediaDto
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.PageChange
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.State
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime


@ExperimentalTime
class ProfileMediaViewerViewModelTest : BaseTest() {
    private val tweetEntity = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "Shouldn't have tweeted this"
        every { retweetCount } returns -9
        every { favoriteCount } returns -60
    }

    private val media = mockk<MediaEntity> {
        every { id } returns -1
        every { isAnimatedMedia } returns false
    }

    private val tweetItem = TweetItem(tweetEntity, listOf(media))

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { getCachedTweetsForUser(any()) } returns listOf(tweetItem)
    }


    private fun viewModel() = ProfileMediaViewerViewModel(userService)

    @Test
    fun `updates state successfully on init`() = testDispatcher.runBlockingTest {
        val sut = viewModel()
        sut.testStates {
            sut.handleIntent(Init("@@", 3))
            assert(expectItem() == State())
            assert(
                expectItem() == State(
                    media = listOf(ProfileMediaDto(media, -9, -60)),
                    currentIndex = 3
                )
            )
        }
    }

    @Test
    fun `updates state successfully on page change`() = testDispatcher.runBlockingTest {
        val sut = viewModel()
        sut.testStates {
            sut.handleIntent(Init("@@", 3))
            expectItem()
            expectItem()
            sut.handleIntent(PageChange(50))
            assert(expectItem().currentIndex == 50)
        }
    }

}