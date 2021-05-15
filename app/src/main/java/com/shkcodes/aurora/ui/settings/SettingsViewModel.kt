package com.shkcodes.aurora.ui.settings

import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.service.PreferencesService
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent.ToggleAutoplayVideos
import com.shkcodes.aurora.ui.settings.SettingsContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val preferencesService: PreferencesService
) : ViewModel() {

    init {
        currentState = currentState.copy(autoplayVideos = preferencesService.autoplayVideos)
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is ToggleAutoplayVideos -> {
                val currentValue = currentState.autoplayVideos
                preferencesService.autoplayVideos = !currentValue
                currentState = currentState.copy(autoplayVideos = !currentValue)
            }
        }
    }
}
