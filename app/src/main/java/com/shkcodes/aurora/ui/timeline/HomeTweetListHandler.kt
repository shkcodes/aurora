package com.shkcodes.aurora.ui.timeline

import android.widget.ImageView
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.util.applySharedAxisExitTransition

class HomeTweetListHandler(private val fragment: HomeTimelineFragment) : TweetListHandler {

    override fun onAnnotationClick(text: String) {
        fragment.dispatchIntent(AnnotatedContentClick(text))
    }

    override fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView) {
        with(fragment) {
            val extras = FragmentNavigatorExtras(
                imageView to "${media.id}"
            )
            applySharedAxisExitTransition()
            navigate(HomeTimelineFragmentDirections.moveToMediaViewer(media.tweetId, index), extras)
        }
    }

    override fun onProfileClick(userHandle: String) {
        fragment.dispatchIntent(AnnotatedContentClick(userHandle))
    }
}
