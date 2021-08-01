package com.shkcodes.aurora.ui.main

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.Event.TogglePaginatedLoading
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.service.FileService
import com.shkcodes.aurora.ui.main.MainContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    eventBus: EventBus,
    fileService: FileService
) : ViewModel() {
    init {
        eventBus.getEvents().onEach(::handleEvent).launchIn(viewModelScope)
        fileService.clearCache()
    }

    private fun handleEvent(event: Event) {
        if (event is TogglePaginatedLoading) {
            currentState = currentState.copy(isLoading = event.isLoading)
        }
    }
}
