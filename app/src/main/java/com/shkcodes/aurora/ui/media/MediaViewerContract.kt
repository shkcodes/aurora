package com.shkcodes.aurora.ui.media

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.media.MediaViewerContract.State.Empty

interface MediaViewerContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(Empty)

    sealed class State {
        object Empty : State()
        data class Content(
            val initialIndex: Int = 0,
            val media: List<MediaEntity> = emptyList()
        ) : State()
    }

    sealed class Intent {
        data class Init(val index: Int, val tweetId: Long) : Intent()
    }
}
