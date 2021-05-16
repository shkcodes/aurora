package com.shkcodes.aurora.service

import com.shkcodes.aurora.api.Result
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.response.Tweet
import com.shkcodes.aurora.base.BaseTest
import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.cache.entities.toCachedTweets
import com.shkcodes.aurora.fakes.FakeTweetsDao
import com.shkcodes.aurora.ui.timeline.TimelineItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
class UserServiceTest : BaseTest() {

    lateinit var userService: UserService

    private val freshTweet = mockk<Tweet>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "definitely shouldn't have tweeted this"
        every { quoteTweet } returns null
        every { retweet } returns null
    }
    private val staleTweet = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 6031994
        every { content } returns "old tweet"
    }

    private val cachedTweets = listOf(freshTweet).toCachedTweets(
        true
    ).map { TimelineItem(it) }

    private val fakeTime = LocalDateTime.of(2021, 4, 16, 23, 10)

    private val userApi = mockk<UserApi> {
        coEvery { getTimelineTweets() } returns listOf(freshTweet)
    }

    lateinit var tweetsDao: FakeTweetsDao

    private val fakeTimeProvider = mockk<TimeProvider> {
        every { now() } returns fakeTime
    }

    private val preferenceManager = mockk<PreferenceManager>()

    @Before
    override fun setUp() {
        super.setUp()
        tweetsDao = FakeTweetsDao()
        userService = UserService(userApi, tweetsDao, preferenceManager, fakeTimeProvider)
    }

    @Test
    fun `get timeline tweets returns fresh tweets if cached data is stale`() =
        testDispatcher.runBlockingTest {
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(5)
            val result = userService.fetchTimelineTweets(false, null)
            assert(result == Result.Success(cachedTweets))
            assert(tweetsDao.getCachedTimeline() == cachedTweets)
        }

    @Test
    fun `get timeline tweets returns fresh tweets if forced to refresh`() =
        testDispatcher.runBlockingTest {
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(5)
            val result = userService.fetchTimelineTweets(true, null)
            assert(result == Result.Success(cachedTweets))
            assert(tweetsDao.getCachedTimeline() == cachedTweets)
        }

    @Test
    fun `get timeline tweets returns cached tweets if cached data is not stale`() =
        testDispatcher.runBlockingTest {
            tweetsDao.saveTweets(listOf(staleTweet))
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(7)
            val result = userService.fetchTimelineTweets(false, null)
            assert(result == Result.Success(listOf(staleTweet).map {
                TimelineItem(it)
            }))
            assert(
                tweetsDao.getCachedTimeline() == listOf(staleTweet).map {
                    TimelineItem(it)
                })
        }

}