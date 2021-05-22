package com.shkcodes.aurora.fakes

import com.shkcodes.aurora.cache.dao.TweetsDao
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.timeline.TimelineItem
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

    override suspend fun getCachedTimeline(createdAt: ZonedDateTime, isTimelineTweet: Boolean) =
        savedTweets.map { TimelineItem(it) }.filter { it.primaryTweet.createdAt > createdAt }

    override suspend fun removeTweets(date: ZonedDateTime) {}

    override suspend fun saveMedia(media: List<MediaEntity>) {
        savedMedia.addAll(media)
    }

    override suspend fun getTweetMedia(tweetId: Long): List<MediaEntity> {
        return savedMedia
    }

    override suspend fun getUserTweets(userId: Long, isTimelineTweet: Boolean) =
        savedTweets.map { TimelineItem(it) }.filter { it.primaryTweet.userId == userId }

}