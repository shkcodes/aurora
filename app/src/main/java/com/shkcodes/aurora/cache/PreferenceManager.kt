package com.shkcodes.aurora.cache

import android.content.SharedPreferences
import androidx.core.content.edit
import com.shkcodes.aurora.util.getData
import com.shkcodes.aurora.util.setData
import com.squareup.moshi.Moshi
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

    var isLoggedIn: Boolean = false
        get() = getData(PrefKey.IS_LOGGED_IN) ?: false
        set(value) {
            field = value
            setData(PrefKey.IS_LOGGED_IN, value)
        }

    private inline fun <reified T> getData(key: String): T? = sharedPreferences.getData(key, moshi)

    private inline fun <reified T> setData(key: String, data: T?, commit: Boolean = false) {
        sharedPreferences.setData(key, data, moshi, commit)
    }
}

internal object PrefKey {
    const val SCHEMA_VERSION = "schema_version"
    const val IS_LOGGED_IN = "is_logged_in"
}
