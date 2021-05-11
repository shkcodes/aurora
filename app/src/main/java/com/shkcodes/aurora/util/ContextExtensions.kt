package com.shkcodes.aurora.util

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.LayoutRes

fun <T> Context.inflate(@LayoutRes layoutId: Int): T =
    LayoutInflater.from(this).inflate(layoutId, null) as T
