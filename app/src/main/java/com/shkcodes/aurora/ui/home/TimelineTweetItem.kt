package com.shkcodes.aurora.ui.home

import androidx.room.Embedded
import androidx.room.Relation
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity

data class TimelineTweetItem(
    @Embedded val primaryTweet: TweetEntity,
    @Relation(
        parentColumn = "quoteTweetId",
        entityColumn = "id"
    )
    val quoteTweet: TweetEntity? = null,
    @Relation(
        parentColumn = "retweetId",
        entityColumn = "id"
    )
    val retweet: TweetEntity? = null,
    @Relation(
        parentColumn = "retweetQuoteId",
        entityColumn = "id"
    )
    val retweetQuote: TweetEntity? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "tweetId"
    )
    val media: List<MediaEntity> = emptyList(),
    @Relation(
        parentColumn = "quoteTweetId",
        entityColumn = "tweetId"
    )
    val quoteTweetMedia: List<MediaEntity> = emptyList(),
    @Relation(
        parentColumn = "retweetId",
        entityColumn = "tweetId"
    )
    val retweetMedia: List<MediaEntity> = emptyList()
)

typealias TimelineTweets = List<TimelineTweetItem>
