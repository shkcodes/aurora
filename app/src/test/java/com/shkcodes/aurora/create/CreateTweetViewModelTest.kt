package com.shkcodes.aurora.create

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.SideEffect.DisplayScreen
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
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

    private fun viewModel() = CreateTweetViewModel(testDispatcherProvider, userService)


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

}