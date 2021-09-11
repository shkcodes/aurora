package com.shkcodes.aurora.ui.settings

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.Event.AutoplayVideosToggled
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent.ToggleAutoplayVideos
import com.shkcodes.aurora.ui.settings.SettingsContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceService: PreferenceService,
    private val eventBus: EventBus
) : ViewModel() {

    init {
        currentState = currentState.copy(autoplayVideos = preferenceService.autoplayVideos)
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is ToggleAutoplayVideos -> {
                val currentValue = currentState.autoplayVideos
                preferenceService.autoplayVideos = !currentValue
                currentState = currentState.copy(autoplayVideos = !currentValue)
                viewModelScope.launch {
                    eventBus.sendEvent(AutoplayVideosToggled)
                }
            }
        }
    }
}
