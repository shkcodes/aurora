package com.shkcodes.aurora.ui.home

import androidx.room.Embedded
import androidx.room.Relation
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
    val retweetedTweet: TweetEntity?
)

typealias TimelineTweets = List<TimelineTweetItem>
