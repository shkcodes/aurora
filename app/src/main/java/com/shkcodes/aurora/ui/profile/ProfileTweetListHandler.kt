package com.shkcodes.aurora.ui.profile

import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.android.material.transition.MaterialSharedAxis
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TweetListHandler
import com.shkcodes.aurora.util.applySharedAxisExitTransition

class ProfileTweetListHandler(private val fragment: ProfileFragment) : TweetListHandler {

    private val states = mutableMapOf<Int, Parcelable?>()

    override fun onAnnotationClick(text: String) {
        fragment.dispatchIntent(AnnotatedContentClick(text))
    }

    override fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView, root: View) {
        with(fragment) {
            val extras = FragmentNavigatorExtras(
                imageView to "${media.id}"
            )
            val isAboveCenter = root.y + root.height / 2 < binding.profilePager.height / 2
            applySharedAxisExitTransition(MaterialSharedAxis.Y, !isAboveCenter)
            navigate(ProfileFragmentDirections.moveToMediaViewer(media.tweetId, index), extras)
        }
    }

    override fun onProfileClick(userHandle: String) {
        fragment.dispatchIntent(AnnotatedContentClick(userHandle))
    }

    override fun saveState(state: Parcelable?) {
        val index = fragment.binding.profilePager.currentItem
        states[index] = state
    }

    override fun getState(index: Int): Parcelable? = states[index]

    fun loadNextPage() {
        fragment.dispatchIntent(LoadNextPage)
    }
}
