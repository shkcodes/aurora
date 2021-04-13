package com.shkcodes.aurora.api.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val DATE_TIME_FORMAT = "EEE MMM dd HH:mm:ss ZZZ yyyy"

@Singleton
class DateTimeAdapter @Inject constructor() {

    @ToJson
    fun toJson(dateTime: ZonedDateTime): String {
        return dateTime.toString()
    }

    @FromJson
    fun fromJson(dateTimeString: String): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
        return OffsetDateTime.parse(dateTimeString, formatter)
            .atZoneSameInstant(ZoneId.systemDefault())
    }
}
