package com.shkcodes.aurora.ui.timeline

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity

data class TimelineItem(
    @Embedded val primaryTweet: TweetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tweetId"
    )
    private val primaryTweetMedia: List<MediaEntity> = emptyList(),
    @Relation(
        parentColumn = "retweetId",
        entityColumn = "id",
        entity = TweetEntity::class
    )
    private val retweetDto: TweetDto? = null,
    @Relation(
        parentColumn = "quoteTweetId",
        entityColumn = "id",
        entity = TweetEntity::class
    )
    private val quoteTweetDto: TweetDto? = null,
    @Relation(
        parentColumn = "retweetQuoteId",
        entityColumn = "id",
        entity = TweetEntity::class
    )
    private val retweetQuoteDto: TweetDto? = null,
) {

    data class TweetDto(
        @Embedded
        val tweet: TweetEntity,
        @Relation(
            parentColumn = "id",
            entityColumn = "tweetId"
        )
        val media: List<MediaEntity>
    )

    @Ignore
    val tweet = retweetDto?.tweet ?: primaryTweet

    @Ignore
    val quoteTweet = retweetQuoteDto?.tweet ?: quoteTweetDto?.tweet

    @Ignore
    val isRetweet = retweetDto != null

    @Ignore
    val retweeter = primaryTweet.userName

    @Ignore
    val tweetId = primaryTweet.id

    @Ignore
    val tweetMedia = retweetDto?.media ?: primaryTweetMedia

    @Ignore
    val hasAnimatedMedia = tweetMedia.any { it.isAnimatedMedia }

    @Ignore
    val quoteTweetMedia =
        if (isRetweet) retweetQuoteDto?.media.orEmpty() else quoteTweetDto?.media.orEmpty()
}
typealias TimelineItems = List<TimelineItem>
