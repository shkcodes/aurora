package com.shkcodes.aurora.ui.profile.media

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.ui.tweetlist.TweetItem

class ProfileMediaViewerContract {
    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val media: List<ProfileMediaDto> = emptyList(),
        val currentIndex: Int = -1
    )

    sealed class Intent {
        data class Init(val userHandle: String, val index: Int) : Intent()
        data class PageChange(val index: Int) : Intent()
    }
}

data class ProfileMediaDto(val image: MediaEntity, val retweets: Int, val likes: Int)

fun TweetItem.toProfileMediaDto(): List<ProfileMediaDto> {
    return tweetMedia.map { ProfileMediaDto(it, primaryTweet.retweetCount, primaryTweet.favoriteCount) }
}
