package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.execute
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.timeline.TimelineItems
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

    suspend fun fetchTimelineTweets(
        newerThan: TweetEntity?,
        afterId: Long?
    ): Result<TimelineItems> {
        return execute {
            if (isTimelineStale || afterId != null || newerThan != null) {
                val freshTweets = userApi.getTimelineTweets(afterId = afterId)
                tweetsDao.cacheTimeline(freshTweets)
                if (isTimelineStale) preferenceManager.timelineRefreshTime = timeProvider.now()
            }
            tweetsDao.getCachedTimeline(newerThan?.createdAt ?: minTime)
        }
    }

    suspend fun flushTweetsCache() {
        tweetsDao.removeTweets(ZonedDateTime.now().minusDays(CACHE_FLUSH_THRESHOLD))
    }

    suspend fun getMediaForTweet(tweetId: Long): List<MediaEntity> {
        return tweetsDao.getTweetMedia(tweetId)
    }

    suspend fun fetchUserProfile(userHandle: String): Result<User> {
        return execute { userApi.getUserProfile(userHandle) }
    }

    suspend fun fetchUserTweets(): Result<TimelineItems> {
        return execute {
            val tweets = userApi.getUserTweets()
            tweetsDao.cacheTimeline(tweets)
            tweetsDao.getUserTweets(tweets.first().user.id)
        }
    }
}
