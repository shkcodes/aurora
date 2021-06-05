package com.shkcodes.aurora.ui.media

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.cache.entities.MediaEntity

interface MediaViewerContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(val initialIndex: Int = 0, val media: List<MediaEntity> = emptyList())

    sealed class Intent {
        data class Init(val index: Int, val tweetId: Long) : Intent()
    }
}
