package com.shkcodes.aurora.ui.timeline

import android.view.View
import android.widget.ImageView
import com.shkcodes.aurora.cache.entities.MediaEntity

interface TweetListHandler {
    fun onTweetContentClick(text: String)
    fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView, root: View)
    fun onProfileClick(userHandle: String)
}