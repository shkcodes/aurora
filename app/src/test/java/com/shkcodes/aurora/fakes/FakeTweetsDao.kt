package com.shkcodes.aurora.fakes

import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.home.TimelineTweetItem
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime

class FakeTweetsDao : TweetsDao() {

    private val tweets = mutableListOf<TweetEntity>()

    override suspend fun getTweet(tweetId: Long): TweetEntity {
        return tweets.first { it.tweetId == tweetId }
    }

    override suspend fun saveTweets(freshTweets: CachedTweets) {
        tweets.addAll(freshTweets)
    }

    override fun getCachedTimeline(isTimelineTweet: Boolean) =
        flowOf(tweets.map { TimelineTweetItem(it, null, null) })


    override suspend fun removeTweets(date: ZonedDateTime) {}
}