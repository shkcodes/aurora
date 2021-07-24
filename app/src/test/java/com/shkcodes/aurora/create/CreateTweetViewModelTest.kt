package com.shkcodes.aurora.create

import android.net.Uri
import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.base.SideEffect.DisplayScreen
import com.shkcodes.aurora.base.StringId
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.ATTACHMENT_TYPE_IMAGE
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.ATTACHMENT_TYPE_VIDEO
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.MediaSelectionError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.State
import com.shkcodes.aurora.ui.create.CreateTweetViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.time.ExperimentalTime


@ExperimentalTime
class CreateTweetViewModelTest : BaseTest() {

    private val userService: UserService = mockk(relaxUnitFun = true) {
        coEvery { postTweet(any()) } returns Result.Success(Unit)
    }
    private val stringProvider = object : StringProvider {
        override fun getString(stringId: StringId): String {
            return stringId.name
        }
    }

    private fun viewModel() = CreateTweetViewModel(testDispatcherProvider, userService, stringProvider)


    @Test
    fun `updates state successfully content change`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                sut.handleIntent(ContentChange("hey!"))
                assert(expectItem() == State())
                assert(expectItem() == State(content = "hey!"))
            }
        }

    @Test
    fun `updates state successfully on post tweet`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                sut.handleIntent(ContentChange("hey!"))
                sut.handleIntent(PostTweet)
                assert(expectItem() == State())
                assert(expectItem() == State(content = "hey!"))
                assert(expectItem() == State(isLoading = true, content = "hey!"))
            }
            sut.testSideEffects {
                sut.handleIntent(ContentChange("hey!"))
                sut.handleIntent(PostTweet)
                assert(expectItem() == DisplayScreen(Previous))
            }
        }

    @Test
    fun `does nothing if no attachments selected`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(emptyList(), emptySet()))
                expectNoEvents()
            }
        }

    @Test
    fun `shows error in case of too many images`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..5).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(ATTACHMENT_TYPE_IMAGE)))
                assert(expectItem() == SideEffect.Action(MediaSelectionError(StringId.TOO_MANY_IMAGES.name)))
            }
        }

    @Test
    fun `shows error in case of too many videos`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(ATTACHMENT_TYPE_VIDEO)))
                assert(expectItem() == SideEffect.Action(MediaSelectionError(StringId.TOO_MANY_VIDEOS.name)))
            }
        }

    @Test
    fun `shows error in case of multiple types`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(ATTACHMENT_TYPE_VIDEO, ATTACHMENT_TYPE_IMAGE)))
                assert(expectItem() == SideEffect.Action(MediaSelectionError(StringId.MULTIPLE_TYPES.name)))
            }
        }

    @Test
    fun `shows error in case of unsupported attachment`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf("boom")))
                assert(expectItem() == SideEffect.Action(MediaSelectionError(StringId.UNSUPPORTED_ATTACHMENT.name)))
            }
        }

    @Test
    fun `updates state successfully on valid media selection`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(ATTACHMENT_TYPE_IMAGE)))
                assert(expectItem().mediaAttachments.size == 2)
            }
        }

}