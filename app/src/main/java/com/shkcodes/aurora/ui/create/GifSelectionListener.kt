package com.shkcodes.aurora.ui.create

import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.views.GiphyDialogFragment

interface GifSelectionListener : GiphyDialogFragment.GifSelectionListener {

    override fun didSearchTerm(term: String) = Unit

    override fun onDismissed(selectedContentType: GPHContentType) = Unit
    override fun onGifSelected(media: Media, searchTerm: String?, selectedContentType: GPHContentType) {
        gifSelected(media)
    }

    fun gifSelected(media: Media)
}
