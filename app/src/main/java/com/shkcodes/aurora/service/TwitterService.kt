package com.shkcodes.aurora.service

import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.api.TwitterApi
import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.cache.toAccessToken
import com.shkcodes.aurora.cache.toAuthorization
import com.shkcodes.aurora.cache.toRequestToken
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwitterService @Inject constructor(
    private val config: Configuration,
    private val preferenceService: PreferenceService
) {
    private var twitterApi: TwitterApi = TwitterFactory(config).instance

    fun switchToAccount(authorization: Authorization) {
        preferenceService.authorization = authorization
        twitterApi = TwitterFactory(config).getInstance(authorization.toAccessToken())
    }

    fun getRequestToken(): Authorization {
        return twitterApi.getOAuthRequestToken(BuildConfig.CALLBACK_URL).toAuthorization()
    }

    fun login(authorization: Authorization, verifier: String) {
        val authorization =
            twitterApi.getOAuthAccessToken(authorization.toRequestToken(), verifier).toAuthorization()
        switchToAccount(authorization)
    }
}
