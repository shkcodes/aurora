package com.shkcodes.aurora.api

import com.shkcodes.aurora.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.ByteString
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

// from https://gist.github.com/polson/227e1a039a09f2728163bf7235990178

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
        private const val OAUTH_NONCE = "oauth_nonce"
        private const val OAUTH_SIGNATURE = "oauth_signature"
        private const val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
        private const val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
        private const val OAUTH_TIMESTAMP = "oauth_timestamp"
        private const val OAUTH_TOKEN = "oauth_token"
        private const val OAUTH_VERSION = "oauth_version"
        private const val OAUTH_VERSION_VALUE = "1.0"
        const val ACCEPT = "Accept"
        const val APPLICATION_JSON = "application/json"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val HMAC_SHA1 = "HmacSHA1"

        private val baseKeys = arrayListOf(
            OAUTH_CONSUMER_KEY,
            OAUTH_NONCE,
            OAUTH_SIGNATURE,
            OAUTH_SIGNATURE_METHOD,
            OAUTH_TIMESTAMP,
            OAUTH_TOKEN,
            OAUTH_VERSION
        )
    }

    private data class OauthKeys(
        val consumerKey: String,
        val consumerSecret: String,
        val accessToken: String,
        val accessSecret: String
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        return with(chain) {
            proceed(signRequest(request()))
        }
    }

    private fun signRequest(request: Request): Request {
        val nonce = UUID.randomUUID().toString()
        val timestamp: Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val keys = OauthKeys(
            BuildConfig.CONSUMER_KEY,
            BuildConfig.CONSUMER_SECRET,
            BuildConfig.ACCESS_TOKEN,
            BuildConfig.ACCESS_SECRET
        )

        val parameters = hashMapOf(
            OAUTH_CONSUMER_KEY to keys.consumerKey,
            OAUTH_NONCE to nonce,
            OAUTH_SIGNATURE_METHOD to OAUTH_SIGNATURE_METHOD_VALUE,
            OAUTH_TIMESTAMP to timestamp.toString(),
            OAUTH_VERSION to OAUTH_VERSION_VALUE,
            OAUTH_TOKEN to keys.accessToken
        )

        // Copy query parameters into param map
        val url = request.url
        for (i in 0 until url.querySize) {
            parameters[url.queryParameterName(i)] = url.queryParameterValue(i).orEmpty()
        }

        // Create signature
        val method = request.method.encodeUtf8()
        val baseUrl = request.url.newBuilder().query(null).build().toString().encodeUtf8()
        val signingKey = "${keys.consumerSecret.encodeUtf8()}&${
            keys.accessSecret.encodeUtf8()
        }"
        val params = parameters.encodeForSignature()
        val dataToSign = "$method&$baseUrl&$params"
        parameters[OAUTH_SIGNATURE] = sign(signingKey, dataToSign).encodeUtf8()

        // Create auth header
        val authHeader = "OAuth ${parameters.toHeaderFormat()}"
        return request.newBuilder().addHeader(AUTHORIZATION_HEADER, authHeader)
            .header(ACCEPT, APPLICATION_JSON)
            .build()
    }

    @Suppress("SpreadOperator")
    private fun sign(key: String, data: String): String {
        val secretKey = SecretKeySpec(key.toBytesUtf8(), HMAC_SHA1)
        val macResult = Mac.getInstance(HMAC_SHA1).run {
            init(secretKey)
            doFinal(data.toBytesUtf8())
        }
        return ByteString.of(*macResult).base64()
    }

    private fun String.toBytesUtf8() = this.toByteArray()

    private fun HashMap<String, String>.toHeaderFormat() =
        filter { it.key in baseKeys }
            .toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=\"${it.value}\"" }
            .joinToString(", ")

    private fun HashMap<String, String>.encodeForSignature() =
        toList()
            .sortedBy { (key, _) -> key }
            .toMap()
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .encodeUtf8()

    private fun String.encodeUtf8() = URLEncoder.encode(this, "UTF-8").replace("+", "%2B")
}
