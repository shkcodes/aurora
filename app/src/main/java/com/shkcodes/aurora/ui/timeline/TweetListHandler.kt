package com.shkcodes.aurora.ui.timeline

import android.widget.ImageView
import com.shkcodes.aurora.cache.entities.MediaEntity

interface TweetListHandler {
    fun onAnnotationClick(text: String)
    fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView)
    fun onProfileClick(userHandle: String)
}
