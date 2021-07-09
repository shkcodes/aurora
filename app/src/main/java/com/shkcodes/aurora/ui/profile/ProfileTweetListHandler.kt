package com.shkcodes.aurora.ui.profile

import android.os.Parcelable
import android.widget.ImageView
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TweetListHandler
import com.shkcodes.aurora.util.applySharedAxisExitTransition

class ProfileTweetListHandler(
    private val fragment: ProfileFragment,
    private val userHandle: String
) : TweetListHandler {

    private val states = mutableMapOf<Int, Parcelable?>()

    override fun onAnnotationClick(text: String) {
        fragment.dispatchIntent(AnnotatedContentClick(text))
    }

    override fun onMediaClick(media: MediaEntity, index: Int, imageView: ImageView) {
        with(fragment) {
            val extras = FragmentNavigatorExtras(
                imageView to "${media.id}"
            )
            applySharedAxisExitTransition()
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

    fun loadNextPage(force: Boolean = false) {
        fragment.dispatchIntent(LoadNextPage(force))
    }

    fun showUserMedia(mediaId: Long, index: Int, imageView: ImageView) {
        with(fragment) {
            val extras = FragmentNavigatorExtras(
                imageView to "$mediaId"
            )
            applySharedAxisExitTransition()
            navigate(ProfileFragmentDirections.moveToProfileMediaViewer(userHandle, index), extras)
        }
    }

    var scrollToBottom = false
        get() {
            val currentValue = field
            field = false
            return currentValue
        }
}
