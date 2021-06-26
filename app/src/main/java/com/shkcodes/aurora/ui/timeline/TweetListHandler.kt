package com.shkcodes.aurora.ui.timeline

import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import com.shkcodes.aurora.cache.entities.MediaEntity

interface TweetListHandler {
    fun onAnnotationClick(text: String)
    fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView, root: View)
    fun onProfileClick(userHandle: String)
    fun saveState(parcelable: Parcelable?) {}
    fun getState(index: Int): Parcelable? = null
}
