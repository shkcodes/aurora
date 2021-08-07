package com.shkcodes.aurora.cache

import android.content.SharedPreferences
import androidx.core.content.edit
import com.shkcodes.aurora.util.getData
import com.shkcodes.aurora.util.setData
import com.squareup.moshi.Moshi
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) {

    internal companion object {
        const val SCHEMA_VERSION = 1
    }

    init {
        if (sharedPreferences.getInt(PrefKey.SCHEMA_VERSION, 0) != SCHEMA_VERSION) {
            sharedPreferences.edit {
                clear()
                putInt(PrefKey.SCHEMA_VERSION, SCHEMA_VERSION)
            }
        }
    }

    var authorization: Authorization? = getData(PrefKey.AUTHORIZATION)
        set(value) {
            field = value
            setData(PrefKey.AUTHORIZATION, value)
        }

    var timelineRefreshTime: LocalDateTime =
        getData(PrefKey.HOME_TIMELINE_REFRESH_TIME) ?: LocalDateTime.MIN
        set(value) {
            field = value
            setData(PrefKey.HOME_TIMELINE_REFRESH_TIME, value)
        }

    var autoplayVideos: Boolean = getData(PrefKey.AUTOPLAY_VIDEOS) ?: true
        set(value) {
            field = value
            setData(PrefKey.AUTOPLAY_VIDEOS, value)
        }

    private inline fun <reified T> getData(key: String): T? = sharedPreferences.getData(key, moshi)

    private inline fun <reified T> setData(key: String, data: T?, commit: Boolean = false) {
        sharedPreferences.setData(key, data, moshi, commit)
    }
}

internal object PrefKey {
    const val SCHEMA_VERSION = "schema_version"
    const val AUTHORIZATION = "authorization"
    const val HOME_TIMELINE_REFRESH_TIME = "home_timeline_refresh_time"
    const val AUTOPLAY_VIDEOS = "autoplay_videos"
}
