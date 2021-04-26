package com.shkcodes.aurora.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

inline fun <reified T> Moshi.fromJson(json: String): T = adapter(T::class.java).fromJson(json)!!
inline fun <reified T> Moshi.toJson(data: T): String = adapter(T::class.java).toJson(data)
inline fun <reified T> Moshi.fromJsonArray(json: String): List<T> =
    adapter<List<T>>(Types.newParameterizedType(List::class.java, T::class.java)).fromJson(json)!!
