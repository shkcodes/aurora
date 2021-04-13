package com.shkcodes.aurora.util

import android.text.format.DateUtils
import java.time.ZonedDateTime

fun ZonedDateTime.toPrettyTime(): String {
    return DateUtils.getRelativeTimeSpanString(toInstant().toEpochMilli())
        .toString().replace("hours", "h").replace("hour", "h").replace("minutes", "m")
        .replace("minute", "m")
}
