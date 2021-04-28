package com.shkcodes.aurora.cache

import androidx.room.TypeConverter
import com.shkcodes.aurora.api.response.Url
import com.shkcodes.aurora.util.fromJsonArray
import com.shkcodes.aurora.util.toJson
import com.squareup.moshi.Moshi
import java.time.ZonedDateTime

internal val moshi = Moshi.Builder().build()

class CacheConverter {

    @TypeConverter
    fun fromDateTime(dateTime: ZonedDateTime): String = dateTime.toString()

    @TypeConverter
    fun toDateTime(dateTime: String): ZonedDateTime = ZonedDateTime.parse(dateTime)

    @TypeConverter
    fun fromUrls(url: List<Url>): String = moshi.toJson(url)

    @TypeConverter
    fun toUrls(urls: String): List<Url> = moshi.fromJsonArray(urls)

    @TypeConverter
    fun fromHashtags(hashtags: List<String>): String = moshi.toJson(hashtags)

    @TypeConverter
    fun toHashtags(hashtags: String): List<String> = moshi.fromJsonArray(hashtags)
}
