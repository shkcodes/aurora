package com.shkcodes.aurora.create

import android.net.Uri
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.base.SideEffect.DisplayScreen
import com.shkcodes.aurora.base.StringId
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.service.FileService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.AttachmentType
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.AttachmentError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.GifSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveAttachment
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveImage
import com.shkcodes.aurora.ui.create.CreateTweetContract.State
import com.shkcodes.aurora.ui.create.CreateTweetViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.File
import kotlin.time.ExperimentalTime


@ExperimentalTime
class CreateTweetViewModelTest : BaseTest() {

    private val userService: UserService = mockk(relaxUnitFun = true)
    private val fileService: FileService = mockk {
        every { getFile(any()) } returns File("nice")
        coEvery { downloadGif(any(), any()) } returns mockk()
    }
    private val stringProvider = object : StringProvider {
        override fun getString(stringId: StringId): String {
            return stringId.name
        }
    }

    private fun viewModel() =
        CreateTweetViewModel(userService, fileService, stringProvider)


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
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                assert(expectItem() == SideEffect.Action(AttachmentError(StringId.TOO_MANY_IMAGES.name)))
            }
        }

    @Test
    fun `shows error in case of too many videos`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.VIDEO)))
                assert(expectItem() == SideEffect.Action(AttachmentError(StringId.TOO_MANY_VIDEOS.name)))
            }
        }

    @Test
    fun `shows error in case of multiple types`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE, AttachmentType.VIDEO)))
                assert(expectItem() == SideEffect.Action(AttachmentError(StringId.MULTIPLE_TYPES.name)))
            }
        }

    @Test
    fun `shows error in case of unsupported attachment`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..2).map { mockk<Uri>() }
            sut.testSideEffects {
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.OTHER)))
                assert(expectItem() == SideEffect.Action(AttachmentError(StringId.UNSUPPORTED_ATTACHMENT.name)))
            }
        }

    @Test
    fun `updates state successfully on valid media selection`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                val state = expectItem()
                assert(state.mediaAttachments.size == 2 && state.hasImageAttachments)
            }
        }

    @Test
    fun `updates state successfully on image removal`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                val state = expectItem()
                assert(state.mediaAttachments.size == 2 && state.hasImageAttachments)
                sut.handleIntent(RemoveImage(uris.first()))
                assert(expectItem().mediaAttachments.size == 1)
            }
        }

    @Test
    fun `updates state successfully on video removal`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(listOf(mockk()), setOf(AttachmentType.VIDEO)))
                expectItem()
                sut.handleIntent(RemoveAttachment)
                val state = expectItem()
                assert(state.mediaAttachments.isEmpty() && state.attachmentType == null)
            }
        }

    @Test
    fun `updates state correctly on adding additional images`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                val state = expectItem()
                assert(state.mediaAttachments.size == 2 && state.hasImageAttachments)
                sut.handleIntent(MediaSelected(listOf(mockk()), setOf(AttachmentType.IMAGE)))
                val stateFinal = expectItem()
                assert(stateFinal.mediaAttachments.size == 3 && stateFinal.hasImageAttachments)
            }
        }

    @Test
    fun `updates state correctly on adding additional images exceeding image attachment limit`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                val state = expectItem()
                assert(state.mediaAttachments.size == 2 && state.hasImageAttachments)
                sut.handleIntent(MediaSelected(listOf(mockk(), mockk()), setOf(AttachmentType.IMAGE)))
                val stateFinal = expectItem()
                assert(stateFinal.mediaAttachments.size == 4 && stateFinal.hasImageAttachments)
            }
        }

    @Test
    fun `updates state correctly on adding additional media of different type`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            val uris = (0..1).map { mockk<Uri>() }
            sut.testStates {
                expectItem()
                sut.handleIntent(MediaSelected(uris, setOf(AttachmentType.IMAGE)))
                val state = expectItem()
                assert(state.mediaAttachments.size == 2 && state.hasImageAttachments)
                sut.handleIntent(MediaSelected(listOf(mockk()), setOf(AttachmentType.VIDEO)))
                val stateFinal = expectItem()
                assert(stateFinal.mediaAttachments.size == 1 && !stateFinal.hasImageAttachments)
            }
        }

    @Test
    fun `updates state correctly on valid gif selection`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testStates {
                expectItem()
                sut.handleIntent(GifSelected("id", "uri"))
                assert(expectItem().isDownloadingGif)
                val state = expectItem()
                assert(!state.isDownloadingGif && state.attachmentType == AttachmentType.GIF && state.mediaAttachments.size == 1)
            }
        }

    @Test
    fun `updates state correctly on invalid gif selection`() =
        testDispatcher.runBlockingTest {
            val sut = viewModel()
            sut.testSideEffects {
                sut.handleIntent(GifSelected(null, "uri"))
                assert(expectItem() == SideEffect.Action(AttachmentError(StringId.GIF_DOWNLOAD_ERROR.name)))
            }
        }

}