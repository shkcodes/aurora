package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.execute
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.toCachedTweets
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val TIMELINE_REFRESH_THRESHOLD = 5 // minutes

@Singleton
class UserService @Inject constructor(
    private val userApi: UserApi,
    private val tweetsDao: TweetsDao,
    private val preferenceManager: PreferenceManager
) {

    private val isTimelineStale: Boolean
        get() {
            val difference =
                Duration.between(preferenceManager.timelineRefreshTime, LocalDateTime.now())
            return difference.toMinutes() > TIMELINE_REFRESH_THRESHOLD
        }

    suspend fun getTimelineTweets(): Result<CachedTweets> {
        return execute {
            val tweets = if (isTimelineStale) {
                val freshTweets = userApi.getTimelineTweets().toCachedTweets()
                tweetsDao.saveTweets(freshTweets)
                preferenceManager.timelineRefreshTime = LocalDateTime.now()
                freshTweets
            } else {
                tweetsDao.getTweets()
            }
            tweets
        }
    }
}
