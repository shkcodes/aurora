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
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
class UserServiceTest : BaseTest() {
    private val time = LocalDateTime.of(1993, 12, 23, 1, 0, 0)
    private val apiTweetTime = ZonedDateTime.of(time, ZoneId.systemDefault())
    private val localTweetTime = ZonedDateTime.of(time.withHour(1), ZoneId.systemDefault())

    private val apiTweet = mockk<Tweet>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "definitely shouldn't have tweeted this"
        every { quoteTweet } returns null
        every { retweet } returns null
        every { createdAt } returns apiTweetTime
    }
    private val freshApiTweet = mockk<Tweet>(relaxed = true) {
        every { id } returns 23121993
        every { content } returns "definitely shouldn't have tweeted this"
        every { quoteTweet } returns null
        every { retweet } returns null
        every { createdAt } returns apiTweetTime.withHour(2)
    }
    private val localTweet = mockk<TweetEntity>(relaxed = true) {
        every { id } returns 6031994
        every { content } returns "old tweet"
        every { createdAt } returns localTweetTime
    }

    private fun Tweet.toTimelineItems() = listOf(this).toCachedTweets(
        true
    ).map { TimelineItem(it) }


    private fun TweetEntity.toTimelineItems() = listOf(this).map { TimelineItem(it) }


    private val fakeTime = LocalDateTime.of(2021, 4, 16, 23, 10)

    private val userApi = mockk<UserApi> {
        coEvery { getTimelineTweets() } returns listOf(apiTweet) andThen listOf(freshApiTweet)
    }

    private val tweetsDao = FakeTweetsDao()

    private val fakeTimeProvider = mockk<TimeProvider> {
        every { now() } returns fakeTime
    }

    private val preferenceManager = mockk<PreferenceManager>()

    private fun sut() = UserService(userApi, tweetsDao, preferenceManager, fakeTimeProvider)

    @Test
    fun `get timeline tweets returns fresh tweets if cached data is stale`() =
        testDispatcher.runBlockingTest {
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(5)
            val result = sut().fetchTimelineTweets(null, null)
            assert(result == Result.Success(apiTweet.toTimelineItems()))
            assert(tweetsDao.getCachedTimeline(Instant.EPOCH.atZone(ZoneOffset.UTC)) == apiTweet.toTimelineItems())
        }

    @Test
    fun `get timeline tweets returns fresh tweets if refreshed manually`() =
        testDispatcher.runBlockingTest {
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(5)
            sut().fetchTimelineTweets(null, null)
            val result =
                sut().fetchTimelineTweets(listOf(apiTweet).toCachedTweets(true).first(), null)
            assert(result == Result.Success(freshApiTweet.toTimelineItems()))
            assert(tweetsDao.getCachedTimeline(localTweetTime) == freshApiTweet.toTimelineItems())
        }

    @Test
    fun `get timeline tweets returns cached tweets if cached data is not stale`() =
        testDispatcher.runBlockingTest {
            tweetsDao.saveTweets(listOf(localTweet))
            every { preferenceManager.timelineRefreshTime } returns fakeTime.withMinute(7)
            val result = sut().fetchTimelineTweets(null, null)
            assert(result == Result.Success(localTweet.toTimelineItems()))
            assert(tweetsDao.getCachedTimeline(Instant.EPOCH.atZone(ZoneOffset.UTC)) == localTweet.toTimelineItems())
        }

}