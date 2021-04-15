package com.shkcodes.aurora.cache

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import java.time.ZonedDateTime

internal val moshi = Moshi.Builder().build()

class CacheConverter {

    @TypeConverter
    fun fromDateTime(dateTime: ZonedDateTime): String = dateTime.toString()

    @TypeConverter
    fun toDateTime(dateTime: String): ZonedDateTime = ZonedDateTime.parse(dateTime)
}
