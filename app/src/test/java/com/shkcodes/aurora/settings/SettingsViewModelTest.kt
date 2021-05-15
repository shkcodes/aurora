package com.shkcodes.aurora.settings

import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.service.PreferencesService
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent.ToggleAutoplayVideos
import com.shkcodes.aurora.ui.settings.SettingsContract.State
import com.shkcodes.aurora.ui.settings.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SettingsViewModelTest : BaseTest() {

    private lateinit var viewModel: SettingsViewModel
    private val preferencesService: PreferencesService = mockk {
        every { autoplayVideos } returns true
    }

    private val eventBus: EventBus = mockk()

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = SettingsViewModel(testDispatcherProvider, preferencesService, eventBus)
    }

    @Test
    fun `state reads data correctly from preference`() = viewModel.test(states = {
        assert(expectItem() == State(autoplayVideos = true))
    })

    @Test
    fun `state updates correctly on toggle auto play`() = viewModel.test(listOf(
        ToggleAutoplayVideos,
        ToggleAutoplayVideos
    ), {
        assert(expectItem() == State(autoplayVideos = true))
        assert(expectItem() == State(autoplayVideos = false))
        assert(expectItem() == State(autoplayVideos = true))
    })
}