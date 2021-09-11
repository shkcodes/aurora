package com.shkcodes.aurora.media

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.State
import com.shkcodes.aurora.ui.media.MediaViewerViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MediaViewerViewModelTest : BaseTest() {

    private val media = mockk<MediaEntity>()

    private val userService = mockk<UserService> {
        coEvery { getMediaForTweet(any()) } returns listOf(media)
    }

    private fun viewModel() = MediaViewerViewModel(userService)

    @Test
    fun `updates state correctly on init`() = testDispatcher.runBlockingTest {
        val sut = viewModel()
        sut.handleIntent(Init(40, 600))

        sut.testStates {
            assert(State() == expectItem())
            assert(State(40, listOf(media)) == expectItem())
        }
    }
}