package com.shkcodes.aurora.service

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.shkcodes.aurora.ui.media.ImageItemViewHolder
import com.shkcodes.aurora.ui.profile.items.GridMediaViewHolder
import com.shkcodes.aurora.ui.timeline.items.TweetItemViewHolder
import com.shkcodes.aurora.util.recyclerView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedElementTransitionHelper @Inject constructor() {
    var tweetIndex: Int = -1
    var mediaIndex: Int = -1

    fun getTweetImageView(list: RecyclerView): ImageView? {
        return (list.findViewHolderForAdapterPosition(tweetIndex) as TweetItemViewHolder)
            .binding.primaryTweet.tweetMedia.getImageView(mediaIndex)
    }

    fun getPagerImageView(pager: ViewPager2): ImageView? {
        return (pager.recyclerView
            .findViewHolderForAdapterPosition(mediaIndex) as ImageItemViewHolder?)?.binding?.image
    }

    fun getGridImageView(grid: RecyclerView): ImageView? {
        return (grid.findViewHolderForAdapterPosition(mediaIndex) as GridMediaViewHolder?)?.binding
            ?.image
    }
}
