package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.TweetType
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val TIMELINE_REFRESH_THRESHOLD = 5 // minutes
private const val CACHE_FLUSH_THRESHOLD = 2L // days

@Singleton
class UserService @Inject constructor(
    private val userApi: UserApi,
    private val tweetsDao: TweetsDao,
    private val preferenceManager: PreferenceManager,
    private val timeProvider: TimeProvider
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
            val freshTweets = userApi.getTimelineTweets(afterId = afterId, sinceId = newerThan?.id)
            tweetsDao.cacheTimeline(freshTweets, TweetType.TIMELINE)
            if (isTimelineStale) preferenceManager.timelineRefreshTime = timeProvider.now()
        }
        return tweetsDao.getCachedTimeline(newerThan?.createdAt ?: minTime)
    }

    suspend fun flushTweetsCache() {
        tweetsDao.removeTweets(ZonedDateTime.now().minusDays(CACHE_FLUSH_THRESHOLD))
    }

    suspend fun getMediaForTweet(tweetId: Long): List<MediaEntity> {
        return tweetsDao.getTweetMedia(tweetId)
    }

    suspend fun getCachedTweetsForUser(userHandle: String): TweetItems {
        return tweetsDao.getUserTweets(userHandle)
    }

    suspend fun fetchUserProfile(userHandle: String): User {
        return userApi.getUserProfile(userHandle)
    }

    suspend fun fetchUserTweets(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = userApi.getUserTweets(userHandle, afterId)
        tweetsDao.cacheTimeline(tweets, TweetType.USER)
        return tweetsDao.getUserTweets(userHandle)
    }

    suspend fun fetchUserFavorites(userHandle: String, afterId: Long? = null): TweetItems {
        val tweets = userApi.getFavoriteTweets(userHandle, afterId)
        tweetsDao.cacheTimeline(tweets, TweetType.FAVORITES, userHandle)
        return tweetsDao.getUserFavorites(userHandle)
    }

    suspend fun postTweet(content: String) {
        val tweet = userApi.postTweet(content)
        tweetsDao.cacheTimeline(listOf(tweet), TweetType.USER, tweet.user.screenName)
    }
}
