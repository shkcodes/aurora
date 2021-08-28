package com.shkcodes.aurora.api

import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.cache.toRequestToken
import twitter4j.PagableResponseList
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.Configuration
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val FRIENDS_PER_PAGE = 200
private const val TWEETS_PER_PAGE = 200

@Suppress("TooManyFunctions")
@Singleton
class TwitterClient @Inject constructor(private val config: Configuration) {

    private var twitterApi: TwitterApi = TwitterFactory(config).instance

    fun switchToAccount(accessToken: AccessToken) {
        twitterApi = TwitterFactory(config).getInstance(accessToken)
    }

    fun getRequestToken(): RequestToken {
        return twitterApi.getOAuthRequestToken(BuildConfig.CALLBACK_URL)
    }

    fun login(authorization: Authorization, verifier: String): AccessToken {
        val accessToken = twitterApi.getOAuthAccessToken(authorization.toRequestToken(), verifier)
        switchToAccount(accessToken)
        return accessToken
    }

    fun getFriendsList(userId: Long, cursor: Long): PagableResponseList<User> {
        return twitterApi.getFriendsList(userId, cursor, FRIENDS_PER_PAGE, true, false)
    }

    fun getTimelineTweets(afterId: Long?, sinceId: Long?): ResponseList<Status> {
        val paging = Paging(1, TWEETS_PER_PAGE)
        if (afterId != null) {
            paging.maxId = afterId
        }
        if (sinceId != null) {
            paging.sinceId = sinceId
        }
        return twitterApi.getHomeTimeline(paging)
    }

    fun getUserTweets(userHandle: String, afterId: Long?): ResponseList<Status> {
        val paging = Paging(1, TWEETS_PER_PAGE)
        if (afterId != null) {
            paging.maxId = afterId
        }
        return twitterApi.getUserTimeline(userHandle, paging)
    }

    fun getFavoriteTweets(userHandle: String, afterId: Long?): ResponseList<Status> {
        val paging = Paging(1, TWEETS_PER_PAGE)
        if (afterId != null) {
            paging.maxId = afterId
        }
        return twitterApi.getFavorites(userHandle, paging)
    }

    @Suppress("SpreadOperator")
    fun updateStatus(content: String, attachments: List<File>, hasImageAttachments: Boolean) {
        val mediaIds = attachments.map {
            if (hasImageAttachments) {
                uploadImage(it)
            } else {
                uploadVideo(it)
            }
        }
        twitterApi.updateStatus(StatusUpdate(content).apply { setMediaIds(*mediaIds.toLongArray()) })
    }

    private fun uploadImage(file: File): Long {
        return twitterApi.uploadMedia(file).mediaId
    }

    private fun uploadVideo(file: File): Long {
        return twitterApi.uploadMediaChunked(file.name, file.inputStream()).mediaId
    }

    fun getProfile(userHandle: String): User {
        return twitterApi.showUser(userHandle)
    }
}
