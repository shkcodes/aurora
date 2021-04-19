package com.shkcodes.aurora.fakes

import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.TweetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime

class FakeTweetsDao : TweetsDao {

    private val items = mutableListOf<TweetEntity>()

    override fun getTweets(): Flow<CachedTweets> = flowOf(items)

    override suspend fun saveTweets(tweets: CachedTweets) {
        items.addAll(tweets)
    }

    override suspend fun removeTweets(date: ZonedDateTime) {}
}