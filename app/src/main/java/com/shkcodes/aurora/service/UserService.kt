package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.TwitterClient
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.api.response.toUser
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.TweetType
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

private const val TIMELINE_REFRESH_THRESHOLD = 5 // minutes

@Singleton
class UserService @Inject constructor(
    private val tweetsDao: TweetsDao,
    private val preferenceManager: PreferenceManager,
    private val dispatcherProvider: DispatcherProvider,
    private val timeProvider: TimeProvider,
    private val client: TwitterClient
) {
    private val minTime = Instant.EPOCH.atZone(ZoneOffset.UTC)

    private val isTimelineStale: Boolean
        get() {
            val difference =
                Duration.between(preferenceManager.timelineRefreshTime, timeProvider.now())
            return difference.toMinutes() >= TIMELINE_REFRESH_THRESHOLD
        }

    suspend fun fetchTimelineTweets(newerThan: TweetEntity?, afterId: Long?): TweetItems {
        if (isTimelineStale || afterId != null || newerThan != null) {
            val freshTweets = dispatcherProvider.execute { client.getTimelineTweets(afterId, newerThan?.id) }

            tweetsDao.cacheTimeline(freshTweets, TweetType.TIMELINE)
            if (isTimelineStale) preferenceManager.timelineRefreshTime = timeProvider.now()
        }
        return tweetsDao.getCachedTimeline(newerThan?.createdAt ?: minTime)
    }

    suspend fun getMediaForTweet(tweetId: Long): List<MediaEntity> {
        return tweetsDao.getTweetMedia(tweetId)
    }

    suspend fun getCachedTweetsForUser(userHandle: String): TweetItems {
        return tweetsDao.getUserTweets(userHandle)
    }

    fun fetchUserProfile(userHandle: String): User {
        return client.getProfile(userHandle).toUser()
    }

    suspend fun fetchUserTweets(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = dispatcherProvider.execute { client.getUserTweets(userHandle, afterId) }
        tweetsDao.cacheTimeline(tweets, TweetType.USER)
        return tweetsDao.getUserTweets(userHandle)
    }

    suspend fun fetchUserFavorites(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = dispatcherProvider.execute { client.getFavoriteTweets(userHandle, afterId) }
        tweetsDao.cacheTimeline(tweets, TweetType.FAVORITES, userHandle)
        return tweetsDao.getUserFavorites(userHandle)
    }

    suspend fun postTweet(content: String, attachments: List<File>, hasImageAttachments: Boolean) {
        dispatcherProvider.execute { client.updateStatus(content, attachments, hasImageAttachments) }
    }
}
