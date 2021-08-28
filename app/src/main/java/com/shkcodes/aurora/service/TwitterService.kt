package com.shkcodes.aurora.service

import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.api.TwitterApi
import com.shkcodes.aurora.api.response.toUser
import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.cache.UserCredentials
import com.shkcodes.aurora.cache.toAccessToken
import com.shkcodes.aurora.cache.toAuthorization
import com.shkcodes.aurora.cache.toRequestToken
import com.shkcodes.aurora.cache.toUserCredentials
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import javax.inject.Inject
import javax.inject.Singleton

private const val TWEETS_PER_PAGE = 200

@Singleton
class TwitterService @Inject constructor(
    private val config: Configuration,
    private val preferenceService: PreferenceService
) {
    private var twitterApi: TwitterApi = TwitterFactory(config).instance

    fun switchToAccount(credentials: UserCredentials) {
        preferenceService.userCredentials = credentials
        twitterApi = TwitterFactory(config).getInstance(credentials.toAccessToken())
    }

    fun getRequestToken(): Authorization {
        return twitterApi.getOAuthRequestToken(BuildConfig.CALLBACK_URL).toAuthorization()
    }

    fun login(authorization: Authorization, verifier: String) {
        val credentials =
            twitterApi.getOAuthAccessToken(authorization.toRequestToken(), verifier).toUserCredentials()
        switchToAccount(credentials)
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

    fun getProfile(userHandle: String): com.shkcodes.aurora.api.response.User {
        return twitterApi.showUser(userHandle)!!.toUser()
    }
}
