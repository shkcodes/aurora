package com.shkcodes.aurora.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.LayoutRes

fun <T> Context.inflate(@LayoutRes layoutId: Int): T =
    LayoutInflater.from(this).inflate(layoutId, null) as T

fun Context.openUrl(link: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
}
