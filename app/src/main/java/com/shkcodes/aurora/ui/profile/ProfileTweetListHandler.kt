package com.shkcodes.aurora.ui.profile

import android.os.Parcelable
import android.widget.ImageView
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.viewpager2.widget.ViewPager2
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.items.pagerMediaGridHolder
import com.shkcodes.aurora.ui.profile.items.pagerTweetListHolder
import com.shkcodes.aurora.ui.timeline.TweetListHandler
import com.shkcodes.aurora.util.applySharedAxisExitTransition
import com.shkcodes.aurora.util.recyclerView

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

    fun saveState(index: Int, state: Parcelable?) {
        states[index] = state
    }

    fun getState(index: Int): Parcelable? = states[index]

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

    fun savePagerStates(pager: ViewPager2) {
        with(pager.recyclerView) {
            saveState(0, pagerTweetListHolder(0).binding.list.layoutManager?.onSaveInstanceState())
            saveState(1, pagerMediaGridHolder.binding.grid.layoutManager?.onSaveInstanceState())
            saveState(2, pagerTweetListHolder(2).binding.list.layoutManager?.onSaveInstanceState())
        }
    }

    var scrollToBottom = false
        get() {
            val currentValue = field
            field = false
            return currentValue
        }
}
