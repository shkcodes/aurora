package com.shkcodes.aurora.util

import com.squareup.moshi.Moshi

inline fun <reified T> Moshi.fromJson(json: String): T = adapter(T::class.java).fromJson(json)!!
inline fun <reified T> Moshi.toJson(data: T): String = adapter(T::class.java).toJson(data)
