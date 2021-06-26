package com.shkcodes.aurora.util

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat

fun <T> Context.inflate(@LayoutRes layoutId: Int): T =
    LayoutInflater.from(this).inflate(layoutId, null) as T

fun Context.openUrl(link: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
}

fun Context.dp(@DimenRes dimen: Int): Float {
    return resources.getDimension(dimen)
}

fun Context.pixelSize(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}
