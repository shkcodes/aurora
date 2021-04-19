package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.execute
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.toCachedTweets
import java.time.Duration
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

    private val isTimelineStale: Boolean
        get() {
            val difference =
                Duration.between(preferenceManager.timelineRefreshTime, timeProvider.now())
            return difference.toMinutes() >= TIMELINE_REFRESH_THRESHOLD
        }

    suspend fun fetchTimelineTweets(afterId: Long?): Result<Unit> {
        return execute {
            if (isTimelineStale || afterId != null) {
                val freshTweets = userApi.getTimelineTweets(afterId = afterId).toCachedTweets()
                tweetsDao.saveTweets(freshTweets)
                if (isTimelineStale) preferenceManager.timelineRefreshTime = timeProvider.now()
            }
        }
    }

    fun getTimelineTweets() = tweetsDao.getTweets()

    suspend fun flushTweetsCache() {
        tweetsDao.removeTweets(ZonedDateTime.now().minusDays(CACHE_FLUSH_THRESHOLD))
    }
}
