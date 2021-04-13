package com.shkcodes.aurora.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi

inline fun <reified T> SharedPreferences.getData(key: String, moshi: Moshi): T? {
    return when (T::class) {
        String::class -> getString(key, null) as? T
        Int::class -> getInt(key, 0) as T
        Long::class -> getLong(key, 0) as T
        Boolean::class -> getBoolean(key, false) as T
        Float::class -> getFloat(key, 0f) as T
        else -> {
            val data = getString(key, null)
            if (data != null && data.isNotEmpty()) {
                moshi.fromJson<T>(data)
            } else {
                null
            }
        }
    }
}

inline fun <reified T> SharedPreferences.setData(key: String, data: T?, moshi: Moshi, commit: Boolean = false) {
    edit(commit) {
        if (data == null) {
            remove(key)
        } else {
            when (data) {
                is String -> putString(key, data)
                is Int -> putInt(key, data)
                is Long -> putLong(key, data)
                is Boolean -> putBoolean(key, data)
                is Float -> putFloat(key, data)
                else -> putString(key, moshi.adapter(T::class.java).toJson(data))
            }
        }
    }
}
