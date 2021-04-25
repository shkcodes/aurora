package com.shkcodes.aurora.ui.home

import androidx.room.Embedded
import androidx.room.Relation
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity

data class TimelineTweetItem(
    @Embedded val primaryTweet: TweetEntity,
    @Relation(
        parentColumn = "quotedTweetId",
        entityColumn = "tweetId"
    )
    val quotedTweet: TweetEntity?,
    @Relation(
        parentColumn = "retweetedTweetId",
        entityColumn = "tweetId"
    )
    val retweetedTweet: TweetEntity?,
    @Relation(
        parentColumn = "tweetId",
        entityColumn = "tweetId"
    )
    val media: List<MediaEntity>
)

typealias TimelineTweets = List<TimelineTweetItem>
