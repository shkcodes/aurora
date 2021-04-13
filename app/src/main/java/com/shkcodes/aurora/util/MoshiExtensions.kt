package com.shkcodes.aurora.util

import com.squareup.moshi.Moshi

inline fun <reified T> Moshi.fromJson(json: String): T = adapter(T::class.java).fromJson(json)!!
