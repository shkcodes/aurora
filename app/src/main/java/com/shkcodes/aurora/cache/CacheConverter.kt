package com.shkcodes.aurora.cache

import androidx.room.TypeConverter
import com.shkcodes.aurora.api.response.Url
import com.shkcodes.aurora.cache.entities.TweetType
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
    fun fromStringList(list: List<String>): String = moshi.toJson(list)

    @TypeConverter
    fun toStringList(list: String): List<String> = moshi.fromJsonArray(list)

    @TypeConverter
    fun fromTweetType(type: TweetType): String = type.name

    @TypeConverter
    fun toTweetType(name: String): TweetType = enumValueOf(name)
}
