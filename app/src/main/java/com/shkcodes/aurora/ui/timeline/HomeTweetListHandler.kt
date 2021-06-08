package com.shkcodes.aurora.ui.timeline

import android.view.View
import android.widget.ImageView
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.android.material.transition.MaterialSharedAxis
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.util.applySharedAxisExitTransition

class HomeTweetListHandler(private val fragment: HomeTimelineFragment) : TweetListHandler {

    override fun onAnnotationClick(text: String) {
        fragment.dispatchIntent(AnnotatedContentClick(text))
    }

    override fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView, root: View) {
        with(fragment) {
            val extras = FragmentNavigatorExtras(
                imageView to "${media.id}"
            )
            val isAboveCenter = root.y + root.height / 2 < binding.timeline.height / 2
            applySharedAxisExitTransition(MaterialSharedAxis.Y, !isAboveCenter)
            navigate(HomeTimelineFragmentDirections.moveToMediaViewer(media.tweetId, index), extras)
        }
    }

    override fun onProfileClick(userHandle: String) {
        fragment.dispatchIntent(AnnotatedContentClick(userHandle))
    }
}
