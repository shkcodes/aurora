package com.shkcodes.aurora.cache

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import twitter4j.auth.AccessToken

@JsonClass(generateAdapter = true)
data class UserCredentials(
    @Json(name = "authorization")
    val authorization: Authorization,
    @Json(name = "handle")
    val handle: String,
    @Json(name = "userId")
    val userId: Long
)

fun AccessToken.toUserCredentials() = UserCredentials(Authorization(token, tokenSecret), screenName, userId)

fun UserCredentials.toAccessToken() = AccessToken(authorization.token, authorization.tokenSecret, userId)
