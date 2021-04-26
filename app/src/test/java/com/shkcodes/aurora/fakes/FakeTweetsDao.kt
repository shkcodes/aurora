package com.shkcodes.aurora.fakes

import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.home.TimelineTweetItem
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime

class FakeTweetsDao : TweetsDao() {

    private val savedTweets = mutableListOf<TweetEntity>()
    private val savedMedia = mutableListOf<MediaEntity>()

    override suspend fun getTweet(tweetId: Long): TweetEntity {
        return savedTweets.first { it.id == tweetId }
    }

    override suspend fun saveTweets(tweets: CachedTweets) {
        savedTweets.addAll(tweets)
    }

    override fun getCachedTimeline(isTimelineTweet: Boolean) =
        flowOf(savedTweets.map { TimelineTweetItem(it) })

    override suspend fun removeTweets(date: ZonedDateTime) {}

    override suspend fun saveMedia(media: List<MediaEntity>) {
        savedMedia.addAll(media)
    }
}