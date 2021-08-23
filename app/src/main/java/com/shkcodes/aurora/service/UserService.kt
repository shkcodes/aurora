package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.TwitterApi
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.TweetType
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import twitter4j.StatusUpdate
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
    private val timeProvider: TimeProvider,
    private val twitterApi: TwitterApi,
    private val twitterService: TwitterService
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
            val freshTweets =
                twitterService.getTimelineTweets(
                    afterId,
                    newerThan?.id
                )

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
        return twitterService.getProfile(userHandle)
    }

    suspend fun fetchUserTweets(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = twitterService.getUserTweets(userHandle, afterId)
        tweetsDao.cacheTimeline(tweets, TweetType.USER)
        return tweetsDao.getUserTweets(userHandle)
    }

    suspend fun fetchUserFavorites(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = twitterService.getFavoriteTweets(userHandle, afterId)
        tweetsDao.cacheTimeline(tweets, TweetType.FAVORITES, userHandle)
        return tweetsDao.getUserFavorites(userHandle)
    }

    @Suppress("SpreadOperator")
    fun postTweet(content: String, attachments: List<File>, hasImageAttachments: Boolean) {
        val mediaIds = attachments.map {
            if (hasImageAttachments) {
                uploadImage(it)
            } else {
                uploadVideo(it)
            }
        }
        twitterApi.updateStatus(StatusUpdate(content).apply { setMediaIds(*mediaIds.toLongArray()) })
    }

    private fun uploadImage(file: File): Long {
        return twitterApi.uploadMedia(file).mediaId
    }

    private fun uploadVideo(file: File): Long {
        return twitterApi.uploadMediaChunked(file.name, file.inputStream()).mediaId
    }
}
