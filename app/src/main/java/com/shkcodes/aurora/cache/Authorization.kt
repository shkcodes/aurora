package com.shkcodes.aurora.cache

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import twitter4j.auth.RequestToken

@JsonClass(generateAdapter = true)
data class Authorization(
    @Json(name = "token")
    val token: String,
    @Json(name = "secret")
    val tokenSecret: String
)

fun RequestToken.toAuthorization() = Authorization(token, tokenSecret)

fun Authorization.toRequestToken() = RequestToken(token, tokenSecret)
